/*
 * @test
 * @summary java.security.AllPermission conformance: implies, getActions, newPermissionCollection, equals.
 * @run main AllPermissionTest
 */

import java.security.AllPermission;
import java.security.SecurityPermission;
import java.security.PermissionCollection;

public class AllPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AllPermission ap = new AllPermission();
        check("<all permissions>".equals(ap.getName()), "getName");
        check("<all actions>".equals(ap.getActions()), "getActions");
        check(ap.implies(new SecurityPermission("a.b")), "implies any permission");
        check(ap.implies(ap), "implies self");
        check(ap.equals(new AllPermission()), "equals");
        PermissionCollection pc = ap.newPermissionCollection();
        pc.add(ap);
        check(pc.implies(new SecurityPermission("x")), "collection implies any");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
