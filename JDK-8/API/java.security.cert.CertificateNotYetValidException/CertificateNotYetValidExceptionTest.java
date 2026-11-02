/*
 * @test
 * @summary java.security.cert.CertificateNotYetValidException conformance: constructors, message, cause and superclass.
 * @run main CertificateNotYetValidExceptionTest
 */

public class CertificateNotYetValidExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.cert.CertificateNotYetValidException e = new java.security.cert.CertificateNotYetValidException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.cert.CertificateException, "extends java.security.cert.CertificateException");
        java.security.cert.CertificateNotYetValidException e4 = new java.security.cert.CertificateNotYetValidException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
