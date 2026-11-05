/*
 * @test
 * @summary org.w3c.dom.css.CSSUnknownRule conformance: unknown rule.
 * @run main CSSUnknownRuleTest
 */

public class CSSUnknownRuleTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSUnknownRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSUnknownRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSUnknownRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSUnknownRule has superinterface");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSUnknownRule a = new org.w3c.dom.css.CSSUnknownRule(){
            public short getType() { return (short)2; }
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(a != null, "anon impl of CSSUnknownRule");
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

    // ---- deepening round 2: CSSUnknownRule contract (marker subtype) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSRule.class.isAssignableFrom(org.w3c.dom.css.CSSUnknownRule.class), "CSSUnknownRule extends CSSRule");
        check(org.w3c.dom.css.CSSUnknownRule.class.getDeclaredMethods().length == 0, "declares no methods of its own");
        org.w3c.dom.css.CSSUnknownRule r = new org.w3c.dom.css.CSSUnknownRule() {
            public String getCssText() { return "@x"; }
            public void setCssText(String t) { }
            public short getType() { return org.w3c.dom.css.CSSRule.UNKNOWN_RULE; }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(r.getType() == org.w3c.dom.css.CSSRule.UNKNOWN_RULE && "@x".equals(r.getCssText()), "unknown rule carries UNKNOWN_RULE(0)");
    }
}
