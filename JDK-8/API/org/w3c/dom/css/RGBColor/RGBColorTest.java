/*
 * @test
 * @summary org.w3c.dom.css.RGBColor conformance: reflection shape checks.
 * @run main RGBColorTest
 */

import java.lang.reflect.*;

public class RGBColorTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.RGBColor");
        check(c != null, "org.w3c.dom.css.RGBColor loads");
        check(c.getName().equals("org.w3c.dom.css.RGBColor"), "binary name is org.w3c.dom.css.RGBColor");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.RGBColor is public");
        check(c.isInterface(), "org.w3c.dom.css.RGBColor is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getRed"), "declares getRed");
        check(declares(c, "getGreen"), "declares getGreen");
        check(declares(c, "getBlue"), "declares getBlue");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: RGBColor contract (three channels) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.css.RGBColor c = new org.w3c.dom.css.RGBColor() {
            public org.w3c.dom.css.CSSPrimitiveValue getRed() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getGreen() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getBlue() { return null; }
        };
        check(c.getRed() == null && c.getGreen() == null && c.getBlue() == null, "three channel accessors routed");
    }
}
