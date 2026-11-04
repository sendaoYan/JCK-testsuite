/*
 * @test
 * @summary java.lang.RuntimePermission API conformance: name/actions and inheritance.
 * @run main RuntimePermissionTest
 */

import java.security.BasicPermission;
import java.lang.RuntimePermission;

public class RuntimePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRuntimePermission() {
        RuntimePermission p = new RuntimePermission("getClassLoader");
        check(p instanceof BasicPermission, "extends BasicPermission");
        check("getClassLoader".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions is empty");
    }

    public static void main(String[] args) {
        testRuntimePermission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
