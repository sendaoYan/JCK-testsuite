/*
 * @test
 * @summary org.w3c.dom.html.HTMLFrameElement conformance: reflection shape checks.
 * @run main HTMLFrameElementTest
 */

import java.lang.reflect.*;

public class HTMLFrameElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLFrameElement");
        check(c != null, "org.w3c.dom.html.HTMLFrameElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLFrameElement"), "binary name is org.w3c.dom.html.HTMLFrameElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLFrameElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLFrameElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getFrameBorder"), "declares getFrameBorder");
        check(declares(c, "setFrameBorder"), "declares setFrameBorder");
        check(declares(c, "getLongDesc"), "declares getLongDesc");
        check(declares(c, "setLongDesc"), "declares setLongDesc");
        check(declares(c, "getMarginHeight"), "declares getMarginHeight");
        check(declares(c, "setMarginHeight"), "declares setMarginHeight");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
