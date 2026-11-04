/*
 * @test
 * @summary java.security.cert.CertificateException conformance: constructors, message, cause and superclass.
 * @run main CertificateExceptionTest
 */

public class CertificateExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.cert.CertificateException e = new java.security.cert.CertificateException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.cert.CertificateException e2 = new java.security.cert.CertificateException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.cert.CertificateException e3 = new java.security.cert.CertificateException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.cert.CertificateException e4 = new java.security.cert.CertificateException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
