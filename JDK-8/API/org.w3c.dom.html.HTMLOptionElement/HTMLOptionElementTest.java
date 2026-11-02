/*
 * @test
 * @summary org.w3c.dom.html.HTMLOptionElement conformance: reflection shape checks.
 * @run main HTMLOptionElementTest
 */

import java.lang.reflect.*;

public class HTMLOptionElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLOptionElement");
        check(c != null, "org.w3c.dom.html.HTMLOptionElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLOptionElement"), "binary name is org.w3c.dom.html.HTMLOptionElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLOptionElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLOptionElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getForm"), "declares getForm");
        check(declares(c, "getDefaultSelected"), "declares getDefaultSelected");
        check(declares(c, "setDefaultSelected"), "declares setDefaultSelected");
        check(declares(c, "getText"), "declares getText");
        check(declares(c, "getIndex"), "declares getIndex");
        check(declares(c, "getDisabled"), "declares getDisabled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
