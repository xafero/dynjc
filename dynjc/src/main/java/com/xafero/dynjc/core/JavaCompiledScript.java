package com.xafero.dynjc.core;

import java.io.File;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.xafero.dynjc.JavaScriptEngine;

public class JavaCompiledScript extends CompiledScript {

	private final JavaScriptEngine engine;
	private final String script;
	private final File file;

	private Class<?> cachedClazz;

	public JavaCompiledScript(JavaScriptEngine engine, String script, File file) {
		this.engine = engine;
		this.script = script;
		this.file = file;
	}

	@Override
	public Object eval(ScriptContext context) throws ScriptException {
		// Only compile if necessary (should be only the first time!)
		if (cachedClazz == null) {
			try {
				// Generate source
				GeneratedSource src = engine.generateSource(file, context, script);
				// Compile it
				GeneratedBinary bin = engine.compile(src);
				// Load it
				cachedClazz = engine.load(bin.file, bin.qualName);
			} catch (Exception e) {
				throw new ScriptException(e);
			}
		}
		// Evaluate it
		Bindings bnd = context.getBindings(ScriptContext.ENGINE_SCOPE);
		return engine.evalClass(cachedClazz, bnd);
	}

	@Override
	public ScriptEngine getEngine() {
		return engine;
	}
}