package com.xafero.dynjc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class JavaScriptEngineTest {

	private static ScriptEngine engine;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ScriptEngineManager mgr = new ScriptEngineManager();
		engine = mgr.getEngineByExtension("java");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		engine = null;
	}

	@Test
	public void testEvalFromString() throws Exception {
		StringWriter out;
		Bindings bnd = engine.createBindings();
		bnd.put("it", new File("test1.txt"));
		bnd.put("that", new Scanner("test2.txt"));
		bnd.put("out", new PrintWriter(out = new StringWriter()));
		String code = "out.println(\"Hello world! \"+it+\" \"+that.next()); return null;";
		Object result = engine.eval(code, bnd);
		assertNull(result);
		assertEquals("Hello world! test1.txt test2.txt", out.toString().trim());
	}

	@Test
	public void testCompileFromString() throws Exception {
		String code = "return cnt.incrementAndGet();";
		CompiledScript script = ((Compilable) engine).compile(code);
		Bindings bnd = engine.createBindings();
		AtomicLong cnt;
		bnd.put("cnt", cnt = new AtomicLong(0L));
		for (int i = 0; i < 1000; i++) {
			Object result = script.eval(bnd);
			assertNotNull(result);
			assertTrue(result instanceof Long);
		}
		assertNotNull(cnt);
		assertEquals(1000L, cnt.get());
	}

	@Test
	public void testEvalWithImport() throws Exception {
		String code = "import javax.script.Bindings; import javax.script.Compilable; "
				+ "return Bindings.class.getSimpleName() + \" \" + Compilable.class.getSimpleName();";
		Object result = engine.eval(code);
		assertNotNull(result);
		assertEquals("Bindings Compilable", result);
	}

	@Test
	public void testGetFactory() {
		ScriptEngineFactory factory;
		assertNotNull(factory = engine.getFactory());
		assertTrue(factory instanceof JavaScriptEngineFactory);
	}

	@Test
	public void testCreateBindings() {
		assertNotNull(engine.createBindings());
	}
}