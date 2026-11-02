/*
 * @test
 * @summary org.w3c.dom.css.CSSStyleSheet conformance: reflection shape checks.
 * @run main CSSStyleSheetTest
 */

import java.lang.reflect.*;

public class CSSStyleSheetTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSStyleSheet");
        check(c != null, "org.w3c.dom.css.CSSStyleSheet loads");
        check(c.getName().equals("org.w3c.dom.css.CSSStyleSheet"), "binary name is org.w3c.dom.css.CSSStyleSheet");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSStyleSheet is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSStyleSheet is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.stylesheets.StyleSheet")), "direct superinterfaces");
        check(declares(c, "getOwnerRule"), "declares getOwnerRule");
        check(declares(c, "getCssRules"), "declares getCssRules");
        check(declares(c, "insertRule"), "declares insertRule");
        check(declares(c, "deleteRule"), "declares deleteRule");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
