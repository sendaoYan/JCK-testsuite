/*
 * @test
 * @summary javax.script.CompiledScript conformance: white-box structure + black-box behavior.
 * @run main CompiledScriptTest
 */

import java.lang.reflect.*;
import java.io.*;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import javax.script.ScriptContext;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (self-made compiled script) ---------------

    static void testDeepContracts() throws Exception {
        dCs27Identity();
        dCs27BaseSemantics();
        System.out.println("deep: CompiledScript contracts OK (self-made)");
    }

    static class CsRecordingEngine extends AbstractScriptEngine {
        final java.util.List<ScriptContext> seen = new java.util.ArrayList<>();
        public Object eval(String s, ScriptContext ctx) throws ScriptException {
            seen.add(ctx);
            if (s.equals("ident")) return ctx.getAttribute("probe");
            return "OK:" + s;
        }
        public Object eval(java.io.Reader r, ScriptContext ctx) throws ScriptException {
            return eval("read", ctx);
        }
        public Bindings createBindings() { return new SimpleBindings(); }
        public ScriptEngineFactory getFactory() { return null; }
    }

    static void dCs27Identity() throws Exception {
        // eval() and eval(null) hand the engine's own context object to the script
        final CsRecordingEngine eng = new CsRecordingEngine();
        CompiledScript cs = new CompiledScript() {
            public Object eval(ScriptContext ctx) { eng.seen.add(ctx); return "R"; }
            public ScriptEngine getEngine() { return eng; }
        };
        check("R".equals(cs.eval()), "eval() delegates");
        check(eng.seen.get(eng.seen.size() - 1) == eng.getContext(),
                "eval() passes the engine's default context object");
        check("R".equals(cs.eval((Bindings) null)), "eval(null Bindings)");
        check(eng.seen.get(eng.seen.size() - 1) == eng.getContext(),
                "eval(null) passes the engine's default context object");
        // eval(Bindings) wraps the bindings in a temporary context
        SimpleBindings b = new SimpleBindings();
        b.put("probe", "from-b");
        check("R".equals(cs.eval(b)), "eval(Bindings) delegates");
        ScriptContext c3 = eng.seen.get(eng.seen.size() - 1);
        check(c3 != eng.getContext(), "eval(Bindings) builds a temporary context");
        check(c3.getBindings(ScriptContext.ENGINE_SCOPE) == b,
                "bindings become the ENGINE_SCOPE");
        check(c3.getBindings(ScriptContext.GLOBAL_SCOPE)
                == eng.getContext().getBindings(ScriptContext.GLOBAL_SCOPE),
                "GLOBAL_SCOPE is carried over");
        check(c3.getWriter() == eng.getContext().getWriter()
                && c3.getErrorWriter() == eng.getContext().getErrorWriter()
                && c3.getReader() == eng.getContext().getReader(),
                "reader/writers are carried over");
    }

    static void dCs27BaseSemantics() throws Exception {
        final CsRecordingEngine eng = new CsRecordingEngine();
        CompiledScript script = new CompiledScript() {
            public Object eval(ScriptContext ctx) throws ScriptException {
                return eng.eval("ident", ctx);
            }
            public ScriptEngine getEngine() { return eng; }
        };
        eng.put("probe", "engine-val");
        check("engine-val".equals(script.eval()), "script sees engine scope through eval()");
        SimpleBindings b = new SimpleBindings();
        b.put("probe", "bindings-val");
        check("bindings-val".equals(script.eval(b)), "script sees given bindings first");
        check("engine-val".equals(script.eval()), "engine scope restored afterwards");
        check(eng.get("probe").equals("engine-val"), "engine scope untouched");
        // the temporary context of eval(Bindings) is a live view for the script run
        ScriptContext explicit = new SimpleScriptContext();
        explicit.setAttribute("probe", "ctx-val", ScriptContext.ENGINE_SCOPE);
        check("ctx-val".equals(script.eval(explicit)), "eval(ScriptContext) uses given context");
    }

}
