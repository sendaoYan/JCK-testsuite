/*
 * @test
 * @summary javax.security.auth.login.AccountNotFoundException conformance: account not found exception hierarchy.
 * @run main AccountNotFoundExceptionTest
 */

import javax.security.auth.login.AccountNotFoundException;

public class AccountNotFoundExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AccountNotFoundException e = new AccountNotFoundException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.auth.login.AccountException, "extends javax.security.auth.login.AccountException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new AccountNotFoundException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
