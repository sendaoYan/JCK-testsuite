/*
 * @test
 * @summary javax.xml.xpath.XPathException conformance: exception hierarchy and constructors.
 * @run main XPathExceptionTest
 */


public class XPathExceptionTest {
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
        check(javax.xml.xpath.XPathException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
