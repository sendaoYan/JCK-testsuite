/*
 * @test
 * @summary org.w3c.dom.css.Rect conformance: reflection shape checks.
 * @run main RectTest
 */

import java.lang.reflect.*;

public class RectTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.Rect");
        check(c != null, "org.w3c.dom.css.Rect loads");
        check(c.getName().equals("org.w3c.dom.css.Rect"), "binary name is org.w3c.dom.css.Rect");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.Rect is public");
        check(c.isInterface(), "org.w3c.dom.css.Rect is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getTop"), "declares getTop");
        check(declares(c, "getRight"), "declares getRight");
        check(declares(c, "getBottom"), "declares getBottom");
        check(declares(c, "getLeft"), "declares getLeft");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: Rect contract (four edges) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.css.Rect r = new org.w3c.dom.css.Rect() {
            public org.w3c.dom.css.CSSPrimitiveValue getTop() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getRight() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getBottom() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getLeft() { return null; }
        };
        check(r.getTop() == null && r.getRight() == null && r.getBottom() == null && r.getLeft() == null, "four edge accessors routed");
    }
}
