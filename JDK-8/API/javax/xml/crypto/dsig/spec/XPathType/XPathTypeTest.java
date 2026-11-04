/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathType conformance: xpath type.
 * @run main XPathTypeTest
 */


public class XPathTypeTest {
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
        javax.xml.crypto.dsig.spec.XPathType t = new javax.xml.crypto.dsig.spec.XPathType("//x", javax.xml.crypto.dsig.spec.XPathType.Filter.INTERSECT);
        check("//x".equals(t.getExpression()), "expression");
        check(t.getFilter() == javax.xml.crypto.dsig.spec.XPathType.Filter.INTERSECT, "filter");
        check(t.getNamespaceMap() != null, "ns map non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
