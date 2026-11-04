/*
 * @test
 * @summary java.lang.management.ManagementPermission conformance: name/actions/equals.
 * @run main ManagementPermissionTest
 */

import java.lang.management.ManagementPermission;
import java.security.BasicPermission;

public class ManagementPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testManagementPermission() {
        ManagementPermission p = new ManagementPermission("monitor");
        check("monitor".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions");
        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(new ManagementPermission("monitor")), "equals monitor");
        check(!p.equals(new ManagementPermission("control")), "monitor != control");
    }

    public static void main(String[] args) {
        testManagementPermission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
