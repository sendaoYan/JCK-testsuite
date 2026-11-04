/*
 * @test
 * @summary javax.security.auth.login.AccountException conformance: constructors, message, cause and superclass.
 * @run main AccountExceptionTest
 */

public class AccountExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.login.AccountException e = new javax.security.auth.login.AccountException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof javax.security.auth.login.LoginException, "extends javax.security.auth.login.LoginException");
        javax.security.auth.login.AccountException e4 = new javax.security.auth.login.AccountException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
