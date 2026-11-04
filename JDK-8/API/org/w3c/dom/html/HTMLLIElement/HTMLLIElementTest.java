/*
 * @test
 * @summary org.w3c.dom.html.HTMLLIElement conformance: reflection shape checks.
 * @run main HTMLLIElementTest
 */

import java.lang.reflect.*;

public class HTMLLIElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLLIElement");
        check(c != null, "org.w3c.dom.html.HTMLLIElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLLIElement"), "binary name is org.w3c.dom.html.HTMLLIElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLLIElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLLIElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getType"), "declares getType");
        check(declares(c, "setType"), "declares setType");
        check(declares(c, "getValue"), "declares getValue");
        check(declares(c, "setValue"), "declares setValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
