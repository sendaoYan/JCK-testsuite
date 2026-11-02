/*
 * @test
 * @summary javax.security.auth.login.CredentialException conformance: constructors, message, cause and superclass.
 * @run main CredentialExceptionTest
 */

public class CredentialExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.login.CredentialException e = new javax.security.auth.login.CredentialException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof javax.security.auth.login.LoginException, "extends javax.security.auth.login.LoginException");
        javax.security.auth.login.CredentialException e4 = new javax.security.auth.login.CredentialException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
