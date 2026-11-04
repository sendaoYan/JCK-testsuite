/*
 * @test
 * @summary javax.xml.xpath.XPathFactory conformance: xpath factory.
 * @run main XPathFactoryTest
 */


public class XPathFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.xml.xpath.XPathFactory xf = javax.xml.xpath.XPathFactory.newInstance();
        check(xf != null, "factory non-null");
        check(xf.isObjectModelSupported(javax.xml.xpath.XPathConstants.DOM_OBJECT_MODEL), "dom supported");
        javax.xml.xpath.XPath xp = xf.newXPath();
        check(xp != null, "xpath non-null");
        check("42".equals(xp.evaluate("40 + 2", (Object) null)), "evaluate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
