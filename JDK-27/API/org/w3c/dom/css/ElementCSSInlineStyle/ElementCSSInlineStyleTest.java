/*
 * @test
 * @summary org.w3c.dom.css.ElementCSSInlineStyle conformance: inline style.
 * @run main ElementCSSInlineStyleTest
 */

public class ElementCSSInlineStyleTest {
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

        Class<?> k = L("org.w3c.dom.css.ElementCSSInlineStyle");
        check(k.isInterface(), "org.w3c.dom.css.ElementCSSInlineStyle is an interface");
        check(k.getInterfaces().length == 0, "ElementCSSInlineStyle declares no superinterface");
        check(k.getMethod("getStyle") != null, "getStyle()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.ElementCSSInlineStyle a = new org.w3c.dom.css.ElementCSSInlineStyle(){
            public org.w3c.dom.css.CSSStyleDeclaration getStyle() { return null; }
        };
        check(a != null, "anon impl of ElementCSSInlineStyle");
        check(a.getStyle() == null, "getStyle() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
