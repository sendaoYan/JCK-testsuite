/*
 * @test
 * @summary org.xml.sax.SAXException conformance: exception hierarchy and constructors.
 * @run main SAXExceptionTest
 */


public class SAXExceptionTest {
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
        check(org.xml.sax.SAXException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        org.xml.sax.SAXException se = new org.xml.sax.SAXException("msg");
        check(se.getException() == null, "nested exception null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
