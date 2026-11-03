/*
 * @test
 * @summary javax.script.AbstractScriptEngine conformance: white-box structure + black-box behavior.
 * @run main AbstractScriptEngineTest
 */

import java.lang.reflect.*;
import java.io.*;

public class AbstractScriptEngineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static class MinEngine extends javax.script.AbstractScriptEngine {
        MinEngine() { }
        MinEngine(javax.script.Bindings b) { super(b); }
        public Object eval(String script, javax.script.ScriptContext ctx) throws javax.script.ScriptException {
            return "EVAL:" + script;
        }
        public Object eval(Reader reader, javax.script.ScriptContext ctx) throws javax.script.ScriptException {
            return "EVAL-READER";
        }
        public javax.script.Bindings createBindings() { return new javax.script.SimpleBindings(); }
        public javax.script.ScriptEngineFactory getFactory() { return null; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.AbstractScriptEngine");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(c.getInterfaces().length == 1, "1 interface(s)");
        check(hasInterface(c, "javax.script.ScriptEngine"), "implements ScriptEngine");
        check(hasMethod(c, "setContext"), "declares setContext");
        check(hasMethod(c, "getContext"), "declares getContext");
        check(hasMethod(c, "getBindings"), "declares getBindings");
        check(hasMethod(c, "setBindings"), "declares setBindings");
        check(hasMethod(c, "put"), "declares put");
    }

    static void testBlackBox() throws Exception {
        MinEngine e = new MinEngine();
        check(e.getContext() != null, "default context non-null");
        check(e.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) != null, "default engine scope non-null");
        check(e.getBindings(javax.script.ScriptContext.GLOBAL_SCOPE) == null, "default global scope null");

        e.put("k", "v");
        check("v".equals(e.get("k")), "put/get round trip");

        javax.script.Bindings g = new javax.script.SimpleBindings();
        e.setBindings(g, javax.script.ScriptContext.GLOBAL_SCOPE);
        check(e.getBindings(javax.script.ScriptContext.GLOBAL_SCOPE) == g, "set/get global bindings");

        boolean iae = false;
        try { e.getBindings(12345); } catch (IllegalArgumentException ex) { iae = true; }
        check(iae, "getBindings invalid scope throws IAE");

        boolean npe = false;
        try { e.setBindings(null, javax.script.ScriptContext.ENGINE_SCOPE); } catch (NullPointerException ex) { npe = true; }
        check(npe, "setBindings null engine scope throws NPE");

        check("EVAL:hello".equals(e.eval("hello")), "eval(String) delegates");
        check("EVAL-READER".equals(e.eval(new StringReader("r"))), "eval(Reader) delegates");
        check("EVAL:x".equals(e.eval("x", new javax.script.SimpleBindings())), "eval(String,Bindings) delegates");

        check(e.createBindings() != null, "createBindings non-null");

        javax.script.ScriptContext c2 = new javax.script.SimpleScriptContext();
        e.setContext(c2);
        check(e.getContext() == c2, "set/get context");

        boolean npeCtx = false;
        try { e.setContext(null); } catch (NullPointerException ex) { npeCtx = true; }
        check(npeCtx, "setContext(null) throws NPE");

        MinEngine e2 = new MinEngine(new javax.script.SimpleBindings());
        check(e2.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) != null, "Bindings ctor sets engine scope");

        boolean npeCtor = false;
        try { new MinEngine(null); } catch (NullPointerException ex) { npeCtor = true; }
        check(npeCtor, "Bindings ctor null throws NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
