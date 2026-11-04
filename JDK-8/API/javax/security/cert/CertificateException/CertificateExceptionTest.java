/*
 * @test
 * @summary javax.security.cert.CertificateException conformance: constructors, message, cause and superclass.
 * @run main CertificateExceptionTest
 */

public class CertificateExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.cert.CertificateException e = new javax.security.cert.CertificateException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        javax.security.cert.CertificateException e4 = new javax.security.cert.CertificateException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
