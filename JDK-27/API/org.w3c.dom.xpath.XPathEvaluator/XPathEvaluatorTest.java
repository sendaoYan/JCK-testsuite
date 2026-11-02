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

    public static void main(String[] args) throws Exception {
        Class<?> k = L("org.w3c.dom.xpath.XPathEvaluator");
        check(k.isInterface(), "org.w3c.dom.xpath.XPathEvaluator is an interface");
        check(k.getInterfaces().length == 0, "XPathEvaluator declares no superinterface");
        check(k.getMethod("createExpression", String.class, L("org.w3c.dom.xpath.XPathNSResolver")) != null, "createExpression(String, org.w3c.dom.xpath.XPathNSResolver)");
        check(k.getMethod("createNSResolver", L("org.w3c.dom.Node")) != null, "createNSResolver(org.w3c.dom.Node)");
        check(k.getMethod("evaluate", String.class, L("org.w3c.dom.Node"), L("org.w3c.dom.xpath.XPathNSResolver"), short.class, Object.class) != null, "evaluate(String, org.w3c.dom.Node, org.w3c.dom.xpath.XPathNSResolver, short, Object)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
