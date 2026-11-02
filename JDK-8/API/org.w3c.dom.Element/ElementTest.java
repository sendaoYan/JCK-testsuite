/*
 * @test
 * @summary org.w3c.dom.Element conformance: reflection shape checks.
 * @run main ElementTest
 */

import java.lang.reflect.*;

public class ElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.Element");
        check(c != null, "org.w3c.dom.Element loads");
        check(c.getName().equals("org.w3c.dom.Element"), "binary name is org.w3c.dom.Element");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.Element is public");
        check(c.isInterface(), "org.w3c.dom.Element is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        check(declares(c, "getTagName"), "declares getTagName");
        check(declares(c, "getAttribute"), "declares getAttribute");
        check(declares(c, "setAttribute"), "declares setAttribute");
        check(declares(c, "removeAttribute"), "declares removeAttribute");
        check(declares(c, "getAttributeNode"), "declares getAttributeNode");
        check(declares(c, "setAttributeNode"), "declares setAttributeNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
