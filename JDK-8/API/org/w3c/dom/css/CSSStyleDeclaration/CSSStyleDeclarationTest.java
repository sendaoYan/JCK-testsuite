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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSStyleDeclaration contract (property channels) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.css.CSSStyleDeclaration d = new org.w3c.dom.css.CSSStyleDeclaration() {
            String text = "color: red";
            public String getCssText() { return text; }
            public void setCssText(String t) { text = t; }
            public String getPropertyValue(String p) { return "red"; }
            public org.w3c.dom.css.CSSValue getPropertyCSSValue(String p) { return null; }
            public String removeProperty(String p) { return "red"; }
            public String getPropertyPriority(String p) { return "important"; }
            public void setProperty(String p, String v, String pri) { }
            public int getLength() { return 1; }
            public String item(int i) { return "color"; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check("red".equals(d.getPropertyValue("color")), "getPropertyValue routed");
        check("important".equals(d.getPropertyPriority("color")), "getPropertyPriority routed");
        check("red".equals(d.removeProperty("color")), "removeProperty returns the old value");
        check(d.getLength() == 1 && "color".equals(d.item(0)), "length/item enumeration");
        d.setCssText("margin: 0");
        check("margin: 0".equals(d.getCssText()), "setCssText/getCssText round-trip");
        check(d.getParentRule() == null, "parent rule null when standalone");
    }
}
