/*
 * @test
 * @summary javax.security.auth.PrivateCredentialPermission conformance: constructor and getters.
 * @run main PrivateCredentialPermissionTest
 */

public class PrivateCredentialPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.PrivateCredentialPermission p = new javax.security.auth.PrivateCredentialPermission(
            "javax.security.auth.Subject javax.security.auth.x500.X500Principal \"CN=Test\"", "read");
        check("javax.security.auth.Subject".equals(p.getCredentialClass()), "getCredentialClass");
        check("read".equals(p.getActions()), "getActions");
        check(p.implies(p), "implies self");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
