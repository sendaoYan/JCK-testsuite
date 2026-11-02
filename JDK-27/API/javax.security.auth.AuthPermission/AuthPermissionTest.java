/*
 * @test
 * @summary javax.security.auth.AuthPermission conformance: auth permission actions.
 * @run main AuthPermissionTest
 */

import javax.security.auth.*;

public class AuthPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AuthPermission p = new AuthPermission("doAs");
        check("doAs".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "empty actions");
        AuthPermission p2 = new AuthPermission("doAs", null);
        check("".equals(p2.getActions()), "two-arg empty actions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
