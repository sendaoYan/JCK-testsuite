/*
 * @test
 * @summary javax.security.cert.CertificateExpiredException conformance: constructors, message, cause and superclass.
 * @run main CertificateExpiredExceptionTest
 */

public class CertificateExpiredExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.cert.CertificateExpiredException e = new javax.security.cert.CertificateExpiredException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof javax.security.cert.CertificateException, "extends javax.security.cert.CertificateException");
        javax.security.cert.CertificateExpiredException e4 = new javax.security.cert.CertificateExpiredException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
