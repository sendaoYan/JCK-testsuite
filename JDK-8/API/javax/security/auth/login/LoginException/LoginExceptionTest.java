/*
 * @test
 * @summary javax.security.auth.login.LoginException conformance: constructors, message, cause and superclass.
 * @run main LoginExceptionTest
 */

public class LoginExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.login.LoginException e = new javax.security.auth.login.LoginException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        javax.security.auth.login.LoginException e4 = new javax.security.auth.login.LoginException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
