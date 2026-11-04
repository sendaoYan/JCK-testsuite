/*
 * @test
 * @summary java.util.PropertyPermission API conformance: name/actions and inheritance.
 * @run main PropertyPermissionTest
 */

import java.security.BasicPermission;
import java.util.PropertyPermission;

public class PropertyPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPropertyPermission() {
        PropertyPermission p = new PropertyPermission("java.home", "read");
        check(p instanceof BasicPermission, "extends BasicPermission");
        check("java.home".equals(p.getName()), "getName");
        check("read".equals(p.getActions()), "getActions");
    }

    public static void main(String[] args) {
        testPropertyPermission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
