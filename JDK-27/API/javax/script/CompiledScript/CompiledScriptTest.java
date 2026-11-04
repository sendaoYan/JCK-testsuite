/*
 * @test
 * @summary javax.script.CompiledScript conformance: white-box structure + black-box behavior.
 * @run main CompiledScriptTest
 */

import java.lang.reflect.*;
import java.io.*;

public class CompiledScriptTest {
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

    static class MinCompiled extends javax.script.CompiledScript {
        final javax.script.ScriptEngine eng = new MinEngine();
        javax.script.ScriptContext lastCtx;
        public Object eval(javax.script.ScriptContext ctx) {
            lastCtx = ctx;
            return "RESULT";
        }
        public javax.script.ScriptEngine getEngine() { return eng; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.CompiledScript");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "eval"), "declares eval");
        check(hasMethod(c, "getEngine"), "declares getEngine");
    }

    static void testBlackBox() throws Exception {
        MinCompiled cs = new MinCompiled();
        check(cs.getEngine() != null, "getEngine non-null");

        check("RESULT".equals(cs.eval()), "eval() delegates to engine context");
        check(cs.lastCtx == cs.getEngine().getContext(), "eval() used engine context");

        javax.script.Bindings b = new javax.script.SimpleBindings();
        b.put("k", "v");
        check("RESULT".equals(cs.eval(b)), "eval(Bindings) returns result");
        check(cs.lastCtx != null && "v".equals(cs.lastCtx.getAttribute("k", javax.script.ScriptContext.ENGINE_SCOPE)),
                "eval(Bindings) engine scope has bindings");

        check("RESULT".equals(cs.eval((javax.script.Bindings) null)), "eval(null Bindings) uses engine context");
        check(cs.lastCtx == cs.getEngine().getContext(), "eval(null) used engine context");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
