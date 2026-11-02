/*
 * @test
 * @summary javax.security.auth.login.CredentialNotFoundException conformance: credential not found exception hierarchy.
 * @run main CredentialNotFoundExceptionTest
 */

import javax.security.auth.login.CredentialNotFoundException;

public class CredentialNotFoundExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CredentialNotFoundException e = new CredentialNotFoundException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.auth.login.CredentialException, "extends javax.security.auth.login.CredentialException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new CredentialNotFoundException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
