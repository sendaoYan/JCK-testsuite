/*
 * @test
 * @summary org.w3c.dom.html.HTMLLinkElement conformance: reflection shape checks.
 * @run main HTMLLinkElementTest
 */

import java.lang.reflect.*;

public class HTMLLinkElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLLinkElement");
        check(c != null, "org.w3c.dom.html.HTMLLinkElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLLinkElement"), "binary name is org.w3c.dom.html.HTMLLinkElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLLinkElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLLinkElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getDisabled"), "declares getDisabled");
        check(declares(c, "setDisabled"), "declares setDisabled");
        check(declares(c, "getCharset"), "declares getCharset");
        check(declares(c, "setCharset"), "declares setCharset");
        check(declares(c, "getHref"), "declares getHref");
        check(declares(c, "setHref"), "declares setHref");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
