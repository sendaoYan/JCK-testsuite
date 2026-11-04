/*
 * @test
 * @summary java.security.Permissions conformance: full contract coverage.
 *          add/implies/elements across multiple permission classes, add(null) -> NPE,
 *          implies(null) -> NPE, deduplication of identical permissions, and
 *          PermissionCollection subtype relationship.
 * @run main PermissionsTest
 */

import java.security.AllPermission;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PermissionsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static class BP extends BasicPermission {
        BP(String name) { super(name); }
    }

    public static void main(String[] args) throws Exception {
        // ---- subtype ----
        check(PermissionCollection.class.isAssignableFrom(Permissions.class),
                "Permissions extends PermissionCollection");

        // ---- add + implies ----
        Permissions perms = new Permissions();
        check(!perms.implies(new BP("a.b")), "empty implies false");
        perms.add(new BP("a.b"));
        check(perms.implies(new BP("a.b")), "implies added BasicPermission");
        check(!perms.implies(new BP("a.c")), "does not imply other name");

        // multiple permission classes
        perms.add(new AllPermission());
        check(perms.implies(new BP("anything")), "AllPermission implies everything");

        // ---- elements enumeration ----
        Permissions p2 = new Permissions();
        p2.add(new BP("x"));
        p2.add(new java.security.SecurityPermission("y"));
        Set<String> names = new HashSet<>();
        for (Permission p : Collections.list(p2.elements())) names.add(p.getName());
        check(names.contains("x") && names.contains("y"), "elements enumerates all");

        // ---- deduplication ----
        Permissions p3 = new Permissions();
        p3.add(new BP("dup"));
        p3.add(new BP("dup"));
        check(Collections.list(p3.elements()).size() == 1, "identical permissions deduplicated");

        // ---- null handling ----
        final Permissions pn = new Permissions();
        checkThrows(NullPointerException.class, () -> pn.add((Permission) null), "add(null)");
        checkThrows(NullPointerException.class, () -> pn.implies(null), "implies(null)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
