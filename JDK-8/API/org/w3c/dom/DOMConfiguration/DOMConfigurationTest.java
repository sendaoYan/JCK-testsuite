/*
 * @test
 * @summary org.w3c.dom.DOMConfiguration conformance: reflection shape checks.
 * @run main DOMConfigurationTest
 */

import java.lang.reflect.*;

public class DOMConfigurationTest {
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
        Class<?> c = Class.forName("org.w3c.dom.DOMConfiguration");
        check(c != null, "org.w3c.dom.DOMConfiguration loads");
        check(c.getName().equals("org.w3c.dom.DOMConfiguration"), "binary name is org.w3c.dom.DOMConfiguration");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.DOMConfiguration is public");
        check(c.isInterface(), "org.w3c.dom.DOMConfiguration is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "setParameter"), "declares setParameter");
        check(declares(c, "getParameter"), "declares getParameter");
        check(declares(c, "canSetParameter"), "declares canSetParameter");
        check(declares(c, "getParameterNames"), "declares getParameterNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
