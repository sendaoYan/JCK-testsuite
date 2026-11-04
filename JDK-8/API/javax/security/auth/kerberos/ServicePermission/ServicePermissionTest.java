/*
 * @test
 * @summary javax.security.auth.kerberos.ServicePermission conformance: constructor and implies.
 * @run main ServicePermissionTest
 */

public class ServicePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.kerberos.ServicePermission p = new javax.security.auth.kerberos.ServicePermission(
            "krbtgt/REALM@REALM", "initiate");
        check("initiate".equals(p.getActions()), "getActions");
        check(p.implies(new javax.security.auth.kerberos.ServicePermission("krbtgt/REALM@REALM", "initiate")), "implies");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
