/*
 * @test
 * @summary org.w3c.dom.css.CSSCharsetRule conformance: charset rule.
 * @run main CSSCharsetRuleTest
 */

public class CSSCharsetRuleTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSCharsetRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSCharsetRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSCharsetRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSCharsetRule has superinterface");
        check(k.getMethod("getEncoding") != null, "getEncoding()");
        check(k.getMethod("setEncoding", String.class) != null, "setEncoding(String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSCharsetRule a = new org.w3c.dom.css.CSSCharsetRule(){
            public java.lang.String getEncoding() { return "s"; }
            public void setEncoding(java.lang.String p0) { }
            public short getType() { return (short)2; }
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(a != null, "anon impl of CSSCharsetRule");
        check("s".equals(a.getEncoding()), "getEncoding() returns fixed default");
        a.setEncoding("s");
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

    // ---- deepening round 2: CSSCharsetRule contract (encoding accessor + rule typing) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSRule.class.isAssignableFrom(org.w3c.dom.css.CSSCharsetRule.class), "CSSCharsetRule extends CSSRule");
        org.w3c.dom.css.CSSCharsetRule r = new org.w3c.dom.css.CSSCharsetRule() {
            String enc = "UTF-8";
            public String getEncoding() { return enc; }
            public void setEncoding(String e) { enc = e; }
            public String getCssText() { return null; }
            public void setCssText(String t) { }
            public short getType() { return org.w3c.dom.css.CSSRule.CHARSET_RULE; }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check("UTF-8".equals(r.getEncoding()), "getEncoding round-trip");
        r.setEncoding("ISO-8859-1");
        check("ISO-8859-1".equals(r.getEncoding()), "setEncoding mutates the wired field");
        check(r.getType() == org.w3c.dom.css.CSSRule.CHARSET_RULE, "rule reports CHARSET_RULE(2)");
    }
}
