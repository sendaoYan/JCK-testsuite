/*
 * @test
 * @summary javax.security.cert.CertificateParsingException conformance: constructors, message, cause and superclass.
 * @run main CertificateParsingExceptionTest
 */

public class CertificateParsingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.cert.CertificateParsingException e = new javax.security.cert.CertificateParsingException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof javax.security.cert.CertificateException, "extends javax.security.cert.CertificateException");
        javax.security.cert.CertificateParsingException e4 = new javax.security.cert.CertificateParsingException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
