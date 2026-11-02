/*
 * @test
 * @summary javax.security.auth.login.CredentialException conformance: credential exception hierarchy.
 * @run main CredentialExceptionTest
 */

import javax.security.auth.login.CredentialException;

public class CredentialExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CredentialException e = new CredentialException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.auth.login.LoginException, "extends javax.security.auth.login.LoginException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new CredentialException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
