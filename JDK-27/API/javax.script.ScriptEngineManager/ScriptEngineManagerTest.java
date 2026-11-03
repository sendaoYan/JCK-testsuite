/*
 * @test
 * @summary javax.script.ScriptEngineManager conformance: white-box structure + black-box behavior.
 * @run main ScriptEngineManagerTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

public class ScriptEngineManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class MinEngine extends javax.script.AbstractScriptEngine {
        public Object eval(String s, javax.script.ScriptContext ctx) { return "E"; }
        public Object eval(Reader r, javax.script.ScriptContext ctx) { return "E"; }
        public javax.script.Bindings createBindings() { return new javax.script.SimpleBindings(); }
        public javax.script.ScriptEngineFactory getFactory() { return null; }
    }

    static javax.script.ScriptEngineFactory makeFactory() {
        return new javax.script.ScriptEngineFactory() {
            public String getEngineName() { return "MyEngine"; }
            public String getEngineVersion() { return "1.0"; }
            public List<String> getExtensions() { return List.of("me"); }
            public List<String> getMimeTypes() { return List.of("application/x-me"); }
            public List<String> getNames() { return List.of("MyEngine", "myengine"); }
            public String getLanguageName() { return "MyLang"; }
            public String getLanguageVersion() { return "1.0"; }
            public Object getParameter(String key) { return null; }
            public String getMethodCallSyntax(String o, String m, String... a) { return o + "." + m + "()"; }
            public String getOutputStatement(String s) { return s; }
            public String getProgram(String... s) { return String.join("", s); }
            public javax.script.ScriptEngine getScriptEngine() { return new MinEngine(); }
        };
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.ScriptEngineManager");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getEngineFactories"), "declares getEngineFactories");
        check(hasMethod(c, "getEngineByName"), "declares getEngineByName");
        check(hasMethod(c, "getEngineByExtension"), "declares getEngineByExtension");
        check(hasMethod(c, "getEngineByMimeType"), "declares getEngineByMimeType");
        check(hasMethod(c, "registerEngineName"), "declares registerEngineName");
    }

    static void testBlackBox() throws Exception {
        javax.script.ScriptEngineManager m = new javax.script.ScriptEngineManager();
        check(m.getEngineFactories() != null, "getEngineFactories non-null");
        check(m.getBindings() != null, "getBindings non-null");

        m.put("k", "v");
        check("v".equals(m.get("k")), "put/get");

        javax.script.Bindings b = new javax.script.SimpleBindings();
        m.setBindings(b);
        check(m.getBindings() == b, "setBindings/getBindings");

        check(m.getEngineByName("no_such_engine_xyz") == null, "unknown engine null");
        check(m.getEngineByExtension("no_such_ext_xyz") == null, "unknown extension null");
        check(m.getEngineByMimeType("no/such/mime/xyz") == null, "unknown mime type null");

        javax.script.ScriptEngineFactory f = makeFactory();
        m.registerEngineName("MyEngine", f);
        check(m.getEngineByName("MyEngine") != null, "registered name resolved");
        check(m.getEngineByName("MyEngine") instanceof javax.script.ScriptEngine, "registered engine type");

        m.registerEngineExtension("me", f);
        check(m.getEngineByExtension("me") != null, "registered extension resolved");

        m.registerEngineMimeType("application/x-me", f);
        check(m.getEngineByMimeType("application/x-me") != null, "registered mime type resolved");

        boolean npe1 = false;
        try { m.registerEngineName(null, f); } catch (NullPointerException e) { npe1 = true; }
        check(npe1, "registerEngineName null name NPE");
        boolean npe2 = false;
        try { m.registerEngineName("x", null); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "registerEngineName null factory NPE");

        javax.script.ScriptEngineManager m2 = new javax.script.ScriptEngineManager(
                ScriptEngineManagerTest.class.getClassLoader());
        check(m2.getEngineFactories() != null, "ctor ClassLoader factories non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
