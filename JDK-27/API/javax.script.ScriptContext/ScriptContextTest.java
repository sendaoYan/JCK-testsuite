/*
 * @test
 * @summary javax.script.ScriptContext conformance: white-box structure + black-box behavior.
 * @run main ScriptContextTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

public class ScriptContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.ScriptContext");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "setBindings"), "declares setBindings");
        check(hasMethod(c, "getBindings"), "declares getBindings");
        check(hasMethod(c, "setAttribute"), "declares setAttribute");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        check(hasMethod(c, "removeAttribute"), "declares removeAttribute");
    }

    static void testBlackBox() throws Exception {
        check(javax.script.ScriptContext.ENGINE_SCOPE == 100, "ENGINE_SCOPE == 100");
        check(javax.script.ScriptContext.GLOBAL_SCOPE == 200, "GLOBAL_SCOPE == 200");

        javax.script.ScriptContext sc = new javax.script.SimpleScriptContext();
        List<Integer> scopes = sc.getScopes();
        check(scopes.size() == 2, "two scopes");
        check(scopes.get(0) == javax.script.ScriptContext.ENGINE_SCOPE, "engine scope first");
        check(scopes.get(1) == javax.script.ScriptContext.GLOBAL_SCOPE, "global scope second");

        check(sc.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) != null, "default engine bindings non-null");
        check(sc.getBindings(javax.script.ScriptContext.GLOBAL_SCOPE) == null, "default global bindings null");

        sc.setAttribute("n", "val", javax.script.ScriptContext.ENGINE_SCOPE);
        check("val".equals(sc.getAttribute("n")), "engine getAttribute(name)");
        check("val".equals(sc.getAttribute("n", javax.script.ScriptContext.ENGINE_SCOPE)), "engine getAttribute(name,scope)");
        check(sc.getAttributesScope("n") == javax.script.ScriptContext.ENGINE_SCOPE, "getAttributesScope engine");
        check("val".equals(sc.removeAttribute("n", javax.script.ScriptContext.ENGINE_SCOPE)), "removeAttribute");
        check(sc.getAttribute("n") == null, "removed attribute null");
        check(sc.getAttributesScope("n") == -1, "absent scope -1");

        // Global scope setAttribute is a no-op until a global Bindings is set.
        sc.setAttribute("g", "gv", javax.script.ScriptContext.GLOBAL_SCOPE);
        check(sc.getAttribute("g") == null, "global setAttribute no-op without global bindings");
        sc.setBindings(new javax.script.SimpleBindings(), javax.script.ScriptContext.GLOBAL_SCOPE);
        sc.setAttribute("g", "gv", javax.script.ScriptContext.GLOBAL_SCOPE);
        check("gv".equals(sc.getAttribute("g", javax.script.ScriptContext.GLOBAL_SCOPE)), "global attribute after bindings");
        check(sc.getAttributesScope("g") == javax.script.ScriptContext.GLOBAL_SCOPE, "getAttributesScope global");

        // Engine scope shadows global scope.
        sc.setAttribute("shared", "engine", javax.script.ScriptContext.ENGINE_SCOPE);
        sc.setAttribute("shared", "global", javax.script.ScriptContext.GLOBAL_SCOPE);
        check("engine".equals(sc.getAttribute("shared")), "engine scope shadows global");

        // Reader / writer round trips.
        StringWriter sw = new StringWriter();
        sc.setWriter(sw);
        check(sc.getWriter() == sw, "set/get writer");
        StringWriter ew = new StringWriter();
        sc.setErrorWriter(ew);
        check(sc.getErrorWriter() == ew, "set/get errorWriter");
        StringReader sr = new StringReader("x");
        sc.setReader(sr);
        check(sc.getReader() == sr, "set/get reader");

        // Invalid scope.
        boolean iae = false;
        try { sc.getBindings(999); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "getBindings invalid scope IAE");

        // Null / empty names.
        boolean npe = false;
        try { sc.getAttribute(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "getAttribute(null) NPE");
        boolean iaeName = false;
        try { sc.getAttribute(""); } catch (IllegalArgumentException e) { iaeName = true; }
        check(iaeName, "getAttribute(\"\") IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
