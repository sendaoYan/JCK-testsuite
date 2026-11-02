/*
 * @test
 * @summary javax.security.auth.kerberos.DelegationPermission conformance: delegation permission actions.
 * @run main DelegationPermissionTest
 */

import javax.security.auth.kerberos.*;

public class DelegationPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        DelegationPermission dp = new DelegationPermission("\"a\" \"b\"");
        check("".equals(dp.getActions()), "empty actions");
        check(dp.getName().startsWith("\"a\""), "getName");
        check(dp.implies(dp), "implies self");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
