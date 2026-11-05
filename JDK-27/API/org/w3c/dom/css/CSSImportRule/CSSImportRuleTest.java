/*
 * @test
 * @summary org.w3c.dom.css.CSSImportRule conformance: import rule.
 * @run main CSSImportRuleTest
 */

public class CSSImportRuleTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSImportRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSImportRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSImportRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSImportRule has superinterface");
        check(k.getMethod("getHref") != null, "getHref()");
        check(k.getMethod("getMedia") != null, "getMedia()");
        check(k.getMethod("getStyleSheet") != null, "getStyleSheet()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSImportRule a = new org.w3c.dom.css.CSSImportRule(){
            public java.lang.String getHref() { return "s"; }
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
            public org.w3c.dom.css.CSSStyleSheet getStyleSheet() { return null; }
            public short getType() { return (short)2; }
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(a != null, "anon impl of CSSImportRule");
        check("s".equals(a.getHref()), "getHref() returns fixed default");
        check(a.getMedia() == null, "getMedia() returns fixed default");
        check(a.getStyleSheet() == null, "getStyleSheet() returns fixed default");
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

    // ---- deepening round 2: CSSImportRule contract (href/media/sheet) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSRule.class.isAssignableFrom(org.w3c.dom.css.CSSImportRule.class), "CSSImportRule extends CSSRule");
        org.w3c.dom.css.CSSImportRule r = new org.w3c.dom.css.CSSImportRule() {
            public String getHref() { return "imp.css"; }
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
            public org.w3c.dom.css.CSSStyleSheet getStyleSheet() { return null; }
            public String getCssText() { return null; }
            public void setCssText(String t) { }
            public short getType() { return org.w3c.dom.css.CSSRule.IMPORT_RULE; }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check("imp.css".equals(r.getHref()), "href round-trip");
        check(r.getType() == org.w3c.dom.css.CSSRule.IMPORT_RULE, "rule reports IMPORT_RULE(3)");
        check(r.getMedia() == null && r.getStyleSheet() == null, "media/sheet nulls by wiring");
    }
}
