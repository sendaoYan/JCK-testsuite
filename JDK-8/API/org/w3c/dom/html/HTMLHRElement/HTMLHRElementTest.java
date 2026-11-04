/*
 * @test
 * @summary org.w3c.dom.html.HTMLHRElement conformance: reflection shape checks.
 * @run main HTMLHRElementTest
 */

import java.lang.reflect.*;

public class HTMLHRElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLHRElement");
        check(c != null, "org.w3c.dom.html.HTMLHRElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLHRElement"), "binary name is org.w3c.dom.html.HTMLHRElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLHRElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLHRElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getAlign"), "declares getAlign");
        check(declares(c, "setAlign"), "declares setAlign");
        check(declares(c, "getNoShade"), "declares getNoShade");
        check(declares(c, "setNoShade"), "declares setNoShade");
        check(declares(c, "getSize"), "declares getSize");
        check(declares(c, "setSize"), "declares setSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
