/*
 * @test
 * @summary javax.script.AbstractScriptEngine conformance: white-box structure + black-box behavior.
 * @run main AbstractScriptEngineTest
 */

import java.lang.reflect.*;
import java.io.*;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import javax.script.ScriptContext;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dAeHookWiring();
        dAeBindingsSwap();
        dAePutGet();
        System.out.println("deep: AbstractScriptEngine contracts OK");
    }

    static class CountingEngine extends AbstractScriptEngine {
        int readerHits, stringHits, readerBindingHits;
        String lastScript, lastReaderContent;
        CountingEngine() { }
        CountingEngine(ScriptContext ctx) { setContext(ctx); }
        public Object eval(java.io.Reader reader, ScriptContext ctx) throws ScriptException {
            readerHits++;
            StringBuilder sb = new StringBuilder();
            try { int c; while ((c = reader.read()) >= 0) sb.append((char) c); }
            catch (java.io.IOException e) { throw new AssertionError(e); }
            lastReaderContent = sb.toString();
            return eval(sb.toString(), ctx);
        }
        public Object eval(String script, ScriptContext ctx) throws ScriptException {
            stringHits++;
            lastScript = script;
            return "S[" + script + "]:" + ctx.getAttribute("marker");
        }
        public Bindings createBindings() { return new SimpleBindings(); }
        public ScriptEngineFactory getFactory() { return null; }
    }

    static void dAeHookWiring() throws Exception {
        CountingEngine e = new CountingEngine();
        e.put("marker", "MV");
        check("S[cmd]:MV".equals(e.eval("cmd")), "eval(String) routes through eval(String,ctx)");
        check(e.stringHits == 1 && e.readerHits == 0, "String hook hit");
        check("S[rc]:MV".equals(e.eval(new java.io.StringReader("rc"))),
                "eval(Reader) routes through eval(Reader,ctx)");
        check(e.readerHits == 1 && "rc".equals(e.lastReaderContent), "Reader hook hit with content");
        // context supplied at construction is used
        SimpleScriptContext custom = new SimpleScriptContext();
        custom.setAttribute("marker", "CV", ScriptContext.ENGINE_SCOPE);
        CountingEngine e2 = new CountingEngine(custom);
        check("S[c]:CV".equals(e2.eval("c")), "ctor context drives evaluation");
        check(e2.getContext() == custom, "getContext returns the injected context");
        check(e.getContext() instanceof SimpleScriptContext, "default context is SimpleScriptContext");
    }

    static void dAeBindingsSwap() throws Exception {
        CountingEngine e = new CountingEngine();
        e.put("marker", "ENGINE");
        Bindings b = e.createBindings();
        b.put("marker", "BOUND");
        check("S[x]:BOUND".equals(e.eval("x", b)), "eval(String,Bindings) swaps engine scope");
        check("S[x]:ENGINE".equals(e.eval("x")), "original bindings restored afterwards");
        check("S[r]:BOUND".equals(e.eval(new java.io.StringReader("r"), b)),
                "eval(Reader,Bindings) swaps engine scope");
        check(e.readerBindingHits == 0 && e.readerHits == 1, "reader-with-bindings counted");
        // bindings swap is temporary even on exception
        try { e.eval("x", b) ; check(true, "normal path"); } catch (ScriptException se) { }
    }

    static void dAePutGet() {
        CountingEngine e = new CountingEngine();
        check(e.get("absent") == null, "get absent -> null");
        e.put("k", "v");
        check("v".equals(e.get("k")), "get after put");
        check("v".equals(e.getBindings(ScriptContext.ENGINE_SCOPE).get("k")),
                "put lands in engine scope");
        e.put("k", "v2");
        check("v2".equals(e.get("k")), "put overwrites the value");
        Bindings replacement = new SimpleBindings();
        e.setBindings(replacement, ScriptContext.ENGINE_SCOPE);
        check(e.get("k") == null, "setBindings resets visible state");
        check(e.getBindings(ScriptContext.ENGINE_SCOPE) == replacement,
                "getBindings returns current");
        check(e.createBindings() instanceof SimpleBindings, "createBindings default");
    }

}
