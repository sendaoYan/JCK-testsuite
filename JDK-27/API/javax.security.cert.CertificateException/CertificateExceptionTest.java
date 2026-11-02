/*
 * @test
 * @summary javax.security.cert.CertificateException conformance: deprecated certificate exception hierarchy.
 * @run main CertificateExceptionTest
 */

import javax.security.cert.CertificateException;

public class CertificateExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CertificateException e = new CertificateException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        check(e instanceof java.lang.Exception, "is java.lang.Exception");
        check(new CertificateException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
