/*
 * @test
 * @summary javax.script.SimpleScriptContext conformance: simple script context.
 * @run main SimpleScriptContextTest
 */


import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import javax.script.ScriptContext;
public class SimpleScriptContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.script.SimpleScriptContext c = new javax.script.SimpleScriptContext();
        check(c.getScopes() != null, "getScopes");
        check(c.getScopes().size() == 2, "two scopes");
        check(c.getWriter() != null, "writer");
        check(c.getErrorWriter() != null, "errorWriter");
        check(c.getReader() != null, "reader");
        java.io.StringWriter sw = new java.io.StringWriter();
        c.setWriter(sw);
        check(c.getWriter() == sw, "setWriter");
        java.io.StringReader sr = new java.io.StringReader("x");
        c.setReader(sr);
        check(c.getReader() == sr, "setReader");
        c.setAttribute("n", "val", javax.script.ScriptContext.ENGINE_SCOPE);
        check("val".equals(c.getAttribute("n")), "engine getAttribute");
        check(c.getAttributesScope("n") == javax.script.ScriptContext.ENGINE_SCOPE, "getAttributesScope");
        check("val".equals(c.removeAttribute("n", javax.script.ScriptContext.ENGINE_SCOPE)), "removeAttribute");
        c.setBindings(new javax.script.SimpleBindings(), javax.script.ScriptContext.GLOBAL_SCOPE);
        c.setAttribute("g", "gv", javax.script.ScriptContext.GLOBAL_SCOPE);
        check("gv".equals(c.getAttribute("g", javax.script.ScriptContext.GLOBAL_SCOPE)), "global attribute");
        check(c.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) != null, "engine bindings");
        c.setBindings(new javax.script.SimpleBindings(), javax.script.ScriptContext.ENGINE_SCOPE);
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dSscDefaults();
        dSscStreams();
        dSscBindings();
        dSscAttributeEdges();
        System.out.println("deep: SimpleScriptContext contracts OK");
    }

    static void dSscDefaults() {
        SimpleScriptContext sc = new SimpleScriptContext();
        check(sc.getBindings(ScriptContext.ENGINE_SCOPE) != null, "default engine bindings");
        check(sc.getBindings(ScriptContext.ENGINE_SCOPE) instanceof SimpleBindings,
                "default engine bindings are SimpleBindings");
        check(sc.getBindings(ScriptContext.GLOBAL_SCOPE) == null, "default global scope null");
        check(sc.getScopes().size() == 2, "two scopes");
        check(sc.getWriter() instanceof java.io.PrintWriter, "default writer PrintWriter");
        check(sc.getErrorWriter() instanceof java.io.PrintWriter, "default error writer PrintWriter");
        check(sc.getReader() instanceof java.io.Reader, "default reader present");
    }

    static void dSscStreams() {
        SimpleScriptContext sc = new SimpleScriptContext();
        java.io.StringWriter out = new java.io.StringWriter();
        java.io.StringWriter err = new java.io.StringWriter();
        java.io.StringReader in = new java.io.StringReader("input");
        sc.setWriter(out);
        sc.setErrorWriter(err);
        sc.setReader(in);
        check(sc.getWriter() == out && sc.getErrorWriter() == err && sc.getReader() == in,
                "writers/reader replaceable");
        check(sc.getWriter() != sc.getErrorWriter(), "distinct channels");
    }

    static void dSscBindings() {
        SimpleScriptContext sc = new SimpleScriptContext();
        Bindings engine = new SimpleBindings();
        Bindings global = new SimpleBindings();
        sc.setBindings(engine, ScriptContext.ENGINE_SCOPE);
        check(sc.getBindings(ScriptContext.ENGINE_SCOPE) == engine, "engine bindings replaced");
        sc.setBindings(global, ScriptContext.GLOBAL_SCOPE);
        check(sc.getBindings(ScriptContext.GLOBAL_SCOPE) == global, "global bindings replaced");
        // null is allowed for global (no global scope) but not for engine
        sc.setBindings(null, ScriptContext.GLOBAL_SCOPE);
        check(sc.getBindings(ScriptContext.GLOBAL_SCOPE) == null, "null global clears scope");
        boolean npe = false;
        try { sc.setBindings(null, ScriptContext.ENGINE_SCOPE); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "setBindings(null, ENGINE) -> NPE");
        boolean iae = false;
        try { sc.setBindings(engine, 999); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "setBindings bad scope -> IAE");
        // attributes route into the current bindings
        sc.setBindings(engine, ScriptContext.ENGINE_SCOPE);
        sc.setAttribute("k", "v", ScriptContext.ENGINE_SCOPE);
        check("v".equals(engine.get("k")), "setAttribute writes into bindings");
        check("v".equals(sc.getAttribute("k", ScriptContext.ENGINE_SCOPE)), "read back");
    }

    static void dSscAttributeEdges() {
        SimpleScriptContext sc = new SimpleScriptContext();
        // writing into a scope whose bindings are null is a silent no-op
        sc.setAttribute("g", "G", ScriptContext.GLOBAL_SCOPE);
        check(sc.getAttribute("g") == null, "write to null global bindings is a no-op");
        sc.setBindings(new SimpleBindings(), ScriptContext.GLOBAL_SCOPE);
        sc.setAttribute("g", "G", ScriptContext.GLOBAL_SCOPE);
        check("G".equals(sc.getAttribute("g", ScriptContext.GLOBAL_SCOPE)), "write now visible");
        // null/empty names
        boolean npe = false, iae = false;
        try { sc.getAttribute(null); } catch (NullPointerException e) { npe = true; }
        try { sc.getAttribute(""); } catch (IllegalArgumentException e) { iae = true; }
        check(npe, "getAttribute(null) -> NPE");
        check(iae, "getAttribute(\"\") -> IAE");
        // removal from a missing scope
        check(sc.removeAttribute("ghost", ScriptContext.GLOBAL_SCOPE) == null,
                "remove absent -> null");
    }

}
