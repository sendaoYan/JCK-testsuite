/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathType.Filter conformance: xpath type filter constants.
 * @run main FilterTest
 */


public class FilterTest {
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
        check(javax.xml.crypto.dsig.spec.XPathType.Filter.INTERSECT != javax.xml.crypto.dsig.spec.XPathType.Filter.UNION, "distinct");
        check(javax.xml.crypto.dsig.spec.XPathType.Filter.SUBTRACT.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
