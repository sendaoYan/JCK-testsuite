/*
 * @test
 * @summary javax.script.ScriptEngine conformance: white-box structure + black-box behavior.
 * @run main ScriptEngineTest
 */

import java.lang.reflect.*;
import java.io.*;

public class ScriptEngineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class MinEngine extends javax.script.AbstractScriptEngine {
        public Object eval(String s, javax.script.ScriptContext ctx) { return "EVAL:" + s; }
        public Object eval(Reader r, javax.script.ScriptContext ctx) { return "EVAL-READER"; }
        public javax.script.Bindings createBindings() { return new javax.script.SimpleBindings(); }
        public javax.script.ScriptEngineFactory getFactory() { return null; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.ScriptEngine");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "eval"), "declares eval");
        check(hasMethod(c, "put"), "declares put");
        check(hasMethod(c, "get"), "declares get");
        check(hasMethod(c, "getBindings"), "declares getBindings");
        check(hasMethod(c, "setBindings"), "declares setBindings");
    }

    static void testBlackBox() throws Exception {
        check("javax.script.argv".equals(javax.script.ScriptEngine.ARGV), "ARGV constant");
        check("javax.script.filename".equals(javax.script.ScriptEngine.FILENAME), "FILENAME constant");
        check("javax.script.engine".equals(javax.script.ScriptEngine.ENGINE), "ENGINE constant");
        check("javax.script.engine_version".equals(javax.script.ScriptEngine.ENGINE_VERSION), "ENGINE_VERSION constant");
        check("javax.script.name".equals(javax.script.ScriptEngine.NAME), "NAME constant");
        check("javax.script.language".equals(javax.script.ScriptEngine.LANGUAGE), "LANGUAGE constant");
        check("javax.script.language_version".equals(javax.script.ScriptEngine.LANGUAGE_VERSION), "LANGUAGE_VERSION constant");

        javax.script.ScriptEngine e = new MinEngine();
        e.put("k", "v");
        check("v".equals(e.get("k")), "put/get");
        check("EVAL:code".equals(e.eval("code")), "eval(String)");
        check("EVAL-READER".equals(e.eval(new StringReader("r"))), "eval(Reader)");
        check("EVAL:code".equals(e.eval("code", new javax.script.SimpleBindings())), "eval(String,Bindings)");
        check(e.createBindings() != null, "createBindings non-null");
        check(e.getContext() != null, "getContext non-null");
        check(e.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) != null, "getBindings engine scope");
        check(e.getFactory() == null, "getFactory null (minimal impl)");

        javax.script.Bindings b = new javax.script.SimpleBindings();
        e.setBindings(b, javax.script.ScriptContext.ENGINE_SCOPE);
        check(e.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) == b, "set/get bindings");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
