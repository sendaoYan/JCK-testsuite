/*
 * @test
 * @summary javax.script.ScriptContext conformance: reflection shape checks.
 * @run main ScriptContextTest
 */

import java.lang.reflect.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
