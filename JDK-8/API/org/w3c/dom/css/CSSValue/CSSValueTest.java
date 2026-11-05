/*
 * @test
 * @summary org.w3c.dom.css.CSSValue conformance: reflection shape checks.
 * @run main CSSValueTest
 */

import java.lang.reflect.*;

public class CSSValueTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSValue");
        check(c != null, "org.w3c.dom.css.CSSValue loads");
        check(c.getName().equals("org.w3c.dom.css.CSSValue"), "binary name is org.w3c.dom.css.CSSValue");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSValue is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSValue is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getCssText"), "declares getCssText");
        check(declares(c, "setCssText"), "declares setCssText");
        check(declares(c, "getCssValueType"), "declares getCssValueType");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSValue contract (4 value-type constants) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSValue.CSS_INHERIT == 0 && org.w3c.dom.css.CSSValue.CSS_PRIMITIVE_VALUE == 1, "CSS_INHERIT=0 CSS_PRIMITIVE_VALUE=1");
        check(org.w3c.dom.css.CSSValue.CSS_VALUE_LIST == 2 && org.w3c.dom.css.CSSValue.CSS_CUSTOM == 3, "CSS_VALUE_LIST=2 CSS_CUSTOM=3");
        org.w3c.dom.css.CSSValue v = new org.w3c.dom.css.CSSValue() {
            String text = "inherit";
            public String getCssText() { return text; }
            public void setCssText(String t) { text = t; }
            public short getCssValueType() { return org.w3c.dom.css.CSSValue.CSS_INHERIT; }
        };
        check("inherit".equals(v.getCssText()), "cssText round-trip start");
        v.setCssText("1px solid");
        check("1px solid".equals(v.getCssText()), "setCssText/getCssText round-trip");
        check(v.getCssValueType() == org.w3c.dom.css.CSSValue.CSS_INHERIT, "value type reports CSS_INHERIT(0)");
    }
}
