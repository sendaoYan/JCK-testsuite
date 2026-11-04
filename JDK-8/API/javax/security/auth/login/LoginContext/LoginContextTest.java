/*
 * @test
 * @summary javax.security.auth.login.LoginContext conformance: constructor throws LoginException for unknown app.
 * @run main LoginContextTest
 */

public class LoginContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        boolean threw = false;
        try { new javax.security.auth.login.LoginContext("__jck_no_such_jaas_app__"); }
        catch (javax.security.auth.login.LoginException expected) { threw = true; }
        check(threw, "LoginContext(unknown) throws LoginException");
        check(javax.security.auth.login.LoginException.class.getSuperclass() == java.security.GeneralSecurityException.class,
            "LoginException extends GeneralSecurityException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
