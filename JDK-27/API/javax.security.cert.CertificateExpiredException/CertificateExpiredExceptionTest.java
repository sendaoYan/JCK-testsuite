/*
 * @test
 * @summary javax.security.cert.CertificateExpiredException conformance: certificate expired exception hierarchy.
 * @run main CertificateExpiredExceptionTest
 */

import javax.security.cert.CertificateExpiredException;

public class CertificateExpiredExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CertificateExpiredException e = new CertificateExpiredException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.cert.CertificateException, "extends javax.security.cert.CertificateException");
        check(e instanceof java.lang.Exception, "is java.lang.Exception");
        check(new CertificateExpiredException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
