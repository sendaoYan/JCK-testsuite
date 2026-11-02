/*
 * @test
 * @summary javax.security.cert.CertificateEncodingException conformance: certificate encoding exception hierarchy.
 * @run main CertificateEncodingExceptionTest
 */

import javax.security.cert.CertificateEncodingException;

public class CertificateEncodingExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CertificateEncodingException e = new CertificateEncodingException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.cert.CertificateException, "extends javax.security.cert.CertificateException");
        check(e instanceof java.lang.Exception, "is java.lang.Exception");
        check(new CertificateEncodingException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
