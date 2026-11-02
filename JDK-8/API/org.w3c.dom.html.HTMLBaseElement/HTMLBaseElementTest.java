/*
 * @test
 * @summary org.w3c.dom.html.HTMLBaseElement conformance: reflection shape checks.
 * @run main HTMLBaseElementTest
 */

import java.lang.reflect.*;

public class HTMLBaseElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLBaseElement");
        check(c != null, "org.w3c.dom.html.HTMLBaseElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLBaseElement"), "binary name is org.w3c.dom.html.HTMLBaseElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLBaseElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLBaseElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getHref"), "declares getHref");
        check(declares(c, "setHref"), "declares setHref");
        check(declares(c, "getTarget"), "declares getTarget");
        check(declares(c, "setTarget"), "declares setTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
