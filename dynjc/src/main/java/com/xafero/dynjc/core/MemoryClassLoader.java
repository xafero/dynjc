package com.xafero.dynjc.core;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class MemoryClassLoader extends URLClassLoader {

	private final Map<String, byte[]> classBytes;

	public MemoryClassLoader(Map<String, byte[]> classBytes, String classPath, ClassLoader parent) {
		super(toURLs(classPath), parent);
		this.classBytes = classBytes;
	}

	public MemoryClassLoader(Map<String, byte[]> classBytes, String classPath) {
		this(classBytes, classPath, null);
	}

	public void inject(String key, byte[] value) {
		classBytes.put(key, value);
	}

	public Iterable<Class<?>> loadAll() throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>(classBytes.size());
		for (String name : classBytes.keySet())
			classes.add(loadClass(name));
		return classes;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] buf = classBytes.get(name);
		if (buf != null) {
			classBytes.remove(name);
			return defineClass(name, buf, 0, buf.length);
		}
		ClassLoader parent = getParent();
		if (parent != null) {
			try {
				Method fcm = ClassLoader.class.getDeclaredMethod("findClass", String.class);
				fcm.setAccessible(true);
				Object clazz = fcm.invoke(parent, name);
				if (clazz != null)
					return (Class<?>) clazz;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return super.findClass(name);
	}

	private static URL[] toURLs(String classPath) {
		if (classPath == null)
			return new URL[0];
		List<URL> list = new ArrayList<URL>();
		StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			File file = new File(token);
			if (file.exists())
				try {
					list.add(file.toURI().toURL());
				} catch (MalformedURLException mue) {
				}
			else
				try {
					list.add(new URL(token));
				} catch (MalformedURLException mue) {
				}
		}
		return list.toArray(new URL[list.size()]);
	}
}