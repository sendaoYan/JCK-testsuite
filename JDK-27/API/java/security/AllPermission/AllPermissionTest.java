/*
 * @test
 * @summary java.security.AllPermission conformance: white-box structure + black-box behavior.
 * @run main AllPermissionTest
 */

import java.security.AllPermission;
import java.security.SecurityPermission;
import java.security.PermissionCollection;
import java.security.*;
import java.util.*;

public class AllPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        AllPermission ap = new AllPermission();
        check("<all permissions>".equals(ap.getName()), "getName");
        check("<all actions>".equals(ap.getActions()), "getActions");
        check(ap.implies(new SecurityPermission("a.b")), "implies any permission");
        check(ap.implies(ap), "implies self");
        check(ap.equals(new AllPermission()), "equals");
        PermissionCollection pc = ap.newPermissionCollection();
        pc.add(ap);
        check(pc.implies(new SecurityPermission("x")), "collection implies any");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AllPermission ap = new AllPermission();
        check(ap.getName().equals("<all permissions>"), "name");
        check(ap.getActions().equals("<all actions>"), "actions");
        check(ap.implies(new SecurityPermission("any")) && ap.implies(new PropertyPermission("*", "write")),
            "implies everything");
        check(ap.implies(new AllPermission()), "implies itself");
        check(ap.equals(new AllPermission()), "equals");
        check(ap.hashCode() == new AllPermission().hashCode(), "hashCode consistent");
        check(ap.toString().contains("<all permissions>") && ap.toString().contains("<all actions>"), "toString");
        PermissionCollection pc = ap.newPermissionCollection();
        check(pc != null, "newPermissionCollection non-null");
        pc.add(ap);
        check(pc.implies(new SecurityPermission("x.y")), "collection implies");
        // AllPermissionCollection only accepts AllPermission instances
        try {
            pc.add(new SecurityPermission("only.this"));
            check(false, "non-AllPermission add rejected");
        } catch (IllegalArgumentException iae) {
            check(true, "non-AllPermission add IAE");
        }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
