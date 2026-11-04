/*
 * @test
 * @summary javax.security.auth.AuthPermission conformance: constructors, getName, getActions, implies.
 * @run main AuthPermissionTest
 */

public class AuthPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.AuthPermission p = new javax.security.auth.AuthPermission("doAs");
        check("doAs".equals(p.getName()), "getName");
        check(p.implies(p), "implies self");
        javax.security.auth.AuthPermission p2 = new javax.security.auth.AuthPermission("doAs", "read");
        check("".equals(p2.getActions()), "getActions empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
