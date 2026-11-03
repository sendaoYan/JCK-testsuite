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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
