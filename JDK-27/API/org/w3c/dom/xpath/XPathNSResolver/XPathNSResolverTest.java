/*
 * @test
 * @summary org.w3c.dom.xpath.XPathNSResolver conformance: namespace resolver.
 * @run main XPathNSResolverTest
 */

public class XPathNSResolverTest {
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

        Class<?> k = L("org.w3c.dom.xpath.XPathNSResolver");
        check(k.isInterface(), "org.w3c.dom.xpath.XPathNSResolver is an interface");
        check(k.getInterfaces().length == 0, "XPathNSResolver declares no superinterface");
        check(k.getMethod("lookupNamespaceURI", String.class) != null, "lookupNamespaceURI(String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.xpath.XPathNSResolver a = new org.w3c.dom.xpath.XPathNSResolver(){
            public java.lang.String lookupNamespaceURI(java.lang.String p0) { return "s"; }
        };
        check(a != null, "anon impl of XPathNSResolver");
        check("s".equals(a.lookupNamespaceURI("s")), "lookupNamespaceURI() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: XPathNSResolver contract ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.xpath.XPathNSResolver.class.isInterface(), "XPathNSResolver is an interface");
        java.lang.reflect.Method m = org.w3c.dom.xpath.XPathNSResolver.class.getMethod("lookupNamespaceURI", String.class);
        check(m != null, "single method lookupNamespaceURI(String)");
        check(org.w3c.dom.xpath.XPathNSResolver.class.getDeclaredFields().length == 0, "no constants");
        org.w3c.dom.xpath.XPathNSResolver r = new org.w3c.dom.xpath.XPathNSResolver() {
            public String lookupNamespaceURI(String prefix) { return "p".equals(prefix) ? "urn:p" : null; }
        };
        check("urn:p".equals(r.lookupNamespaceURI("p")), "resolver maps the known prefix");
        check(r.lookupNamespaceURI("unknown") == null, "unknown prefix -> null");
        check(r.lookupNamespaceURI(null) == null, "null prefix -> null");
    }
}
