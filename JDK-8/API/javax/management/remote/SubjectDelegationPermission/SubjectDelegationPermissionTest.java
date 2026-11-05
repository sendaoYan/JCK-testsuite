/*
 * @test
 * @summary javax.management.remote.SubjectDelegationPermission conformance: name/implies.
 * @run main SubjectDelegationPermissionTest
 */
import javax.management.*;


import javax.management.remote.*;
public class SubjectDelegationPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        SubjectDelegationPermission p = new SubjectDelegationPermission("com.example.Foo");
        check("com.example.Foo".equals(p.getName()), "name");
        check(p.implies(new SubjectDelegationPermission("com.example.Foo")), "implies same");
        check(!p.implies(new SubjectDelegationPermission("com.example.Bar")), "not implies different");
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): target-name matrix, implies lattice, actions, hierarchy, serialization ====
    static void testDeepened() throws Exception {
        SubjectDelegationPermission star = new SubjectDelegationPermission("javax.management.remote.*");
        SubjectDelegationPermission one = new SubjectDelegationPermission("javax.management.remote.Tail");
        check("javax.management.remote.*".equals(star.getName()), "d-star name");
        check("".equals(star.getActions()), "d-getActions empty");
        check(star.implies(one), "d-star implies specific");
        check(!one.implies(star), "d-specific not implies star");
        check(star.implies(star) && one.implies(one), "d-reflexive implies");
        check(!one.implies(new SubjectDelegationPermission("javax.management.remote.Other")), "d-not implies sibling");
        check(!one.implies(new SubjectDelegationPermission("com.example.Foo")), "d-not implies foreign");
        check(star.equals(new SubjectDelegationPermission("javax.management.remote.*")), "d-equals same name");
        check(star.hashCode() == new SubjectDelegationPermission("javax.management.remote.*").hashCode(), "d-hashCode same name");
        check(!star.equals(one), "d-not equals different name");
        check(!star.equals(null), "d-not equals null");
        // implies(null) is false rather than an exception (BasicPermission behavior, both JDKs).
        check(!star.implies(null), "d-implies null false");
        // Hierarchy: BasicPermission -> Permission.
        check(star instanceof java.security.BasicPermission, "d-extends BasicPermission");
        check(star instanceof java.security.Permission, "d-extends Permission");
        // Target names are not namespace-validated (only "*" wildcards follow BasicPermission rules).
        check(new SubjectDelegationPermission("bogus") != null, "d-arbitrary name accepted");
        // Any actions value is rejected: this permission carries actions-less semantics.
        try {
            new SubjectDelegationPermission("javax.management.remote.*", "read");
            check(false, "d-actions ctor should throw");
        } catch (IllegalArgumentException e) {
            check("Non-null actions".equals(e.getMessage()), "d-actions message: " + e.getMessage());
        }
        try {
            new SubjectDelegationPermission(null);
            check(false, "d-null name should throw");
        } catch (NullPointerException e) {
            check("name can't be null".equals(e.getMessage()), "d-null name message: " + e.getMessage());
        }
        // Serialization round-trip.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = null;
        try {
            oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(star);
        } finally {
            if (oos != null) oos.close();
        }
        java.io.ObjectInputStream ois = null;
        try {
            ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
            check(star.equals(ois.readObject()), "d-serialization round-trip equals");
        } finally {
            if (ois != null) ois.close();
        }
    }

}
