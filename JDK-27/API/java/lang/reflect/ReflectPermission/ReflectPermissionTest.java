/*
 * @test
 * @summary java.lang.reflect.ReflectPermission conformance: white-box structure + black-box behavior.
 * @run main ReflectPermissionTest
 */

import java.lang.reflect.ReflectPermission;
import java.security.BasicPermission;
import java.security.Permission;

public class ReflectPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        ReflectPermission p = new ReflectPermission("suppressAccessChecks");
        check("suppressAccessChecks".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions empty");
        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(new ReflectPermission("suppressAccessChecks")), "equals");
        check(p.hashCode() == new ReflectPermission("suppressAccessChecks").hashCode(), "hashCode");
        ReflectPermission p2 = new ReflectPermission("newProxyInPackage.*");
        check("newProxyInPackage.*".equals(p2.getName()), "wildcard name");
        check(BasicPermission.class.isAssignableFrom(ReflectPermission.class),
              "extends BasicPermission");
        check(java.security.Permission.class.isAssignableFrom(ReflectPermission.class),
              "extends Permission");
        check(!ReflectPermissionTest.class.getName().isEmpty(), "loaded");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. BasicPermission implies lattice for known reflect targets
        Permission suppress = new ReflectPermission("suppressAccessChecks");
        check(suppress.implies(suppress), "self implies");
        check(!suppress.implies(new ReflectPermission("newProxyInPackage.java.lang")),
              "different name not implied");
        check(new ReflectPermission("*").implies(suppress), "wildcard * implies specific");
        check(new ReflectPermission("suppressAccessChecks").implies(new ReflectPermission("*"))
              == false, "specific does not imply *");

        // 2. hierarchical names
        Permission pkg = new ReflectPermission("newProxyInPackage.java.lang");
        check(new ReflectPermission("newProxyInPackage.*").implies(pkg), "package wildcard implies");
        check(!pkg.implies(new ReflectPermission("newProxyInPackage.java.util")),
              "sibling not implied");
        check(pkg.implies(pkg), "self implies hierarchical");

        // 3. two-arg constructor ignores actions in comparisons
        ReflectPermission a1 = new ReflectPermission("suppressAccessChecks");
        ReflectPermission a2 = new ReflectPermission("suppressAccessChecks", "read");
        check(a1.equals(a2), "actions ignored for equals");
        check(a1.implies(a2) && a2.implies(a1), "actions ignored for implies");
        check("".equals(a1.getActions()), "one-arg actions empty");
        check("".equals(a2.getActions()), "BasicPermission normalizes actions to empty");

        // 4. equals/hashCode contract
        check(a1.equals(new ReflectPermission("suppressAccessChecks")), "equals same name");
        check(!a1.equals(new ReflectPermission("other")), "different names unequal");
        check(!a1.equals(null) && !a1.equals("suppressAccessChecks"), "foreign types unequal");
        check(a1.hashCode() == a2.hashCode(), "hashCode consistent with equals");

        // 5. name validation (null / empty rejected)
        try {
            new ReflectPermission(null);
            check(false, "null name must fail");
        } catch (NullPointerException e) {
            check(true, "NPE null name");
        }
        try {
            new ReflectPermission("");
            check(false, "empty name must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IAE empty name");
        }

        // 6. permission collection behavior
        java.security.PermissionCollection pc = a1.newPermissionCollection();
        pc.add(new ReflectPermission("newProxyInPackage.java.util"));
        check(pc.implies(new ReflectPermission("newProxyInPackage.java.util")),
              "collection implies added");
        check(!pc.implies(a1), "collection does not imply unadded");
        pc.add(new ReflectPermission("*"));
        check(pc.implies(a1), "collection wildcard covers");

        // 7. toString rendering
        check(a1.toString() != null && a1.toString().contains("suppressAccessChecks"),
              "toString contains name");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
