package com.xafero.dynjc.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.List;

import javax.script.ScriptException;

public final class Files {

	private Files() {
	}

	public static File saveToFile(String text, File file) throws IOException {
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
		out.write(text);
		out.flush();
		out.close();
		return file;
	}

	public static File saveToFile(List<String> lines, File file) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
		for (String line : lines) {
			out.write(line);
			out.newLine();
		}
		out.flush();
		out.close();
		return file;
	}

	public static String readFully(Reader reader) throws ScriptException {
		char[] arr = new char[8 * 1024];
		StringBuilder buf = new StringBuilder();
		int numChars;
		try {
			while ((numChars = reader.read(arr, 0, arr.length)) > 0)
				buf.append(arr, 0, numChars);
		} catch (IOException exp) {
			throw new ScriptException(exp);
		}
		return buf.toString();
	}

	public static byte[] readBytes(File file) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileInputStream in = new FileInputStream(file);
		byte[] array = new byte[8 * 1024];
		int bytes;
		while ((bytes = in.read(array)) >= 0)
			out.write(array, 0, bytes);
		out.flush();
		out.close();
		in.close();
		return out.toByteArray();
	}
}