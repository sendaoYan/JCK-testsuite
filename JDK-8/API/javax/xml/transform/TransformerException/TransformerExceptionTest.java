/*
 * @test
 * @summary javax.xml.transform.TransformerException conformance: exception hierarchy and constructors.
 * @run main TransformerExceptionTest
 */


public class TransformerExceptionTest {
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
        check(javax.xml.transform.TransformerException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        javax.xml.transform.TransformerException te = new javax.xml.transform.TransformerException("msg");
        check(te.getLocator() == null, "locator null");
        check(te.getException() == null, "exception null");
        check(te.getMessageAndLocation() != null, "message and location");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
