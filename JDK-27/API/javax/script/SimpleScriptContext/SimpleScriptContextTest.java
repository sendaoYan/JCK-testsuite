/*
 * @test
 * @summary javax.script.SimpleScriptContext conformance: white-box structure + black-box behavior.
 * @run main SimpleScriptContextTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.util.List;
import javax.script.ScriptContext;

public class SimpleScriptContextTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.SimpleScriptContext");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasInterface(c, "javax.script.ScriptContext"), "implements ScriptContext");
        check(hasMethod(c, "getBindings"), "declares getBindings");
        check(hasMethod(c, "setBindings"), "declares setBindings");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        check(hasMethod(c, "setAttribute"), "declares setAttribute");
    }

    static void testBlackBox() throws Exception {
        javax.script.SimpleScriptContext c = new javax.script.SimpleScriptContext();
        List<Integer> scopes = c.getScopes();
        check(scopes != null && scopes.size() == 2, "two scopes");
        check(scopes.get(0) == javax.script.ScriptContext.ENGINE_SCOPE, "engine scope first");
        check(scopes.get(1) == javax.script.ScriptContext.GLOBAL_SCOPE, "global scope second");

        check(c.getWriter() != null, "writer non-null");
        check(c.getErrorWriter() != null, "errorWriter non-null");
        check(c.getReader() != null, "reader non-null");

        StringWriter sw = new StringWriter();
        c.setWriter(sw);
        check(c.getWriter() == sw, "setWriter");
        StringWriter ew = new StringWriter();
        c.setErrorWriter(ew);
        check(c.getErrorWriter() == ew, "setErrorWriter");
        StringReader sr = new StringReader("x");
        c.setReader(sr);
        check(c.getReader() == sr, "setReader");

        check(c.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) != null, "default engine bindings");
        check(c.getBindings(javax.script.ScriptContext.GLOBAL_SCOPE) == null, "default global bindings null");

        c.setAttribute("n", "val", javax.script.ScriptContext.ENGINE_SCOPE);
        check("val".equals(c.getAttribute("n")), "engine getAttribute(name)");
        check(c.getAttributesScope("n") == javax.script.ScriptContext.ENGINE_SCOPE, "getAttributesScope");
        check("val".equals(c.removeAttribute("n", javax.script.ScriptContext.ENGINE_SCOPE)), "removeAttribute");

        // Global scope needs bindings before attributes stick.
        c.setBindings(new javax.script.SimpleBindings(), javax.script.ScriptContext.GLOBAL_SCOPE);
        c.setAttribute("g", "gv", javax.script.ScriptContext.GLOBAL_SCOPE);
        check("gv".equals(c.getAttribute("g", javax.script.ScriptContext.GLOBAL_SCOPE)), "global attribute");
        check(c.getAttributesScope("g") == javax.script.ScriptContext.GLOBAL_SCOPE, "global scope");

        // Engine shadows global.
        c.setAttribute("shared", "engine", javax.script.ScriptContext.ENGINE_SCOPE);
        c.setAttribute("shared", "global", javax.script.ScriptContext.GLOBAL_SCOPE);
        check("engine".equals(c.getAttribute("shared")), "engine shadows global");

        c.setBindings(new javax.script.SimpleBindings(), javax.script.ScriptContext.ENGINE_SCOPE);
        check(c.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) != null, "engine bindings after set");

        boolean npe = false;
        try { c.setBindings(null, javax.script.ScriptContext.ENGINE_SCOPE); } catch (NullPointerException e) { npe = true; }
        check(npe, "setBindings null engine scope NPE");
        boolean iae = false;
        try { c.getBindings(999); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "getBindings invalid scope IAE");
        boolean iaeAttr = false;
        try { c.setAttribute("", "v", javax.script.ScriptContext.ENGINE_SCOPE); } catch (IllegalArgumentException e) { iaeAttr = true; }
        check(iaeAttr, "setAttribute empty name IAE");
        boolean npeAttr = false;
        try { c.setAttribute(null, "v", javax.script.ScriptContext.ENGINE_SCOPE); } catch (NullPointerException e) { npeAttr = true; }
        check(npeAttr, "setAttribute null name NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
