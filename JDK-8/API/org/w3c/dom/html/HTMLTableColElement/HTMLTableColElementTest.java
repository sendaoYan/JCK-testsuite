/*
 * @test
 * @summary org.w3c.dom.html.HTMLTableColElement conformance: reflection shape checks.
 * @run main HTMLTableColElementTest
 */

import java.lang.reflect.*;

public class HTMLTableColElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLTableColElement");
        check(c != null, "org.w3c.dom.html.HTMLTableColElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLTableColElement"), "binary name is org.w3c.dom.html.HTMLTableColElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLTableColElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLTableColElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getAlign"), "declares getAlign");
        check(declares(c, "setAlign"), "declares setAlign");
        check(declares(c, "getCh"), "declares getCh");
        check(declares(c, "setCh"), "declares setCh");
        check(declares(c, "getChOff"), "declares getChOff");
        check(declares(c, "setChOff"), "declares setChOff");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
