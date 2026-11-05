/*
 * @test
 * @summary javax.management.remote.SubjectDelegationPermission conformance: white-box structure + black-box behavior.
 * @run main SubjectDelegationPermissionTest
 */
import javax.management.*;
import javax.management.remote.*;


import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.BasicPermission;

import javax.management.remote.SubjectDelegationPermission;

@SuppressWarnings("removal")
public class SubjectDelegationPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.SubjectDelegationPermission");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check(c.getConstructor(String.class) != null, "(String) ctor");
        check(c.getConstructor(String.class, String.class) != null, "(String,String) ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SubjectDelegationPermission p = new SubjectDelegationPermission("javax.management.remote.SubjectDelegation");
        check(p.getName().equals("javax.management.remote.SubjectDelegation"), "name");
        check(p.getActions().equals(""), "actions empty");
        check(p.equals(new SubjectDelegationPermission("javax.management.remote.SubjectDelegation")), "equals");
        check(p.hashCode() == new SubjectDelegationPermission("javax.management.remote.SubjectDelegation").hashCode(), "hashCode");
        check(p.implies(new SubjectDelegationPermission("javax.management.remote.SubjectDelegation")), "implies itself");
        check(!p.equals(new SubjectDelegationPermission("other.name")), "different name not equal");

        // The two-argument constructor requires null actions.
        SubjectDelegationPermission p2 = new SubjectDelegationPermission("n", null);
        check(p2.getName().equals("n"), "two-arg null-actions name");
        check(p2.getActions().equals(""), "two-arg null-actions actions");

        try {
            new SubjectDelegationPermission("n", "read");
            check(false, "expected IAE for non-null actions");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Non-null actions"), "non-null actions message: " + e.getMessage());
        }
        try {
            new SubjectDelegationPermission(null);
            check(false, "expected NPE for null name");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("name can't be null"), "null name message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
