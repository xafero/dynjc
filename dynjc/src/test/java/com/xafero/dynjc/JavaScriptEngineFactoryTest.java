package com.xafero.dynjc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class JavaScriptEngineFactoryTest {

	private static ScriptEngineFactory factory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ScriptEngineManager mgr = new ScriptEngineManager();
		factory = mgr.getEngineByExtension("java").getFactory();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		factory = null;
	}

	@Test
	public void testGetEngineName() {
		assertEquals("Eclipse Compiler for Java(TM)", factory.getEngineName());
	}

	@Test
	public void testGetEngineVersion() {
		assertEquals("v20150120-1634, 3.10.2", factory.getEngineVersion());
	}

	@Test
	public void testGetLanguageName() {
		assertEquals("Java", factory.getLanguageName());
	}

	@Test
	public void testGetLanguageVersion() {
		assertEquals("1.8", factory.getLanguageVersion());
	}

	@Test
	public void testGetNames() {
		assertEquals("[java]", factory.getNames() + "");
	}

	@Test
	public void testGetMimeTypes() {
		assertEquals("[text/x-java-source]", factory.getMimeTypes() + "");
	}

	@Test
	public void testGetExtensions() {
		assertEquals("[java]", factory.getExtensions() + "");
	}

	@Test
	public void testGetMethodCallSyntax() {
		assertEquals("adder.add(42, b)", factory.getMethodCallSyntax("adder", "add", "42", "b"));
	}

	@Test
	public void testGetOutputStatement() {
		assertEquals("System.out.print(\"Hello\");", factory.getOutputStatement("Hello"));
	}

	@Test
	public void testGetParameter() {
		assertEquals("1.8", factory.getParameter(ScriptEngine.LANGUAGE_VERSION));
	}

	@Test
	public void testGetProgram() {
		assertEquals(
				"class com_xafero_dynjc_Main$1 {|  " + "  public static void main(String[] args) {|    "
						+ "    int c = a + b;|    " + "    float d = 42.0;|    }|}|",
				factory.getProgram("int c = a + b", "float d = 42.0").replace('\n', '|').trim());
	}

	@Test
	public void testGetScriptEngine() {
		ScriptEngine engine;
		assertNotNull(engine = factory.getScriptEngine());
		assertTrue(engine instanceof JavaScriptEngine);
	}
}