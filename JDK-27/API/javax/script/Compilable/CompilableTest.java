/*
 * @test
 * @summary javax.script.Compilable conformance: white-box structure + black-box behavior.
 * @run main CompilableTest
 */

import java.lang.reflect.*;
import java.io.*;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class CompilableTest {
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
        public Object eval(javax.script.ScriptContext ctx) { return "RESULT"; }
        public javax.script.ScriptEngine getEngine() { return eng; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.Compilable");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "compile"), "declares compile");
    }

    static void testBlackBox() throws Exception {
        javax.script.Compilable compilable = new javax.script.Compilable() {
            public javax.script.CompiledScript compile(String script) { return new MinCompiled(); }
            public javax.script.CompiledScript compile(Reader script) { return new MinCompiled(); }
        };
        javax.script.CompiledScript cs1 = compilable.compile("var x = 1;");
        javax.script.CompiledScript cs2 = compilable.compile(new StringReader("var x = 2;"));
        check(cs1 != null, "compile(String) non-null");
        check(cs2 != null, "compile(Reader) non-null");
        check(cs1.getEngine() != null, "compiled script engine non-null");
        check("RESULT".equals(cs1.eval()), "compiled script evals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (self-made compilable engine) -------------

    static void testDeepContracts() throws Exception {
        check(new ScriptEngineManager().getEngineFactories().isEmpty(),
                "no platform engine: Compilable tested with self-made engine");
        DeepCompEngine e = new DeepCompEngine();
        check(e instanceof Compilable, "engine implements Compilable");
        Compilable c = e;
        dComp27(c, e);
        System.out.println("deep: Compilable contracts OK (self-made)");
    }

    static class DeepCompEngine extends AbstractScriptEngine implements Compilable {
        int compiles;
        int execs;
        public CompiledScript compile(String script) throws ScriptException {
            if (script == null) throw new NullPointerException("null script");
            if (script.indexOf("{") >= 0) throw new ScriptException("deep syntax error", "c.js", 1, 2);
            compiles++;
            final String src = script;
            return new CompiledScript() {
                public Object eval(ScriptContext ctx) throws ScriptException {
                    execs++;
                    return DeepCompEngine.this.eval(src, ctx);
                }
                public ScriptEngine getEngine() { return DeepCompEngine.this; }
            };
        }
        public CompiledScript compile(java.io.Reader reader) throws ScriptException {
            StringBuilder sb = new StringBuilder();
            try { int ch; while ((ch = reader.read()) >= 0) sb.append((char) ch); }
            catch (java.io.IOException x) { throw new AssertionError(x); }
            return compile(sb.toString());
        }
        public Object eval(String s, ScriptContext ctx) throws ScriptException {
            if (s.equals("ident")) return ctx.getAttribute("probe");
            return "OK:" + s;
        }
        public Object eval(java.io.Reader r, ScriptContext ctx) throws ScriptException {
            StringBuilder sb = new StringBuilder();
            try { int ch; while ((ch = r.read()) >= 0) sb.append((char) ch); }
            catch (java.io.IOException x) { throw new AssertionError(x); }
            return eval(sb.toString(), ctx);
        }
        public Bindings createBindings() { return new SimpleBindings(); }
        public ScriptEngineFactory getFactory() { return null; }
    }

    static void dComp27(Compilable c, DeepCompEngine e) throws Exception {
        CompiledScript cs = c.compile("cmd");
        check(e.compiles == 1, "one compilation");
        check("OK:cmd".equals(cs.eval()), "compiled eval");
        check("OK:cmd".equals(cs.eval()), "second eval reuses the compilation");
        check("OK:cmd".equals(cs.eval(e.getContext())), "eval(ScriptContext)");
        check(e.compiles == 1 && e.execs == 3, "three executions, still one compilation");
        check(cs.getEngine() == e, "getEngine returns the compiling engine");
        // the compiled artifact observes current bindings at eval time
        e.put("probe", "first");
        CompiledScript id = c.compile("ident");
        e.put("probe", "second");
        check("second".equals(id.eval()), "bindings read at eval, not compile, time");
        // Reader overload funnels through compile(String)
        CompiledScript r = c.compile(new java.io.StringReader("rr"));
        check("OK:rr".equals(r.eval()), "compile(Reader) works");
        // negative contract of this engine
        boolean se = false;
        try { c.compile("{bad"); }
        catch (ScriptException x) {
            se = true;
            check("c.js".equals(x.getFileName()), "compile error file");
            check(x.getLineNumber() == 1 && x.getColumnNumber() == 2, "compile error position");
        }
        check(se, "syntax error reported at compile time");
        boolean npe = false;
        try { c.compile((String) null); }
        catch (NullPointerException x) { npe = true; }
        check(npe, "compile(null) -> NPE (engine contract)");
    }

}
