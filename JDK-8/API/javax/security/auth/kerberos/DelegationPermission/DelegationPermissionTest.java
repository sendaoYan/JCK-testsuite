/*
 * @test
 * @summary javax.security.auth.kerberos.DelegationPermission conformance: constructor, getName, implies.
 * @run main DelegationPermissionTest
 */

public class DelegationPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.kerberos.DelegationPermission p = new javax.security.auth.kerberos.DelegationPermission(
            "\"krbtgt/REALM@REALM\" \"krbtgt/REALM@REALM\"");
        check("".equals(p.getActions()), "getActions empty");
        check(p.implies(p), "implies self");
        check(p.getName().indexOf("krbtgt/REALM@REALM") >= 0, "getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
