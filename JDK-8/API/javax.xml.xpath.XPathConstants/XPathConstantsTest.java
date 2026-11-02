/*
 * @test
 * @summary javax.xml.xpath.XPathConstants conformance: xpath constants.
 * @run main XPathConstantsTest
 */


public class XPathConstantsTest {
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
        check(javax.xml.xpath.XPathConstants.NUMBER != null, "NUMBER");
        check(javax.xml.xpath.XPathConstants.STRING != null, "STRING");
        check(javax.xml.xpath.XPathConstants.BOOLEAN != null, "BOOLEAN");
        check(javax.xml.xpath.XPathConstants.NODESET != null, "NODESET");
        check(javax.xml.xpath.XPathConstants.NODE != null, "NODE");
        check("http://java.sun.com/jaxp/xpath/dom".equals(javax.xml.xpath.XPathConstants.DOM_OBJECT_MODEL), "DOM_OBJECT_MODEL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
