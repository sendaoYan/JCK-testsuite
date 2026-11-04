/*
 * @test
 * @summary org.w3c.dom.css.Counter conformance: reflection shape checks.
 * @run main CounterTest
 */

import java.lang.reflect.*;

public class CounterTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.Counter");
        check(c != null, "org.w3c.dom.css.Counter loads");
        check(c.getName().equals("org.w3c.dom.css.Counter"), "binary name is org.w3c.dom.css.Counter");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.Counter is public");
        check(c.isInterface(), "org.w3c.dom.css.Counter is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getIdentifier"), "declares getIdentifier");
        check(declares(c, "getListStyle"), "declares getListStyle");
        check(declares(c, "getSeparator"), "declares getSeparator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
