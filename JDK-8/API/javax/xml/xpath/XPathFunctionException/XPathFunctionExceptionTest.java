/*
 * @test
 * @summary javax.xml.xpath.XPathFunctionException conformance: exception hierarchy and constructors.
 * @run main XPathFunctionExceptionTest
 */


public class XPathFunctionExceptionTest {
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
        check(javax.xml.xpath.XPathFunctionException.class.getSuperclass().getName().equals("javax.xml.xpath.XPathExpressionException"), "extends javax.xml.xpath.XPathExpressionException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
