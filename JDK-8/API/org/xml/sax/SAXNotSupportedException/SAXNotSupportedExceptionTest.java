/*
 * @test
 * @summary org.xml.sax.SAXNotSupportedException conformance: exception hierarchy and constructors.
 * @run main SAXNotSupportedExceptionTest
 */


public class SAXNotSupportedExceptionTest {
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
        check(org.xml.sax.SAXNotSupportedException.class.getSuperclass().getName().equals("org.xml.sax.SAXException"), "extends org.xml.sax.SAXException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
