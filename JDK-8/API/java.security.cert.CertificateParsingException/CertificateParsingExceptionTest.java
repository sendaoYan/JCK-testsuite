/*
 * @test
 * @summary java.security.cert.CertificateParsingException conformance: constructors, message, cause and superclass.
 * @run main CertificateParsingExceptionTest
 */

public class CertificateParsingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.cert.CertificateParsingException e = new java.security.cert.CertificateParsingException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.cert.CertificateException, "extends java.security.cert.CertificateException");
        Throwable cau = new Exception("cause");
        java.security.cert.CertificateParsingException e2 = new java.security.cert.CertificateParsingException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.cert.CertificateParsingException e3 = new java.security.cert.CertificateParsingException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.cert.CertificateParsingException e4 = new java.security.cert.CertificateParsingException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
