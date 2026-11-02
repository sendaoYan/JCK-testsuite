/*
 * @test
 * @summary javax.security.auth.login.AccountLockedException conformance: account locked exception hierarchy.
 * @run main AccountLockedExceptionTest
 */

import javax.security.auth.login.AccountLockedException;

public class AccountLockedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AccountLockedException e = new AccountLockedException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.auth.login.AccountException, "extends javax.security.auth.login.AccountException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new AccountLockedException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
