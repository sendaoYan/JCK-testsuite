/*
 * @test
 * @summary org.w3c.dom.css.CSSRule conformance: rule type constants.
 * @run main CSSRuleTest
 */

public class CSSRuleTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSRule is an interface");
        check(k.getInterfaces().length == 0, "CSSRule declares no superinterface");
        check(sconst("org.w3c.dom.css.CSSRule", "UNKNOWN_RULE") == 0, "UNKNOWN_RULE == 0");
        check(sconst("org.w3c.dom.css.CSSRule", "STYLE_RULE") == 1, "STYLE_RULE == 1");
        check(sconst("org.w3c.dom.css.CSSRule", "CHARSET_RULE") == 2, "CHARSET_RULE == 2");
        check(sconst("org.w3c.dom.css.CSSRule", "IMPORT_RULE") == 3, "IMPORT_RULE == 3");
        check(sconst("org.w3c.dom.css.CSSRule", "MEDIA_RULE") == 4, "MEDIA_RULE == 4");
        check(sconst("org.w3c.dom.css.CSSRule", "FONT_FACE_RULE") == 5, "FONT_FACE_RULE == 5");
        check(sconst("org.w3c.dom.css.CSSRule", "PAGE_RULE") == 6, "PAGE_RULE == 6");
        check(k.getMethod("getType") != null, "getType()");
        check(k.getMethod("getCssText") != null, "getCssText()");
        check(k.getMethod("setCssText", String.class) != null, "setCssText(String)");
        check(k.getMethod("getParentStyleSheet") != null, "getParentStyleSheet()");
        check(k.getMethod("getParentRule") != null, "getParentRule()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.css.CSSRule.UNKNOWN_RULE == 0, "UNKNOWN_RULE == 0");
        check(org.w3c.dom.css.CSSRule.STYLE_RULE == 1, "STYLE_RULE == 1");
        check(org.w3c.dom.css.CSSRule.CHARSET_RULE == 2, "CHARSET_RULE == 2");
        check(org.w3c.dom.css.CSSRule.IMPORT_RULE == 3, "IMPORT_RULE == 3");
        check(org.w3c.dom.css.CSSRule.MEDIA_RULE == 4, "MEDIA_RULE == 4");
        check(org.w3c.dom.css.CSSRule.FONT_FACE_RULE == 5, "FONT_FACE_RULE == 5");
        check(org.w3c.dom.css.CSSRule.PAGE_RULE == 6, "PAGE_RULE == 6");
        org.w3c.dom.css.CSSRule a = new org.w3c.dom.css.CSSRule(){
            public short getType() { return (short)2; }
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(a != null, "anon impl of CSSRule");
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

    // ---- deepening round 2: CSSRule contract (7 rule-type constants + base shape) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSRule.UNKNOWN_RULE == 0 && org.w3c.dom.css.CSSRule.STYLE_RULE == 1, "UNKNOWN_RULE=0 STYLE_RULE=1");
        check(org.w3c.dom.css.CSSRule.CHARSET_RULE == 2 && org.w3c.dom.css.CSSRule.IMPORT_RULE == 3, "CHARSET_RULE=2 IMPORT_RULE=3");
        check(org.w3c.dom.css.CSSRule.MEDIA_RULE == 4 && org.w3c.dom.css.CSSRule.FONT_FACE_RULE == 5, "MEDIA_RULE=4 FONT_FACE_RULE=5");
        check(org.w3c.dom.css.CSSRule.PAGE_RULE == 6, "PAGE_RULE=6");
        org.w3c.dom.css.CSSRule r = new org.w3c.dom.css.CSSRule() {
            public String getCssText() { return "@rule"; }
            public void setCssText(String t) { }
            public short getType() { return org.w3c.dom.css.CSSRule.STYLE_RULE; }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check("@rule".equals(r.getCssText()) && r.getType() == org.w3c.dom.css.CSSRule.STYLE_RULE, "anonymous base rule returns the wired values");
    }
}
