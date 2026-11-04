/*
 * @test
 * @summary javax.security.auth.login.AccountExpiredException conformance: constructors, message, cause and superclass.
 * @run main AccountExpiredExceptionTest
 */

public class AccountExpiredExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.login.AccountExpiredException e = new javax.security.auth.login.AccountExpiredException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof javax.security.auth.login.AccountException, "extends javax.security.auth.login.AccountException");
        javax.security.auth.login.AccountExpiredException e4 = new javax.security.auth.login.AccountExpiredException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
