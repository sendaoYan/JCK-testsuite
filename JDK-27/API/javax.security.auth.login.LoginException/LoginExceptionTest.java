/*
 * @test
 * @summary javax.security.auth.login.LoginException conformance: login exception hierarchy.
 * @run main LoginExceptionTest
 */

import javax.security.auth.login.LoginException;

public class LoginExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        LoginException e = new LoginException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new LoginException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
