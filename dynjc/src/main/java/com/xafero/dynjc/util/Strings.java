package com.xafero.dynjc.util;

import java.util.Arrays;

public final class Strings {

	public static final String lineSeparator = System.getProperty("line.separator");

	private Strings() {
	}

	public static <T> String toSimpleString(String separator, T[] items) {
		return toSimpleString(separator, Arrays.asList(items));
	}

	public static <T> String toSimpleString(String separator, Iterable<T> items) {
		StringBuilder bld = new StringBuilder();
		for (Object item : items) {
			if (bld.length() > 1)
				bld.append(separator);
			bld.append(item + "");
		}
		return bld.toString();
	}
}