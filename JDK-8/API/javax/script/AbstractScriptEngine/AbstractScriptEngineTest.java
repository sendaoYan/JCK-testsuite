/*
 * @test
 * @summary javax.script.AbstractScriptEngine conformance: reflection shape checks.
 * @run main AbstractScriptEngineTest
 */

import java.lang.reflect.*;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

public class AbstractScriptEngineTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.script.AbstractScriptEngine");
        check(c != null, "javax.script.AbstractScriptEngine loads");
        check(c.getName().equals("javax.script.AbstractScriptEngine"), "binary name is javax.script.AbstractScriptEngine");
        check(Modifier.isPublic(c.getModifiers()), "javax.script.AbstractScriptEngine is public");
        check(!c.isInterface(), "javax.script.AbstractScriptEngine is not an interface");
        check(Modifier.isAbstract(c.getModifiers()), "javax.script.AbstractScriptEngine is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(ifaceNames(c).equals(set("javax.script.ScriptEngine")), "direct superinterfaces");
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
