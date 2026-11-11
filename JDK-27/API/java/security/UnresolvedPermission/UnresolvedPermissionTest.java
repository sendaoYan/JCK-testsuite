/*
 * @test
 * @summary java.security.UnresolvedPermission conformance: full contract coverage.
 *          constructor (null type -> NPE, null name/actions legal), getUnresolvedType/
 *          getUnresolvedName/getUnresolvedActions/getActions(always "")/getName(== type),
 *          defensive copy of certificates, implies(any) == false, equals/hashCode,
 *          toString, and newPermissionCollection.
 * @run main/othervm UnresolvedPermissionTest
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

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: exact messages, resolution matrix, serialization, toString form ----
    static void testContractDeep() throws Exception {
        // null type precise message
        try {
            new UnresolvedPermission(null, "n", "a", null);
        } catch (NullPointerException npe) {
            check(npe.getMessage() != null && npe.getMessage().contains("type"), "null type message mentions type");
        }

        // implies(null) is false, not NPE (UP never implies anything)
        check(!new UnresolvedPermission("t", "n", null, null).implies(null), "implies(null) false");

        // equals partitions on each field
        UnresolvedPermission base = new UnresolvedPermission("T", "n", "a", null);
        check(!base.equals(new UnresolvedPermission("T2", "n", "a", null)), "equals different type false");
        check(!base.equals(new UnresolvedPermission("T", "n2", "a", null)), "equals different name false");
        check(!base.equals(new UnresolvedPermission("T", "n", "a2", null)), "equals different actions false");
        check(base.equals(base), "equals reflexive");
        check(!base.equals("T"), "equals non-Permission false");
        // certs participate in equality (null vs empty array are different)
        check(!base.equals(new UnresolvedPermission("T", "n", "a", new Certificate[0])),
                "null certs != empty certs");

        // toString exact shape: (unresolved <type> <name> <actions>)
        String ts = new UnresolvedPermission("java.io.FilePermission", "read", "write", null).toString();
        check(ts.contains("(unresolved java.io.FilePermission read write)"), "toString exact shape");

        // resolution contract through Permissions: an UnresolvedPermission whose type names an
        // existing class is resolved lazily and implies ONLY the matching queried name
        java.security.Permissions ps = new java.security.Permissions();
        ps.add(new UnresolvedPermission("java.security.SecurityPermission", "getPolicy", null, null));
        check(ps.implies(new java.security.SecurityPermission("getPolicy")), "resolved implies matching name");
        check(!ps.implies(new java.security.SecurityPermission("setLog")), "no implication for other name");
        ps.add(new UnresolvedPermission("com.bogus.NoSuchClass", "x", null, null));
        check(!ps.implies(new java.security.SecurityPermission("x")), "unresolvable type never implies");
        // direct UnresolvedPermissionCollection never implies (no Permissions-level resolution)
        PermissionCollection upc = new UnresolvedPermission("java.security.SecurityPermission", "getPolicy", null, null)
                .newPermissionCollection();
        upc.add(new UnresolvedPermission("java.security.SecurityPermission", "getPolicy", null, null));
        check(!upc.implies(new java.security.SecurityPermission("getPolicy")),
                "direct UPC implies false without Permissions machinery");
        // UP collections do not enforce read-only on add (unlike BasicPermission collections)
        upc.setReadOnly();
        upc.add(new UnresolvedPermission("java.security.SecurityPermission", "setLog", null, null));
        check(true, "UPC add after setReadOnly does not throw");

        // newPermissionCollection yields the UP-specific collection type
        check("java.security.UnresolvedPermissionCollection".equals(
                new UnresolvedPermission("t", "n", null, null).newPermissionCollection().getClass().getName()),
                "collection class UnresolvedPermissionCollection");

        // serialization round-trip preserves all four fields (certs null path)
        UnresolvedPermission rt = (UnresolvedPermission) roundTrip(
                new UnresolvedPermission("java.io.FilePermission", "read", "write", null));
        check("java.io.FilePermission".equals(rt.getUnresolvedType()), "round-trip type");
        check("read".equals(rt.getUnresolvedName()), "round-trip name");
        check("write".equals(rt.getUnresolvedActions()), "round-trip actions");
        check(rt.getUnresolvedCerts() == null, "round-trip null certs");
        check("".equals(rt.getActions()), "round-trip resolved actions empty");
        check(rt.implies(new java.security.AllPermission()) == false, "round-trip never implies");
        // empty-certs serialization path: serialization normalizes an empty cert array to null
        UnresolvedPermission rt2 = (UnresolvedPermission) roundTrip(
                new UnresolvedPermission("t", "n", "a", new Certificate[0]));
        check(rt2.getUnresolvedCerts() == null, "round-trip normalizes empty certs to null");
    }

    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream oin = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return oin.readObject();
    }
}

