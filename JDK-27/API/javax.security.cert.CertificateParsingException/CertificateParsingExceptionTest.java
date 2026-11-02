/*
 * @test
 * @summary javax.security.cert.CertificateParsingException conformance: certificate parsing exception hierarchy.
 * @run main CertificateParsingExceptionTest
 */

import javax.security.cert.CertificateParsingException;

public class CertificateParsingExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CertificateParsingException e = new CertificateParsingException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.cert.CertificateException, "extends javax.security.cert.CertificateException");
        check(e instanceof java.lang.Exception, "is java.lang.Exception");
        check(new CertificateParsingException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
