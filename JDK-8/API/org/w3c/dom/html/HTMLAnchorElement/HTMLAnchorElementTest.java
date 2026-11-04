/*
 * @test
 * @summary org.w3c.dom.html.HTMLAnchorElement conformance: reflection shape checks.
 * @run main HTMLAnchorElementTest
 */

import java.lang.reflect.*;

public class HTMLAnchorElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLAnchorElement");
        check(c != null, "org.w3c.dom.html.HTMLAnchorElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLAnchorElement"), "binary name is org.w3c.dom.html.HTMLAnchorElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLAnchorElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLAnchorElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getAccessKey"), "declares getAccessKey");
        check(declares(c, "setAccessKey"), "declares setAccessKey");
        check(declares(c, "getCharset"), "declares getCharset");
        check(declares(c, "setCharset"), "declares setCharset");
        check(declares(c, "getCoords"), "declares getCoords");
        check(declares(c, "setCoords"), "declares setCoords");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
