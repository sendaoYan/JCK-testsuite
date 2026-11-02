/*
 * @test
 * @summary javax.security.cert.CertificateNotYetValidException conformance: certificate not yet valid exception hierarchy.
 * @run main CertificateNotYetValidExceptionTest
 */

import javax.security.cert.CertificateNotYetValidException;

public class CertificateNotYetValidExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CertificateNotYetValidException e = new CertificateNotYetValidException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.cert.CertificateException, "extends javax.security.cert.CertificateException");
        check(e instanceof java.lang.Exception, "is java.lang.Exception");
        check(new CertificateNotYetValidException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
