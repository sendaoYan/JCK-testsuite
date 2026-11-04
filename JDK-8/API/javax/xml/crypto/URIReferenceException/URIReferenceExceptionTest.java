/*
 * @test
 * @summary javax.xml.crypto.URIReferenceException conformance: exception hierarchy and constructors.
 * @run main URIReferenceExceptionTest
 */


public class URIReferenceExceptionTest {
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
        check(javax.xml.crypto.URIReferenceException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        javax.xml.crypto.URIReferenceException ue = new javax.xml.crypto.URIReferenceException("msg");
        check(ue.getURIReference() == null, "uri reference null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
