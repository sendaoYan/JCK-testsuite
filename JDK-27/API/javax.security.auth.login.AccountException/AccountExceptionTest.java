/*
 * @test
 * @summary javax.security.auth.login.AccountException conformance: account exception hierarchy.
 * @run main AccountExceptionTest
 */

import javax.security.auth.login.AccountException;

public class AccountExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AccountException e = new AccountException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.auth.login.LoginException, "extends javax.security.auth.login.LoginException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new AccountException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
