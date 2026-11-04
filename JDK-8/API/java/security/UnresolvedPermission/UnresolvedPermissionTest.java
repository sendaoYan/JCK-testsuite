/*
 * @test
 * @summary java.security.UnresolvedPermission conformance: full contract coverage.
 *          constructor (null type -> NPE, null name/actions legal), getUnresolvedType/
 *          getUnresolvedName/getUnresolvedActions/getActions(always "")/getName(== type),
 *          defensive copy of certificates, implies(any) == false, equals/hashCode,
 *          toString, and newPermissionCollection.
 * @run main UnresolvedPermissionTest
 */

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.UnresolvedPermission;
import java.security.cert.Certificate;
import java.util.Arrays;

public class UnresolvedPermissionTest {
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

    public static void main(String[] args) throws Exception {
        UnresolvedPermission up = new UnresolvedPermission("java.io.FilePermission", "read", "read", null);

        // ---- getters ----
        check("java.io.FilePermission".equals(up.getUnresolvedType()), "getUnresolvedType");
        check("read".equals(up.getUnresolvedName()), "getUnresolvedName");
        check("read".equals(up.getUnresolvedActions()), "getUnresolvedActions");
        check("".equals(up.getActions()), "getActions always empty");
        check("java.io.FilePermission".equals(up.getName()), "getName == type (super(type))");
        check(up.getUnresolvedCerts() == null, "getUnresolvedCerts null when null passed");

        // ---- null type -> NPE, null name/actions legal ----
        checkThrows(NullPointerException.class,
                () -> new UnresolvedPermission(null, "n", "a", null), "null type");
        UnresolvedPermission nullName = new UnresolvedPermission("t", null, "a", null);
        check(nullName.getUnresolvedName() == null, "null name legal -> null unresolved name");
        UnresolvedPermission nullActions = new UnresolvedPermission("t", "n", null, null);
        check(nullActions.getUnresolvedActions() == null, "null actions legal -> null unresolved actions");

        // ---- defensive copy of certs ----
        Certificate[] certs = new Certificate[0];
        UnresolvedPermission upCerts = new UnresolvedPermission("t", "n", "a", certs);
        check(upCerts.getUnresolvedCerts() != null && upCerts.getUnresolvedCerts() != certs,
                "getUnresolvedCerts defensive copy");

        // ---- implies always false ----
        check(!up.implies(new java.security.AllPermission()), "implies always false");

        // ---- equals/hashCode ----
        UnresolvedPermission up2 = new UnresolvedPermission("java.io.FilePermission", "read", "read", null);
        check(up.equals(up2), "equals equal fields");
        check(up.hashCode() == up2.hashCode(), "hashCode equal");
        check(!up.equals(new UnresolvedPermission("java.io.FilePermission", "write", "read", null)),
                "equals different name false");

        // ---- toString ----
        String s = up.toString();
        check(s.contains("unresolved") && s.contains("java.io.FilePermission"), "toString contains type");
        check(s.contains("read"), "toString contains name/actions");

        // ---- newPermissionCollection ----
        PermissionCollection pc = up.newPermissionCollection();
        check(pc != null, "newPermissionCollection non-null");
        check(!pc.implies(up), "empty collection implies false");
        pc.add(up);
        check(!pc.implies(up), "unresolved permission never implies (even after add)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
