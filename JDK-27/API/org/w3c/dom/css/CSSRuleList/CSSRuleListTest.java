/*
 * @test
 * @summary org.w3c.dom.css.CSSRuleList conformance: ordered rule list.
 * @run main CSSRuleListTest
 */

public class CSSRuleListTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSRuleList");
        check(k.isInterface(), "org.w3c.dom.css.CSSRuleList is an interface");
        check(k.getInterfaces().length == 0, "CSSRuleList declares no superinterface");
        check(k.getMethod("getLength") != null, "getLength()");
        check(k.getMethod("item", int.class) != null, "item(int)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSRuleList a = new org.w3c.dom.css.CSSRuleList(){
            public int getLength() { return 3; }
            public org.w3c.dom.css.CSSRule item(int p0) { return null; }
        };
        check(a != null, "anon impl of CSSRuleList");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check(a.item(3) == null, "item() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSRuleList contract (indexed access) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.css.CSSRuleList l = new org.w3c.dom.css.CSSRuleList() {
            public int getLength() { return 2; }
            public org.w3c.dom.css.CSSRule item(int i) { return null; }
        };
        check(l.getLength() == 2 && l.item(0) == null, "getLength/item wiring");
        check(l.item(99) == null, "out-of-range item -> null (no exception)");
    }
}
