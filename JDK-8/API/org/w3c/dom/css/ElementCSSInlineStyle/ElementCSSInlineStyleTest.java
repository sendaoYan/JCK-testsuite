/*
 * @test
 * @summary org.w3c.dom.css.ElementCSSInlineStyle conformance: reflection shape checks.
 * @run main ElementCSSInlineStyleTest
 */

import java.lang.reflect.*;

public class ElementCSSInlineStyleTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.ElementCSSInlineStyle");
        check(c != null, "org.w3c.dom.css.ElementCSSInlineStyle loads");
        check(c.getName().equals("org.w3c.dom.css.ElementCSSInlineStyle"), "binary name is org.w3c.dom.css.ElementCSSInlineStyle");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.ElementCSSInlineStyle is public");
        check(c.isInterface(), "org.w3c.dom.css.ElementCSSInlineStyle is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getStyle"), "declares getStyle");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: ElementCSSInlineStyle contract (inline style channel) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        check(!org.w3c.dom.css.ElementCSSInlineStyle.class.isInstance(d.createElement("e")), "Element is not an ElementCSSInlineStyle");
        org.w3c.dom.css.CSSStyleDeclaration decl = new org.w3c.dom.css.CSSStyleDeclaration() {
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
        org.w3c.dom.css.ElementCSSInlineStyle e = new org.w3c.dom.css.ElementCSSInlineStyle() {
            public org.w3c.dom.css.CSSStyleDeclaration getStyle() { return decl; }
        };
        check(e.getStyle() == decl && "red".equals(e.getStyle().getPropertyValue("color")), "inline style declaration wired");
    }
}
