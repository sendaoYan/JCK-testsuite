/*
 * @test
 * @summary java.security.UnresolvedPermission conformance: constructors and getters.
 * @run main UnresolvedPermissionTest
 */

import java.security.UnresolvedPermission;

public class UnresolvedPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        UnresolvedPermission up = new UnresolvedPermission("java.io.FilePermission", "read", "read", null);
        check("java.io.FilePermission".equals(up.getUnresolvedType()), "getUnresolvedType");
        check("read".equals(up.getUnresolvedName()), "getUnresolvedName");
        check("read".equals(up.getUnresolvedActions()), "getUnresolvedActions");
        check(up.getUnresolvedCerts() == null, "getUnresolvedCerts null");
        check("".equals(up.getActions()), "getActions empty");
        UnresolvedPermission up2 = new UnresolvedPermission("java.io.FilePermission", "read", "read", null);
        check(up.equals(up2), "equals");
        check(up.hashCode() == up2.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
