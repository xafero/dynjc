package com.xafero.dynjc;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xafero.dynjc.core.EclipseCompiler;
import com.xafero.dynjc.core.EclipseMessage;
import com.xafero.dynjc.core.GeneratedBinary;
import com.xafero.dynjc.core.GeneratedSource;
import com.xafero.dynjc.core.JavaCompiledScript;
import com.xafero.dynjc.core.MemoryClassLoader;
import com.xafero.dynjc.util.Files;
import com.xafero.dynjc.util.Strings;

public class JavaScriptEngine extends AbstractScriptEngine implements ScriptEngine, Compilable {

	private static final Logger log = LoggerFactory.getLogger("ecj");

	private final JavaScriptEngineFactory factory;
	private final EclipseCompiler compiler;
	private final MemoryClassLoader loader;

	public JavaScriptEngine(JavaScriptEngineFactory factory) throws IOException {
		this.factory = factory;
		this.compiler = new EclipseCompiler();
		this.loader = new MemoryClassLoader(new HashMap<String, byte[]>(), null, getClass().getClassLoader());
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}

	@Override
	public Bindings createBindings() {
		return new SimpleBindings();
	}

	@Override
	public CompiledScript compile(String script) throws ScriptException {
		try {
			return new JavaCompiledScript(this, script, File.createTempFile("ecj_cmp", ".java"));
		} catch (IOException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		try {
			// Set output file
			File file = File.createTempFile("ecj_evl", ".java");
			// Generate source
			GeneratedSource src = generateSource(file, context, script);
			// Compile it
			GeneratedBinary bin = compile(src);
			// Load it
			Class<?> clazz = load(bin.file, bin.qualName);
			// Evaluate it
			Bindings bnd = context.getBindings(ScriptContext.ENGINE_SCOPE);
			return evalClass(clazz, bnd);
		} catch (Exception e) {
			throw new ScriptException(e);
		}
	}

	public Class<?> load(File bin, String qualName) throws IOException, ClassNotFoundException {
		// Read bytes into class loader
		byte[] bytes = Files.readBytes(bin);
		loader.inject(qualName, bytes);
		// Get class and its instance
		return loader.loadClass(qualName);
	}

	public GeneratedBinary compile(GeneratedSource src) throws ScriptException {
		boolean compiled = compiler.compile(src.file);
		log.info("Compiling '{}' => {}", src.file, compiled);
		// Handle compile errors
		if (!compiled) {
			List<EclipseMessage> lastMsgs = compiler.getLastMessages();
			String errorTxt = Strings.toSimpleString(Strings.lineSeparator, lastMsgs);
			throw new ScriptException(errorTxt);
		}
		// Build qualified name
		String qualName = src.packageName + '.' + src.className;
		return new GeneratedBinary(new File(compiler.getOutDir(), qualName.replace('.', File.separatorChar) + ".class"),
				qualName);
	}

	public GeneratedSource generateSource(File file, ScriptContext ctx, String script) throws IOException {
		// Set package' and class' name
		String pkgName = "com.xafero.dynjc.scripts";
		String className = file.getName().replace(".java", "");
		// Write class header
		List<String> lines = new LinkedList<String>();
		lines.add(String.format("package %s;", pkgName));
		lines.add("");
		lines.add("import java.io.*;");
		lines.add("import java.util.*;");
		lines.add("import java.util.concurrent.*;");
		lines.add("");
		lines.add(String.format("public class %s implements Callable<Object> {", className));
		lines.add("");
		// Write setup constructor
		lines.add(String.format("\t" + "public %s(%s ctx) {", className, Bindings.class.getName()));
		// Inject environment for assignment
		Bindings bnd = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
		for (String key : bnd.keySet()) {
			Object value = bnd.get(key);
			Class<?> clazz = value.getClass();
			String type = clazz.getName();
			lines.add(String.format("\t\t" + "this.%s = (%s) ctx.get(\"%s\");", key, type, key));
			// Fetch source of code
			CodeSource cs = clazz.getProtectionDomain().getCodeSource();
			if (cs != null) {
				File rf = new File(URI.create(cs.getLocation() + ""));
				compiler.addToClassPath(rf);
			}
		}
		// Rest of constructor
		lines.add('\t' + "}");
		lines.add("");
		// Inject environment for declaration
		for (String key : bnd.keySet()) {
			Object value = bnd.get(key);
			String type = value.getClass().getName();
			lines.add(String.format('\t' + "private %s %s;", type, key));
		}
		// Write script contents
		lines.add("");
		lines.add('\t' + "@Override");
		lines.add('\t' + "public Object call() throws Exception {");
		lines.add("\t\t" + script);
		lines.add('\t' + "}");
		lines.add("}");
		return new GeneratedSource(Files.saveToFile(lines, file), pkgName, className);
	}

	public Object evalClass(Class<?> clazz, Bindings ctx) throws ScriptException {
		try {
			Object obj = clazz.getConstructor(Bindings.class).newInstance(ctx);
			Callable<?> inst = (Callable<?>) obj;
			return inst.call();
		} catch (Exception e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public CompiledScript compile(Reader reader) throws ScriptException {
		return compile(Files.readFully(reader));
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		return eval(Files.readFully(reader), context);
	}
}