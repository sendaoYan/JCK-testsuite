/*
 * @test
 * @summary org.w3c.dom.DOMError conformance: reflection shape checks.
 * @run main DOMErrorTest
 */

import java.lang.reflect.*;

public class DOMErrorTest {
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
        Class<?> c = Class.forName("org.w3c.dom.DOMError");
        check(c != null, "org.w3c.dom.DOMError loads");
        check(c.getName().equals("org.w3c.dom.DOMError"), "binary name is org.w3c.dom.DOMError");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.DOMError is public");
        check(c.isInterface(), "org.w3c.dom.DOMError is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getSeverity"), "declares getSeverity");
        check(declares(c, "getMessage"), "declares getMessage");
        check(declares(c, "getType"), "declares getType");
        check(declares(c, "getRelatedException"), "declares getRelatedException");
        check(declares(c, "getRelatedData"), "declares getRelatedData");
        check(declares(c, "getLocation"), "declares getLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
