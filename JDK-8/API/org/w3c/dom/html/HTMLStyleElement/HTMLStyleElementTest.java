/*
 * @test
 * @summary org.w3c.dom.html.HTMLStyleElement conformance: reflection shape checks.
 * @run main HTMLStyleElementTest
 */

import java.lang.reflect.*;

public class HTMLStyleElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLStyleElement");
        check(c != null, "org.w3c.dom.html.HTMLStyleElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLStyleElement"), "binary name is org.w3c.dom.html.HTMLStyleElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLStyleElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLStyleElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getDisabled"), "declares getDisabled");
        check(declares(c, "setDisabled"), "declares setDisabled");
        check(declares(c, "getMedia"), "declares getMedia");
        check(declares(c, "setMedia"), "declares setMedia");
        check(declares(c, "getType"), "declares getType");
        check(declares(c, "setType"), "declares setType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
