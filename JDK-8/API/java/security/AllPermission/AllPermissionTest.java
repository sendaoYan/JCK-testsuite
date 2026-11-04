/*
 * @test
 * @summary java.security.AllPermission conformance: full contract coverage.
 *          no-arg and (name,actions) constructors, getName/getActions sentinel
 *          values, implies(any permission) == true, equals/hashCode across
 *          instances, toString, and newPermissionCollection add/implies.
 * @run main AllPermissionTest
 */

import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;

public class AllPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class BP extends java.security.BasicPermission {
        BP(String name) { super(name); }
    }

    public static void main(String[] args) throws Exception {
        AllPermission ap = new AllPermission();
        AllPermission ap2 = new AllPermission("ignored", "ignored");

        check("<all permissions>".equals(ap.getName()), "getName sentinel");
        check("<all actions>".equals(ap.getActions()), "getActions sentinel");
        check("<all permissions>".equals(ap2.getName()), "(name,actions) getName sentinel");

        // ---- implies any permission ----
        check(ap.implies(new BP("a.b.c")), "implies BasicPermission true");
        check(ap.implies(new java.security.SecurityPermission("a.b")), "implies SecurityPermission true");
        check(ap.implies(ap2), "implies AllPermission true");

        // ---- equals/hashCode ----
        check(ap.equals(ap2), "AllPermission equals AllPermission");
        check(ap.hashCode() == ap2.hashCode(), "AllPermission hashCode equal");
        check(!ap.equals(new BP("x")), "AllPermission not equal other permission");

        // ---- toString ----
        check(ap.toString().contains("AllPermission"), "toString contains AllPermission");
        check(ap.toString().contains("<all permissions>"), "toString contains name");
        check(ap.toString().contains("<all actions>"), "toString contains actions");

        // ---- newPermissionCollection ----
        PermissionCollection pc = ap.newPermissionCollection();
        check(pc != null, "newPermissionCollection non-null");
        check(!pc.implies(new BP("x")), "empty collection implies false");
        pc.add(ap);
        check(pc.implies(new BP("x")), "collection implies anything after add");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
