/*
 * @test
 * @summary javax.security.auth.login.CredentialExpiredException conformance: credential expired exception hierarchy.
 * @run main CredentialExpiredExceptionTest
 */

import javax.security.auth.login.CredentialExpiredException;

public class CredentialExpiredExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CredentialExpiredException e = new CredentialExpiredException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.auth.login.CredentialException, "extends javax.security.auth.login.CredentialException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new CredentialExpiredException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
