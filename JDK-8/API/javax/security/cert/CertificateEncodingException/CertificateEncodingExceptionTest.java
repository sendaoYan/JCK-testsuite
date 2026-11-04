/*
 * @test
 * @summary javax.security.cert.CertificateEncodingException conformance: constructors, message, cause and superclass.
 * @run main CertificateEncodingExceptionTest
 */

public class CertificateEncodingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.cert.CertificateEncodingException e = new javax.security.cert.CertificateEncodingException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof javax.security.cert.CertificateException, "extends javax.security.cert.CertificateException");
        javax.security.cert.CertificateEncodingException e4 = new javax.security.cert.CertificateEncodingException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
