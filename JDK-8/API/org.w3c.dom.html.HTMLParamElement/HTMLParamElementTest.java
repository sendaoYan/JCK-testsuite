/*
 * @test
 * @summary org.w3c.dom.html.HTMLParamElement conformance: reflection shape checks.
 * @run main HTMLParamElementTest
 */

import java.lang.reflect.*;

public class HTMLParamElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLParamElement");
        check(c != null, "org.w3c.dom.html.HTMLParamElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLParamElement"), "binary name is org.w3c.dom.html.HTMLParamElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLParamElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLParamElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getName"), "declares getName");
        check(declares(c, "setName"), "declares setName");
        check(declares(c, "getType"), "declares getType");
        check(declares(c, "setType"), "declares setType");
        check(declares(c, "getValue"), "declares getValue");
        check(declares(c, "setValue"), "declares setValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
