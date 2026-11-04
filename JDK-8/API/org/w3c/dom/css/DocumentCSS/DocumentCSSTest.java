/*
 * @test
 * @summary org.w3c.dom.css.DocumentCSS conformance: reflection shape checks.
 * @run main DocumentCSSTest
 */

import java.lang.reflect.*;

public class DocumentCSSTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.DocumentCSS");
        check(c != null, "org.w3c.dom.css.DocumentCSS loads");
        check(c.getName().equals("org.w3c.dom.css.DocumentCSS"), "binary name is org.w3c.dom.css.DocumentCSS");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.DocumentCSS is public");
        check(c.isInterface(), "org.w3c.dom.css.DocumentCSS is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.stylesheets.DocumentStyle")), "direct superinterfaces");
        check(declares(c, "getOverrideStyle"), "declares getOverrideStyle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
