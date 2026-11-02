/*
 * @test
 * @summary javax.security.auth.login.CredentialExpiredException conformance: constructors, message, cause and superclass.
 * @run main CredentialExpiredExceptionTest
 */

public class CredentialExpiredExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.login.CredentialExpiredException e = new javax.security.auth.login.CredentialExpiredException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof javax.security.auth.login.CredentialException, "extends javax.security.auth.login.CredentialException");
        javax.security.auth.login.CredentialExpiredException e4 = new javax.security.auth.login.CredentialExpiredException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
