/*
 * @test
 * @summary org.w3c.dom.html.HTMLOptGroupElement conformance: reflection shape checks.
 * @run main HTMLOptGroupElementTest
 */

import java.lang.reflect.*;

public class HTMLOptGroupElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLOptGroupElement");
        check(c != null, "org.w3c.dom.html.HTMLOptGroupElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLOptGroupElement"), "binary name is org.w3c.dom.html.HTMLOptGroupElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLOptGroupElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLOptGroupElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getDisabled"), "declares getDisabled");
        check(declares(c, "setDisabled"), "declares setDisabled");
        check(declares(c, "getLabel"), "declares getLabel");
        check(declares(c, "setLabel"), "declares setLabel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
