package com.xafero.dynjc.core;

import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xafero.dynjc.JavaScriptEngineFactory;
import com.xafero.dynjc.util.Strings;

public class EclipseCompiler {

	private static final Logger log = LoggerFactory.getLogger("ecj");

	private float javaVer;
	private List<String> classpath;
	private File outDir;
	private List<EclipseMessage> messages;

	public EclipseCompiler() throws IOException {
		javaVer = Float.parseFloat(JavaScriptEngineFactory.SpecVersion);
		classpath = new LinkedList<String>();
		classpath.add("rt.jar");
		outDir = File.createTempFile("ecj_tmp", ".tmp").getParentFile();
	}

	public boolean compile(File... files) {
		String cpStr = Strings.toSimpleString(File.pathSeparator, classpath);
		String fileStr = Strings.toSimpleString(" ", files);
		String warnOpt = "-nowarn";
		// First stage: Only annotations
		boolean result = compile(cpStr, warnOpt, "-proc:only", fileStr);
		if (!result)
			return result;
		// Second stage: Real compilation
		addClassLoader((URLClassLoader) getClass().getClassLoader());
		addClassLoader((URLClassLoader) ClassLoader.getSystemClassLoader());
		return compile(cpStr, warnOpt, "-proc:none", fileStr);
	}

	private boolean compile(String cpStr, String warnOpt, String annotOpt, String fileStr) {
		String cmd = String.format("-%s -classpath %s %s %s -d %s %s", javaVer, cpStr, warnOpt, annotOpt, outDir,
				fileStr);
		log.info("Executing => {}", cmd);
		CompilationProgress progress = new LogCompilationProgress();
		StringWriter stdOut = new StringWriter();
		PrintWriter out = new PrintWriter(stdOut);
		StringWriter errorOut = new StringWriter();
		PrintWriter err = new PrintWriter(errorOut);
		boolean result = BatchCompiler.compile(cmd, out, err, progress);
		log.info("Result => [{}] {} {}", result ? "OK" : "FAIL", stdOut, errorOut);
		messages = parseMessages(errorOut);
		return result;
	}

	public List<EclipseMessage> getLastMessages() {
		return messages;
	}

	public File getOutDir() {
		return outDir;
	}

	private List<EclipseMessage> parseMessages(StringWriter errorOut) {
		List<EclipseMessage> messages = new LinkedList<EclipseMessage>();
		for (String item : errorOut.toString().split("----------")) {
			item = item.trim();
			if (item.isEmpty())
				continue;
			String[] parts = item.split(Strings.lineSeparator);
			if (parts.length < 4)
				continue;
			String place = parts[1].trim();
			String msg = parts[3].trim();
			String first = parts[0].trim();
			parts = first.split(" in ");
			String[] subParts = parts[0].split(". ");
			int id = Integer.parseInt(subParts[0]);
			MessageType mtype = MessageType.valueOf(subParts[1]);
			subParts = parts[1].split(" \\(at line ");
			if (subParts.length != 2)
				continue;
			File file = new File(subParts[0].trim());
			int line = Integer.parseInt(subParts[1].replace(')', ' ').trim());
			messages.add(new EclipseMessage(file, id, mtype, line, place, msg));
		}
		return messages;
	}

	public boolean addToClassPath(File entry) {
		if (!entry.exists() || !entry.canRead())
			return false;
		String path = entry.getAbsolutePath();
		if (classpath.contains(path))
			return false;
		classpath.add(path);
		return true;
	}

	public void addJavaClassPath() {
		for (String path : System.getProperty("java.class.path").split(File.pathSeparator))
			addToClassPath(new File(path));
	}

	public void addClassLoader(URLClassLoader loader) {
		for (URL url : loader.getURLs())
			addToClassPath(new File(URI.create(url + "")));
	}
}