/*
 * @test
 * @summary org.w3c.dom.css.CSSValue conformance: value type constants.
 * @run main CSSValueTest
 */

public class CSSValueTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSValue");
        check(k.isInterface(), "org.w3c.dom.css.CSSValue is an interface");
        check(k.getInterfaces().length == 0, "CSSValue declares no superinterface");
        check(sconst("org.w3c.dom.css.CSSValue", "CSS_INHERIT") == 0, "CSS_INHERIT == 0");
        check(sconst("org.w3c.dom.css.CSSValue", "CSS_PRIMITIVE_VALUE") == 1, "CSS_PRIMITIVE_VALUE == 1");
        check(sconst("org.w3c.dom.css.CSSValue", "CSS_VALUE_LIST") == 2, "CSS_VALUE_LIST == 2");
        check(sconst("org.w3c.dom.css.CSSValue", "CSS_CUSTOM") == 3, "CSS_CUSTOM == 3");
        check(k.getMethod("getCssText") != null, "getCssText()");
        check(k.getMethod("setCssText", String.class) != null, "setCssText(String)");
        check(k.getMethod("getCssValueType") != null, "getCssValueType()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.css.CSSValue.CSS_INHERIT == 0, "CSS_INHERIT == 0");
        check(org.w3c.dom.css.CSSValue.CSS_PRIMITIVE_VALUE == 1, "CSS_PRIMITIVE_VALUE == 1");
        check(org.w3c.dom.css.CSSValue.CSS_VALUE_LIST == 2, "CSS_VALUE_LIST == 2");
        check(org.w3c.dom.css.CSSValue.CSS_CUSTOM == 3, "CSS_CUSTOM == 3");
        org.w3c.dom.css.CSSValue a = new org.w3c.dom.css.CSSValue(){
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public short getCssValueType() { return (short)2; }
        };
        check(a != null, "anon impl of CSSValue");
        check("s".equals(a.getCssText()), "getCssText() returns fixed default");
        a.setCssText("s");
        check(a.getCssValueType() == (short)2, "getCssValueType() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
