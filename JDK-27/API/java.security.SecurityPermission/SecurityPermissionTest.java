/*
 * @test
 * @summary java.security.SecurityPermission conformance: constructors, getName, getActions, implies.
 * @run main SecurityPermissionTest
 */

import java.security.SecurityPermission;

public class SecurityPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SecurityPermission sp = new SecurityPermission("createAccessControlContext");
        check("createAccessControlContext".equals(sp.getName()), "getName");
        check("".equals(sp.getActions()), "getActions");
        check(sp.implies(new SecurityPermission("createAccessControlContext")), "implies");
        SecurityPermission sp2 = new SecurityPermission("setPolicy", "write");
        check("setPolicy".equals(sp2.getName()), "getName with actions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
