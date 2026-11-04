/*
 * @test
 * @summary org.w3c.dom.html.HTMLOListElement conformance: reflection shape checks.
 * @run main HTMLOListElementTest
 */

import java.lang.reflect.*;

public class HTMLOListElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLOListElement");
        check(c != null, "org.w3c.dom.html.HTMLOListElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLOListElement"), "binary name is org.w3c.dom.html.HTMLOListElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLOListElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLOListElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getCompact"), "declares getCompact");
        check(declares(c, "setCompact"), "declares setCompact");
        check(declares(c, "getStart"), "declares getStart");
        check(declares(c, "setStart"), "declares setStart");
        check(declares(c, "getType"), "declares getType");
        check(declares(c, "setType"), "declares setType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
