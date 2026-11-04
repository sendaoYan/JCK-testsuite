/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerPermission conformance: white-box structure + black-box behavior.
 * @run main MBeanServerPermissionTest
 */

import java.lang.reflect.*;
import java.security.*;
import javax.management.*;

@SuppressWarnings({"removal","deprecation"})
public class MBeanServerPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkIAE(String expected, Runnable r) {
        try {
            r.run();
            check(false, "expected IllegalArgumentException: " + expected);
        } catch (IllegalArgumentException e) {
            check(expected.equals(e.getMessage()), "IAE message got: [" + e.getMessage() + "]");
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerPermission");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.security.BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check("java.security.BasicPermission".equals(c.getSuperclass().getName()), "direct superclass BasicPermission");
        check(c.getConstructor(String.class) != null, "1-arg constructor");
        check(c.getConstructor(String.class, String.class) != null, "2-arg constructor");
        c.getMethod("implies", java.security.Permission.class);
        c.getMethod("equals", Object.class);
        c.getMethod("hashCode");
        c.getMethod("newPermissionCollection");
    }

    static void testBlackBox() throws Exception {
        MBeanServerPermission sp = new MBeanServerPermission("findMBeanServer,createMBeanServer");
        check("createMBeanServer,findMBeanServer".equals(sp.getName()), "name canonicalized");
        check("".equals(sp.getActions()), "actions empty (BasicPermission)");
        check(sp.implies(new MBeanServerPermission("createMBeanServer")), "implies subset");
        check(sp.implies(new MBeanServerPermission("findMBeanServer")), "implies other subset");
        check(!sp.implies(new MBeanServerPermission("releaseMBeanServer")), "does not imply disjoint");
        check(!new MBeanServerPermission("createMBeanServer").implies(sp), "subset does not imply superset");
        check(new MBeanServerPermission("*").implies(new MBeanServerPermission("createMBeanServer")), "* implies specific");
        check(!new MBeanServerPermission("createMBeanServer").implies(new MBeanServerPermission("*")), "specific does not imply *");
        check("*".equals(new MBeanServerPermission("*").getName()), "wildcard name");
        check("".equals(new MBeanServerPermission("*").getActions()), "wildcard actions empty");

        MBeanServerPermission same = new MBeanServerPermission("findMBeanServer,createMBeanServer");
        check(sp.equals(same), "equals ignores name order");
        check(sp.hashCode() == same.hashCode(), "hashCode ignores name order");
        check(!sp.equals(new MBeanServerPermission("createMBeanServer")), "equals differs by mask");

        java.security.PermissionCollection pc = sp.newPermissionCollection();
        pc.add(sp);
        check(pc.implies(new MBeanServerPermission("createMBeanServer")), "PermissionCollection implies subset");

        checkIAE("Invalid MBeanServerPermission name: \"bogus\"", () -> new MBeanServerPermission("bogus"));
        checkIAE("MBeanServerPermission actions must be null: foo", () -> new MBeanServerPermission("createMBeanServer", "foo"));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepTargets();
        deepImplies();
        deepEqualsSerial();
        deepCollection();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== appended deepening (2026-11): shared helpers ====

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

    // ==== appended deepening (2026-11): target-name matrix ====

    static void deepTargets() {
        String[] targets = {"createMBeanServer", "newMBeanServer", "findMBeanServer", "releaseMBeanServer"};
        for (final String t : targets) {
            check("".equals(new javax.management.MBeanServerPermission(t).getActions()),
                    "getActions empty for " + t);
        }
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanServerPermission(null); }
        }, "null name -> NPE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanServerPermission(""); }
        }, "empty name -> IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanServerPermission("bogus"); }
        }, "unknown target -> IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanServerPermission("createMBeanServer", "foo"); }
        }, "non-null actions -> IAE");
        check(new javax.management.MBeanServerPermission("createMBeanServer", null) != null,
                "2-arg constructor with null actions is accepted");
    }

    // ==== appended deepening (2026-11): implies matrix ====

    static void deepImplies() {
        javax.management.MBeanServerPermission create =
                new javax.management.MBeanServerPermission("createMBeanServer");
        javax.management.MBeanServerPermission neu =
                new javax.management.MBeanServerPermission("newMBeanServer");
        check(create.implies(neu), "createMBeanServer implies newMBeanServer");
        check(!neu.implies(create), "newMBeanServer does not imply createMBeanServer");
        javax.management.MBeanServerPermission star = new javax.management.MBeanServerPermission("*");
        check(star.implies(create) && star.implies(neu)
                        && star.implies(new javax.management.MBeanServerPermission("findMBeanServer"))
                        && star.implies(new javax.management.MBeanServerPermission("releaseMBeanServer")),
                "* implies every valid target");
        check(!create.implies(star), "a specific target does not imply *");
        check(create.implies(create), "reflexive");
        check(!create.implies(new javax.management.MBeanServerPermission("findMBeanServer")),
                "disjoint targets do not imply each other");
        check("createMBeanServer".equals(
                        new javax.management.MBeanServerPermission("newMBeanServer,createMBeanServer").getName()),
                "combined name canonicalizes to createMBeanServer (mask covers new)");
        javax.management.MBeanServerPermission combo =
                new javax.management.MBeanServerPermission("releaseMBeanServer,findMBeanServer");
        check(combo.implies(new javax.management.MBeanServerPermission("releaseMBeanServer"))
                        && combo.implies(new javax.management.MBeanServerPermission("findMBeanServer")),
                "comma-combined permission implies each part");
    }

    // ==== appended deepening (2026-11): equals/hashCode/serialization ====

    static void deepEqualsSerial() throws Exception {
        javax.management.MBeanServerPermission p =
                new javax.management.MBeanServerPermission("createMBeanServer,newMBeanServer");
        check(p.equals(new javax.management.MBeanServerPermission("createMBeanServer")),
                "equals follows the mask, not the literal name");
        check(p.hashCode() == new javax.management.MBeanServerPermission("createMBeanServer").hashCode(),
                "hashCode follows the mask");
        check(!p.equals(new javax.management.MBeanServerPermission("newMBeanServer")),
                "mask difference distinguishes equals");
        check(!p.equals(null) && !p.equals("x"), "not equal to null or an unrelated type");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(p);
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(p.equals(back) && p.hashCode() == back.hashCode(), "serialization round-trip");
    }

    // ==== appended deepening (2026-11): PermissionCollection ====

    static void deepCollection() {
        javax.management.MBeanServerPermission p =
                new javax.management.MBeanServerPermission("newMBeanServer");
        java.security.PermissionCollection pc = p.newPermissionCollection();
        check(pc != null, "newPermissionCollection non-null");
        pc.add(new javax.management.MBeanServerPermission("newMBeanServer"));
        check(pc.implies(new javax.management.MBeanServerPermission("newMBeanServer")),
                "collection implies an added permission");
        check(!pc.implies(new javax.management.MBeanServerPermission("createMBeanServer")),
                "collection does not imply create when only new was added");
        check(!pc.implies(new javax.management.MBeanServerPermission("releaseMBeanServer")),
                "collection does not imply an absent target");
        pc.add(new javax.management.MBeanServerPermission("releaseMBeanServer"));
        check(pc.implies(new javax.management.MBeanServerPermission("releaseMBeanServer")),
                "collection implies after a second add");
        check(pc.implies(new javax.management.MBeanServerPermission("newMBeanServer")),
                "earlier permission still implied");
    }
}

