/*
 * @test
 * @summary javax.security.auth.PrivateCredentialPermission conformance: private credential permission syntax.
 * @run main PrivateCredentialPermissionTest
 */

import javax.security.auth.*;

public class PrivateCredentialPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        PrivateCredentialPermission p = new PrivateCredentialPermission("javax.security.auth.x500.X500PrivateCredential javax.security.auth.x500.X500Principal \"CN=Alice\"", "read");
        check("javax.security.auth.x500.X500PrivateCredential".equals(p.getCredentialClass()), "getCredentialClass");
        check("read".equals(p.getActions()), "getActions");
        String[][] ps = p.getPrincipals();
        check(ps.length == 1 && ps[0].length == 2 && "javax.security.auth.x500.X500Principal".equals(ps[0][0]) && "CN=Alice".equals(ps[0][1]), "getPrincipals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
