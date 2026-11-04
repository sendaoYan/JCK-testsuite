/*
 * @test
 * @summary org.w3c.dom.html.HTMLAreaElement conformance: reflection shape checks.
 * @run main HTMLAreaElementTest
 */

import java.lang.reflect.*;

public class HTMLAreaElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLAreaElement");
        check(c != null, "org.w3c.dom.html.HTMLAreaElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLAreaElement"), "binary name is org.w3c.dom.html.HTMLAreaElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLAreaElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLAreaElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getAccessKey"), "declares getAccessKey");
        check(declares(c, "setAccessKey"), "declares setAccessKey");
        check(declares(c, "getAlt"), "declares getAlt");
        check(declares(c, "setAlt"), "declares setAlt");
        check(declares(c, "getCoords"), "declares getCoords");
        check(declares(c, "setCoords"), "declares setCoords");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
