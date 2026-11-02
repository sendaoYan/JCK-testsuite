/*
 * @test
 * @summary javax.security.auth.login.AccountExpiredException conformance: account expired exception hierarchy.
 * @run main AccountExpiredExceptionTest
 */

import javax.security.auth.login.AccountExpiredException;

public class AccountExpiredExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AccountExpiredException e = new AccountExpiredException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.auth.login.AccountException, "extends javax.security.auth.login.AccountException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new AccountExpiredException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
