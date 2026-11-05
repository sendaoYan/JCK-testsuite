/*
 * @test
 * @summary org.w3c.dom.xpath.XPathEvaluator conformance: expression evaluator.
 * @run main XPathEvaluatorTest
 */

public class XPathEvaluatorTest {
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

        Class<?> k = L("org.w3c.dom.xpath.XPathEvaluator");
        check(k.isInterface(), "org.w3c.dom.xpath.XPathEvaluator is an interface");
        check(k.getInterfaces().length == 0, "XPathEvaluator declares no superinterface");
        check(k.getMethod("createExpression", String.class, L("org.w3c.dom.xpath.XPathNSResolver")) != null, "createExpression(String, org.w3c.dom.xpath.XPathNSResolver)");
        check(k.getMethod("createNSResolver", L("org.w3c.dom.Node")) != null, "createNSResolver(org.w3c.dom.Node)");
        check(k.getMethod("evaluate", String.class, L("org.w3c.dom.Node"), L("org.w3c.dom.xpath.XPathNSResolver"), short.class, Object.class) != null, "evaluate(String, org.w3c.dom.Node, org.w3c.dom.xpath.XPathNSResolver, short, Object)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.xpath.XPathEvaluator a = new org.w3c.dom.xpath.XPathEvaluator(){
            public org.w3c.dom.xpath.XPathExpression createExpression(java.lang.String p0, org.w3c.dom.xpath.XPathNSResolver p1) { return null; }
            public org.w3c.dom.xpath.XPathNSResolver createNSResolver(org.w3c.dom.Node p0) { return null; }
            public java.lang.Object evaluate(java.lang.String p0, org.w3c.dom.Node p1, org.w3c.dom.xpath.XPathNSResolver p2, short p3, java.lang.Object p4) { return null; }
        };
        check(a != null, "anon impl of XPathEvaluator");
        check(a.createExpression("s", (org.w3c.dom.xpath.XPathNSResolver) null) == null, "createExpression() returns fixed default");
        check(a.createNSResolver((org.w3c.dom.Node) null) == null, "createNSResolver() returns fixed default");
        check(a.evaluate("s", (org.w3c.dom.Node) null, (org.w3c.dom.xpath.XPathNSResolver) null, (short)2, (java.lang.Object) null) == null, "evaluate() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: XPathEvaluator contract (shape-lock; no JDK implementation) ----
    static void deepContract() throws Exception {
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.xpath.XPathEvaluator.class.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("createExpression") && names.contains("createNSResolver") && names.contains("evaluate"), "three evaluator methods declared");
        check(org.w3c.dom.xpath.XPathEvaluator.class.isInterface(), "XPathEvaluator is an interface");
        // no implementation is wired into the DOM: feature probe + cast probe
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        check(d.getImplementation().getFeature("XPath", "3.0") == null, "getFeature(XPath,3.0) -> null (module absent)");
        check(!org.w3c.dom.xpath.XPathEvaluator.class.isInstance(d), "Document is not an XPathEvaluator");
        // anonymous implementation honors the method contracts
        org.w3c.dom.xpath.XPathEvaluator ev = new org.w3c.dom.xpath.XPathEvaluator() {
            public org.w3c.dom.xpath.XPathExpression createExpression(String expr, org.w3c.dom.xpath.XPathNSResolver r) { return null; }
            public org.w3c.dom.xpath.XPathNSResolver createNSResolver(org.w3c.dom.Node n) { return null; }
            public Object evaluate(String expr, org.w3c.dom.Node ctx, org.w3c.dom.xpath.XPathNSResolver r, short type, Object res) { return null; }
        };
        check(ev.createExpression("/r", null) == null && ev.createNSResolver(d) == null, "anonymous evaluator methods callable");
        check(ev.evaluate("/r", d, null, org.w3c.dom.xpath.XPathResult.ANY_TYPE, null) == null, "evaluate delegates with ANY_TYPE");
    }
}
