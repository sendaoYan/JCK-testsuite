/*
 * @test
 * @summary org.w3c.dom.css.CSSPageRule conformance: reflection shape checks.
 * @run main CSSPageRuleTest
 */

import java.lang.reflect.*;

public class CSSPageRuleTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSPageRule");
        check(c != null, "org.w3c.dom.css.CSSPageRule loads");
        check(c.getName().equals("org.w3c.dom.css.CSSPageRule"), "binary name is org.w3c.dom.css.CSSPageRule");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSPageRule is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSPageRule is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.css.CSSRule")), "direct superinterfaces");
        check(declares(c, "getSelectorText"), "declares getSelectorText");
        check(declares(c, "setSelectorText"), "declares setSelectorText");
        check(declares(c, "getStyle"), "declares getStyle");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSPageRule contract (selector + declaration) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSRule.class.isAssignableFrom(org.w3c.dom.css.CSSPageRule.class), "CSSPageRule extends CSSRule");
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
        org.w3c.dom.css.CSSPageRule r = new org.w3c.dom.css.CSSPageRule() {
            String sel = ":first";
            public String getSelectorText() { return sel; }
            public void setSelectorText(String s) { sel = s; }
            public org.w3c.dom.css.CSSStyleDeclaration getStyle() { return decl; }
            public String getCssText() { return null; }
            public void setCssText(String t) { }
            public short getType() { return org.w3c.dom.css.CSSRule.PAGE_RULE; }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(":first".equals(r.getSelectorText()), "selector round-trip");
        r.setSelectorText(":left");
        check(":left".equals(r.getSelectorText()), "setSelectorText mutates");
        check(r.getStyle() == decl, "style declaration wired");
        check(r.getType() == org.w3c.dom.css.CSSRule.PAGE_RULE, "rule reports PAGE_RULE(6)");
    }
}
