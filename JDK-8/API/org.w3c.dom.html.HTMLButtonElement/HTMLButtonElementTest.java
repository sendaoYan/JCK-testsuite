/*
 * @test
 * @summary org.w3c.dom.html.HTMLButtonElement conformance: reflection shape checks.
 * @run main HTMLButtonElementTest
 */

import java.lang.reflect.*;

public class HTMLButtonElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLButtonElement");
        check(c != null, "org.w3c.dom.html.HTMLButtonElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLButtonElement"), "binary name is org.w3c.dom.html.HTMLButtonElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLButtonElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLButtonElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getForm"), "declares getForm");
        check(declares(c, "getAccessKey"), "declares getAccessKey");
        check(declares(c, "setAccessKey"), "declares setAccessKey");
        check(declares(c, "getDisabled"), "declares getDisabled");
        check(declares(c, "setDisabled"), "declares setDisabled");
        check(declares(c, "getName"), "declares getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
