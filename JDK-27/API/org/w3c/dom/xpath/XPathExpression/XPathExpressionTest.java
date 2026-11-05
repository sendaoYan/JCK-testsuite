/*
 * @test
 * @summary org.w3c.dom.xpath.XPathExpression conformance: compiled expression.
 * @run main XPathExpressionTest
 */

public class XPathExpressionTest {
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

        Class<?> k = L("org.w3c.dom.xpath.XPathExpression");
        check(k.isInterface(), "org.w3c.dom.xpath.XPathExpression is an interface");
        check(k.getInterfaces().length == 0, "XPathExpression declares no superinterface");
        check(k.getMethod("evaluate", L("org.w3c.dom.Node"), short.class, Object.class) != null, "evaluate(org.w3c.dom.Node, short, Object)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.xpath.XPathExpression a = new org.w3c.dom.xpath.XPathExpression(){
            public java.lang.Object evaluate(org.w3c.dom.Node p0, short p1, java.lang.Object p2) { return null; }
        };
        check(a != null, "anon impl of XPathExpression");
        check(a.evaluate((org.w3c.dom.Node) null, (short)2, (java.lang.Object) null) == null, "evaluate() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: XPathExpression contract (shape-lock) ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.xpath.XPathExpression.class.isInterface(), "XPathExpression is an interface");
        java.lang.reflect.Method m = org.w3c.dom.xpath.XPathExpression.class.getMethod("evaluate",
            org.w3c.dom.Node.class, short.class, Object.class);
        check(m != null, "evaluate(Node,short,Object) declared");
        Class<?>[] exs = m.getExceptionTypes();
        java.util.Set<String> exn = new java.util.HashSet<String>();
        for (Class<?> c : exs) exn.add(c.getName());
        check(exn.contains("org.w3c.dom.xpath.XPathException") && exn.contains("org.w3c.dom.DOMException"), "evaluate declares XPathException + DOMException");
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        org.w3c.dom.xpath.XPathExpression x = new org.w3c.dom.xpath.XPathExpression() {
            public Object evaluate(org.w3c.dom.Node ctx, short type, Object res) { return ctx == null ? null : ctx.getNodeName(); }
        };
        org.w3c.dom.Node probe = d;
        check(probe.getNodeName().equals(x.evaluate(probe, org.w3c.dom.xpath.XPathResult.ANY_TYPE, null)), "anonymous expression returns the context node name");
        check(x.evaluate(null, org.w3c.dom.xpath.XPathResult.NUMBER_TYPE, null) == null, "anonymous expression honors null context");
    }
}
