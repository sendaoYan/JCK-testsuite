/*
 * @test
 * @summary org.w3c.dom.css.CSSRule conformance: reflection shape checks.
 * @run main CSSRuleTest
 */

import java.lang.reflect.*;

public class CSSRuleTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSRule");
        check(c != null, "org.w3c.dom.css.CSSRule loads");
        check(c.getName().equals("org.w3c.dom.css.CSSRule"), "binary name is org.w3c.dom.css.CSSRule");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSRule is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSRule is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getType"), "declares getType");
        check(declares(c, "getCssText"), "declares getCssText");
        check(declares(c, "setCssText"), "declares setCssText");
        check(declares(c, "getParentStyleSheet"), "declares getParentStyleSheet");
        check(declares(c, "getParentRule"), "declares getParentRule");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
