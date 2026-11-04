/*
 * @test
 * @summary org.w3c.dom.html.HTMLInputElement conformance: reflection shape checks.
 * @run main HTMLInputElementTest
 */

import java.lang.reflect.*;

public class HTMLInputElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLInputElement");
        check(c != null, "org.w3c.dom.html.HTMLInputElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLInputElement"), "binary name is org.w3c.dom.html.HTMLInputElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLInputElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLInputElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getDefaultValue"), "declares getDefaultValue");
        check(declares(c, "setDefaultValue"), "declares setDefaultValue");
        check(declares(c, "getDefaultChecked"), "declares getDefaultChecked");
        check(declares(c, "setDefaultChecked"), "declares setDefaultChecked");
        check(declares(c, "getForm"), "declares getForm");
        check(declares(c, "getAccept"), "declares getAccept");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
