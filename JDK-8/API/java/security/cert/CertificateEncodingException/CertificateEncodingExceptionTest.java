/*
 * @test
 * @summary java.security.cert.CertificateEncodingException conformance: constructors, message, cause and superclass.
 * @run main CertificateEncodingExceptionTest
 */

public class CertificateEncodingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.cert.CertificateEncodingException e = new java.security.cert.CertificateEncodingException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.cert.CertificateException, "extends java.security.cert.CertificateException");
        Throwable cau = new Exception("cause");
        java.security.cert.CertificateEncodingException e2 = new java.security.cert.CertificateEncodingException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.cert.CertificateEncodingException e3 = new java.security.cert.CertificateEncodingException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.cert.CertificateEncodingException e4 = new java.security.cert.CertificateEncodingException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
