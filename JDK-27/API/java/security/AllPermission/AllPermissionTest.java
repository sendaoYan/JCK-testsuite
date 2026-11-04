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

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: null-tolerant implies, sentinel immutability, collection lifecycle ----
    static void testContractDeep() throws Exception {
        AllPermission ap = new AllPermission();

        // AllPermission.implies(null) is TRUE (unconditional dominance, no null check)
        check(ap.implies(null), "implies(null) true");
        check(ap.implies(new java.net.NetPermission("x")), "implies NetPermission");
        check(ap.implies(new java.security.UnresolvedPermission("t", "n", null, null)), "implies UnresolvedPermission");

        // (name, actions) constructor: even null/null is accepted, sentinels win
        AllPermission nulls = new AllPermission(null, null);
        check("<all permissions>".equals(nulls.getName()), "(null,null) getName sentinel");
        check("<all actions>".equals(nulls.getActions()), "(null,null) getActions sentinel");
        check(nulls.equals(ap), "(null,null) equals default");

        // toString exact format: ("class" "<all permissions>" "<all actions>")
        check("(\"java.security.AllPermission\" \"<all permissions>\" \"<all actions>\")".equals(ap.toString()),
                "toString exact form");

        // AllPermissionCollection lifecycle: read-only enforcement and serialization
        PermissionCollection pc = ap.newPermissionCollection();
        check("java.security.AllPermissionCollection".equals(pc.getClass().getName()),
                "collection class AllPermissionCollection");
        check(!pc.implies(new BP("x")), "empty collection implies false");
        pc.add(ap);
        check(pc.implies(null), "AllPermissionCollection implies(null) true");
        pc.setReadOnly();
        try {
            pc.add(ap);
            check(false, "AllPermissionCollection readonly add must throw");
        } catch (SecurityException se) {
            check(true, "AllPermissionCollection readonly add throws SecurityException");
        }
        PermissionCollection rt = (PermissionCollection) roundTrip(pc);
        check(rt.implies(new BP("whatever")), "round-trip collection implies anything");
        check(rt.implies(null), "round-trip implies(null) true");

        // AllPermission itself round-trips with value equality
        AllPermission back = (AllPermission) roundTrip(ap);
        check(back.equals(ap) && back.hashCode() == ap.hashCode(), "round-trip equals/hashCode");
        check("<all actions>".equals(back.getActions()), "round-trip actions sentinel");

        // null-permission tolerance of AllPermission inside Permissions
        java.security.Permissions ps = new java.security.Permissions();
        ps.add(ap);
        check(ps.implies(new java.security.SecurityPermission("s.p")), "Permissions+AllPermission implies");
        check(!ps.equals(ap), "Permissions not equal AllPermission (different types)");
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

