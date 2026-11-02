/*
 * @test
 * @summary org.w3c.dom.css.CSSStyleDeclaration conformance: reflection shape checks.
 * @run main CSSStyleDeclarationTest
 */

import java.lang.reflect.*;

public class CSSStyleDeclarationTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSStyleDeclaration");
        check(c != null, "org.w3c.dom.css.CSSStyleDeclaration loads");
        check(c.getName().equals("org.w3c.dom.css.CSSStyleDeclaration"), "binary name is org.w3c.dom.css.CSSStyleDeclaration");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSStyleDeclaration is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSStyleDeclaration is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getCssText"), "declares getCssText");
        check(declares(c, "setCssText"), "declares setCssText");
        check(declares(c, "getPropertyValue"), "declares getPropertyValue");
        check(declares(c, "getPropertyCSSValue"), "declares getPropertyCSSValue");
        check(declares(c, "removeProperty"), "declares removeProperty");
        check(declares(c, "getPropertyPriority"), "declares getPropertyPriority");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
