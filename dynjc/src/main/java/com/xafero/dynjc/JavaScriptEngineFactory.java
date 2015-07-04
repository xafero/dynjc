package com.xafero.dynjc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.eclipse.jdt.internal.compiler.batch.Main;

public class JavaScriptEngineFactory implements ScriptEngineFactory {

	public static final String SpecVersion = System.getProperty("java.specification.version");

	private static final String langName = "Java";
	private static final String shortName = "java";

	private static final AtomicLong classNumber = new AtomicLong(0L);
	private static final List<String> names = Arrays.asList(shortName);
	private static final List<String> extensions = Arrays.asList("java");
	private static final List<String> mimeTypes = Arrays.asList("text/x-java-source");

	private static final String engineName;
	private static final String engineVer;

	static {
		@SuppressWarnings("deprecation")
		Main main = new Main(null, null, false);
		engineName = main.bind("compiler.name");
		engineVer = main.bind("compiler.version");
	}

	@Override
	public String getEngineName() {
		return engineName;
	}

	@Override
	public String getEngineVersion() {
		return engineVer;
	}

	@Override
	public String getLanguageName() {
		return langName;
	}

	@Override
	public String getLanguageVersion() {
		return SpecVersion;
	}

	@Override
	public List<String> getNames() {
		return names;
	}

	@Override
	public List<String> getMimeTypes() {
		return mimeTypes;
	}

	@Override
	public List<String> getExtensions() {
		return extensions;
	}

	@Override
	public String getMethodCallSyntax(String obj, String method, String... args) {
		StringBuilder buf = new StringBuilder();
		buf.append(obj);
		buf.append(".");
		buf.append(method);
		buf.append("(");
		if (args.length != 0) {
			int i = 0;
			for (; i < args.length - 1; i++) {
				buf.append(args[i] + ", ");
			}
			buf.append(args[i]);
		}
		buf.append(")");
		return buf.toString();
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		StringBuilder buf = new StringBuilder();
		buf.append("System.out.print(\"");
		int len = toDisplay.length();
		for (int i = 0; i < len; i++) {
			char ch = toDisplay.charAt(i);
			switch (ch) {
			case '"':
				buf.append("\\\"");
				break;
			case '\\':
				buf.append("\\\\");
				break;
			default:
				buf.append(ch);
				break;
			}
		}
		buf.append("\");");
		return buf.toString();
	}

	@Override
	public Object getParameter(String key) {
		if (key.equals(ScriptEngine.ENGINE))
			return getEngineName();
		if (key.equals(ScriptEngine.ENGINE_VERSION))
			return getEngineVersion();
		if (key.equals(ScriptEngine.NAME))
			return shortName;
		if (key.equals(ScriptEngine.LANGUAGE))
			return getLanguageName();
		if (key.equals(ScriptEngine.LANGUAGE_VERSION))
			return getLanguageVersion();
		return null;
	}

	@Override
	public String getProgram(String... statements) {
		StringBuilder buf = new StringBuilder();
		buf.append("class ");
		buf.append(getClassName());
		buf.append(" {\n");
		buf.append("    public static void main(String[] args) {\n");
		if (statements.length != 0) {
			for (int i = 0; i < statements.length; i++) {
				buf.append("        ");
				buf.append(statements[i]);
				buf.append(";\n");
			}
		}
		buf.append("    }\n");
		buf.append("}\n");
		return buf.toString();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		try {
			return new JavaScriptEngine(this, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getClassName() {
		return "com_xafero_dynjc_Main$" + classNumber.incrementAndGet();
	}
}