/*
 * @test
 * @summary javax.security.auth.login.FailedLoginException conformance: failed login exception hierarchy.
 * @run main FailedLoginExceptionTest
 */

import javax.security.auth.login.FailedLoginException;

public class FailedLoginExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        FailedLoginException e = new FailedLoginException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.auth.login.LoginException, "extends javax.security.auth.login.LoginException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new FailedLoginException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
