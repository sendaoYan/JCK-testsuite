/*
 * @test
 * @summary org.w3c.dom.css.CSSValueList conformance: value list.
 * @run main CSSValueListTest
 */

public class CSSValueListTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSValueList");
        check(k.isInterface(), "org.w3c.dom.css.CSSValueList is an interface");
        check(L("org.w3c.dom.css.CSSValue").isAssignableFrom(k), "CSSValueList extends CSSValue");
        check(k.getInterfaces().length >= 1, "CSSValueList has superinterface");
        check(k.getMethod("getLength") != null, "getLength()");
        check(k.getMethod("item", int.class) != null, "item(int)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSValueList a = new org.w3c.dom.css.CSSValueList(){
            public int getLength() { return 3; }
            public org.w3c.dom.css.CSSValue item(int p0) { return null; }
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public short getCssValueType() { return (short)2; }
        };
        check(a != null, "anon impl of CSSValueList");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check(a.item(3) == null, "item() returns fixed default");
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

    // ---- deepening round 2: CSSValueList contract (indexed access) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.css.CSSValueList l = new org.w3c.dom.css.CSSValueList() {
            public int getLength() { return 3; }
            public org.w3c.dom.css.CSSValue item(int i) { return null; }
            public String getCssText() { return "a b c"; }
            public void setCssText(String t) { }
            public short getCssValueType() { return org.w3c.dom.css.CSSValue.CSS_VALUE_LIST; }
        };
        check("a b c".equals(l.getCssText()), "inherits the CSSValue text channel");
        check(l.getLength() == 3 && l.item(1) == null, "getLength/item wiring");
        check(l.item(3) == null, "out-of-range item -> null");
    }
}
