/*
 * @test
 * @summary javax.script.ScriptContext conformance: reflection shape checks.
 * @run main ScriptContextTest
 */

import java.lang.reflect.*;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

public class ScriptContextTest {
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
        Class<?> c = Class.forName("javax.script.ScriptContext");
        check(c != null, "javax.script.ScriptContext loads");
        check(c.getName().equals("javax.script.ScriptContext"), "binary name is javax.script.ScriptContext");
        check(Modifier.isPublic(c.getModifiers()), "javax.script.ScriptContext is public");
        check(c.isInterface(), "javax.script.ScriptContext is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "setBindings"), "declares setBindings");
        check(declares(c, "getBindings"), "declares getBindings");
        check(declares(c, "setAttribute"), "declares setAttribute");
        check(declares(c, "getAttribute"), "declares getAttribute");
        check(declares(c, "removeAttribute"), "declares removeAttribute");
        check(declares(c, "getAttributesScope"), "declares getAttributesScope");
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dScConstants();
        dScSurface();
        dScAttributeSemantics();
        System.out.println("deep: ScriptContext contracts OK");
    }

    static void dScConstants() {
        check(ScriptContext.ENGINE_SCOPE == 100, "ENGINE_SCOPE == 100");
        check(ScriptContext.GLOBAL_SCOPE == 200, "GLOBAL_SCOPE == 200");
        check(ScriptContext.ENGINE_SCOPE < ScriptContext.GLOBAL_SCOPE,
                "engine scope searched before global");
    }

    static void dScSurface() throws Exception {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : ScriptContext.class.getMethods()) names.add(m.getName());
        for (String want : new String[] {"setAttribute", "getAttribute", "removeAttribute",
                "getAttributesScope",
                "getBindings", "setBindings", "getWriter", "getErrorWriter", "getReader",
                "setWriter", "setErrorWriter", "setReader", "getScopes"})
            check(names.contains(want), "declares " + want);
        check(ScriptContext.class.isInterface(), "is interface");
    }

    static void dScAttributeSemantics() {
        SimpleScriptContext sc = new SimpleScriptContext();
        sc.setAttribute("a", 1, ScriptContext.ENGINE_SCOPE);
        check(Integer.valueOf(1).equals(sc.getAttribute("a")), "engine scope visible");
        check(sc.getAttributesScope("a") == ScriptContext.ENGINE_SCOPE,
                "found in engine scope");
        // engine scope wins over global
        Bindings global = new SimpleBindings();
        global.put("a", 99);
        global.put("g", "G");
        sc.setBindings(global, ScriptContext.GLOBAL_SCOPE);
        check(Integer.valueOf(1).equals(sc.getAttribute("a")), "engine scope precedence");
        check("G".equals(sc.getAttribute("g")), "global scope fallback");
        check(sc.getAttributesScope("g") == ScriptContext.GLOBAL_SCOPE, "global discovery");
        check(sc.getAttribute("missing") == null, "absent -> null");
        check(sc.getAttributesScope("missing") == -1, "absent scope -> -1");
        // removal returns the previous value
        Object prev = sc.removeAttribute("a", ScriptContext.ENGINE_SCOPE);
        check(Integer.valueOf(1).equals(prev), "removeAttribute returns old value");
        check(Integer.valueOf(99).equals(sc.getAttribute("a")), "global now visible");
        // bad scope
        boolean iae1 = false, iae2 = false, iae3 = false;
        try { sc.getAttribute("a", 999); } catch (IllegalArgumentException e) { iae1 = true; }
        try { sc.setAttribute("a", 1, 999); } catch (IllegalArgumentException e) { iae2 = true; }
        try { sc.removeAttribute("a", 999); } catch (IllegalArgumentException e) { iae3 = true; }
        check(iae1 && iae2 && iae3, "invalid scope -> IAE on all attribute ops");
        // scopes list
        java.util.List<Integer> scopes = sc.getScopes();
        check(scopes.contains(ScriptContext.ENGINE_SCOPE)
                && scopes.contains(ScriptContext.GLOBAL_SCOPE), "scopes list content");
    }

}
