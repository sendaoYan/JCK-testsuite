/*
 * @test
 * @summary javax.security.auth.kerberos.ServicePermission conformance: service permission actions.
 * @run main ServicePermissionTest
 */

import javax.security.auth.kerberos.*;

public class ServicePermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        ServicePermission sp = new ServicePermission("krbtgt/EXAMPLE.COM@EXAMPLE.COM", "initiate");
        check("initiate".equals(sp.getActions()), "getActions");
        check("krbtgt/EXAMPLE.COM@EXAMPLE.COM".equals(sp.getName()), "getName");
        check(sp.implies(sp), "implies self");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
