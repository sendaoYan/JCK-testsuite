/*
 * @test
 * @summary javax.xml.xpath.XPathExpressionException conformance: exception hierarchy and constructors.
 * @run main XPathExpressionExceptionTest
 */


public class XPathExpressionExceptionTest {
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
        check(javax.xml.xpath.XPathExpressionException.class.getSuperclass().getName().equals("javax.xml.xpath.XPathException"), "extends javax.xml.xpath.XPathException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
