/*
 * @test
 * @summary org.w3c.dom.html.HTMLMetaElement conformance: reflection shape checks.
 * @run main HTMLMetaElementTest
 */

import java.lang.reflect.*;

public class HTMLMetaElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLMetaElement");
        check(c != null, "org.w3c.dom.html.HTMLMetaElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLMetaElement"), "binary name is org.w3c.dom.html.HTMLMetaElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLMetaElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLMetaElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getContent"), "declares getContent");
        check(declares(c, "setContent"), "declares setContent");
        check(declares(c, "getHttpEquiv"), "declares getHttpEquiv");
        check(declares(c, "setHttpEquiv"), "declares setHttpEquiv");
        check(declares(c, "getName"), "declares getName");
        check(declares(c, "setName"), "declares setName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
