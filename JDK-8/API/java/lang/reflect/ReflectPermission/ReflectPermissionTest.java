/*
 * @test
 * @summary java.lang.reflect.ReflectPermission conformance: name/actions/equals.
 * @run main ReflectPermissionTest
 */

import java.lang.reflect.ReflectPermission;
import java.security.BasicPermission;

public class ReflectPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReflectPermission() {
        ReflectPermission p = new ReflectPermission("suppressAccessChecks");
        check("suppressAccessChecks".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions empty");
        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(new ReflectPermission("suppressAccessChecks")), "equals");
        check(p.hashCode() == new ReflectPermission("suppressAccessChecks").hashCode(), "hashCode");
        ReflectPermission p2 = new ReflectPermission("newProxyInPackage.*");
        check("newProxyInPackage.*".equals(p2.getName()), "wildcard name");
    }

    public static void main(String[] args) {
        testReflectPermission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
