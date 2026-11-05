/*
 * @test
 * @summary org.w3c.dom.css.CSSPageRule conformance: page rule.
 * @run main CSSPageRuleTest
 */

public class CSSPageRuleTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.css.CSSPageRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSPageRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSPageRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSPageRule has superinterface");
        check(k.getMethod("getSelectorText") != null, "getSelectorText()");
        check(k.getMethod("setSelectorText", String.class) != null, "setSelectorText(String)");
        check(k.getMethod("getStyle") != null, "getStyle()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSPageRule a = new org.w3c.dom.css.CSSPageRule(){
            public java.lang.String getSelectorText() { return "s"; }
            public void setSelectorText(java.lang.String p0) { }
            public org.w3c.dom.css.CSSStyleDeclaration getStyle() { return null; }
            public short getType() { return (short)2; }
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(a != null, "anon impl of CSSPageRule");
        check("s".equals(a.getSelectorText()), "getSelectorText() returns fixed default");
        a.setSelectorText("s");
        check(a.getStyle() == null, "getStyle() returns fixed default");
        check(a.getType() == (short)2, "getType() returns fixed default");
        check("s".equals(a.getCssText()), "getCssText() returns fixed default");
        a.setCssText("s");
        check(a.getParentStyleSheet() == null, "getParentStyleSheet() returns fixed default");
        check(a.getParentRule() == null, "getParentRule() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
