/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanPermission conformance: white-box structure + black-box behavior.
 * @run main MBeanPermissionTest
 */

import java.lang.reflect.*;
import java.security.*;
import javax.management.*;

@SuppressWarnings({"removal","deprecation"})
public class MBeanPermissionTest {
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
        Class<?> c = Class.forName("javax.management.MBeanPermission");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.security.Permission.class.isAssignableFrom(c), "extends Permission");
        check("java.security.Permission".equals(c.getSuperclass().getName()), "direct superclass Permission");
        check(c.getConstructor(String.class, String.class) != null, "2-arg constructor");
        check(c.getConstructor(String.class, String.class, javax.management.ObjectName.class, String.class) != null, "4-arg constructor");
        c.getMethod("getActions");
        c.getMethod("implies", java.security.Permission.class);
        c.getMethod("equals", Object.class);
        c.getMethod("hashCode");
    }

    static void testBlackBox() throws Exception {
        MBeanPermission p = new MBeanPermission("*", "getAttribute,setAttribute,invoke");
        check("getAttribute,setAttribute,invoke".equals(p.getActions()), "actions preserved");
        check(p.implies(new MBeanPermission("*", "getAttribute")), "implies subset action");
        check(p.implies(new MBeanPermission("*", "setAttribute")), "implies another subset action");
        check(!p.implies(new MBeanPermission("*", "instantiate")), "does not imply disjoint action");
        check("*".equals(new MBeanPermission("*", "*").getActions()), "wildcard actions normalize to *");
        check(new MBeanPermission("*", "*").implies(p), "wildcard actions imply all");
        check(!p.implies(new MBeanPermission("*", "*")), "subset does not imply wildcard");

        check(new MBeanPermission("*", "invoke").implies(new MBeanPermission("a.b.C", "invoke")), "* class implies specific class");
        check(!new MBeanPermission("a.b.C", "invoke").implies(new MBeanPermission("*", "invoke")), "specific class does not imply *");

        MBeanPermission p4 = new MBeanPermission("a.b.C", "invoke", new javax.management.ObjectName("d:type=x"), "invoke");
        check("a.b.C#invoke[d:type=x]".equals(p4.getName()), "4-arg name");
        check("invoke".equals(p4.getActions()), "4-arg actions");
        check(p4.implies(new MBeanPermission("a.b.C", "invoke", new javax.management.ObjectName("d:type=x"), "invoke")), "4-arg implies equal");

        check(p.equals(new MBeanPermission("*", "getAttribute,setAttribute,invoke")), "equals same name/actions");
        check(p.hashCode() == new MBeanPermission("*", "getAttribute,setAttribute,invoke").hashCode(), "hashCode stable");
        check(!p.equals(new MBeanPermission("*", "getAttribute")), "equals differs by actions");

        checkIAE("MBeanPermission name cannot be null", () -> new MBeanPermission(null, "invoke"));
        checkIAE("MBeanPermission name cannot be empty", () -> new MBeanPermission("", "invoke"));
        checkIAE("MBeanPermission: actions can't be null", () -> new MBeanPermission("*", null));
        checkIAE("MBeanPermission: actions can't be empty", () -> new MBeanPermission("*", ""));
        checkIAE("Invalid permission: bogus", () -> new MBeanPermission("*", "bogus"));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepActionParsing();
        deepImplies();
        deepNameForms();
        deepExceptions();
        deepSerialization();

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

    // ==== appended deepening (2026-11): action parsing + equality ====

    static void deepActionParsing() {
        javax.management.MBeanPermission p =
                new javax.management.MBeanPermission("com.example.Foo", "invoke,getAttribute");
        check("invoke,getAttribute".equals(p.getActions()), "getActions preserves given order and case");
        check("*".equals(new javax.management.MBeanPermission("*", "*").getActions()),
                "wildcard actions normalize to *");
        check(p.equals(new javax.management.MBeanPermission("com.example.Foo", "getAttribute,invoke")),
                "equals is action-order insensitive (mask equality)");
        check(!p.equals(new javax.management.MBeanPermission("com.example.Foo", "invoke")),
                "equals differs by action set");
        check(!p.equals(new javax.management.MBeanPermission("com.example.Bar", "invoke,getAttribute")),
                "equals differs by class name");
        check(!p.equals(null), "not equal to null");
        check(!p.equals("x"), "not equal to an unrelated type");
        check(p.hashCode() == new javax.management.MBeanPermission("com.example.Foo", "invoke,getAttribute").hashCode(),
                "hashCode stable for identical inputs");
    }

    // ==== appended deepening (2026-11): implies matrix ====

    static void deepImplies() {
        javax.management.MBeanPermission wide =
                new javax.management.MBeanPermission("com.example.Foo", "invoke,getAttribute");
        check(wide.implies(new javax.management.MBeanPermission("com.example.Foo", "invoke")),
                "implies action subset");
        check(!wide.implies(new javax.management.MBeanPermission("com.example.Foo", "setAttribute")),
                "no implication of a disjoint action");
        check(!wide.implies(new javax.management.MBeanPermission("com.example.Foo", "invoke,getAttribute,setAttribute")),
                "no implication of an action superset");
        check(wide.implies(wide), "reflexive");
        check(!wide.implies(null), "implies(null) is false");
        check(new javax.management.MBeanPermission("*", "invoke")
                        .implies(new javax.management.MBeanPermission("com.example.Foo", "invoke")),
                "wildcard class implies specific class");
        check(!new javax.management.MBeanPermission("com.example.Foo", "invoke")
                        .implies(new javax.management.MBeanPermission("*", "invoke")),
                "specific class does not imply wildcard");
        check(new javax.management.MBeanPermission("*", "*")
                        .implies(new javax.management.MBeanPermission("com.example.Foo#op[d:k=v]", "invoke")),
                "* class and * actions imply a fully-qualified permission");
        check(new javax.management.MBeanPermission("com.example.Foo", "invoke")
                        .implies(new javax.management.MBeanPermission("com.example.Foo#op[d:k=v]", "invoke")),
                "permission without member/objectName implies the member-scoped form");
    }

    // ==== appended deepening (2026-11): name forms and the 4-arg constructor ====

    static void deepNameForms() throws Exception {
        javax.management.MBeanPermission m2 =
                new javax.management.MBeanPermission("com.example.Foo#attr[d:type=Foo]", "getAttribute");
        check("com.example.Foo#attr[d:type=Foo]".equals(m2.getName()),
                "className#member[objectName] name round-trip");
        check("getAttribute".equals(m2.getActions()), "member form actions");
        javax.management.MBeanPermission m4 = new javax.management.MBeanPermission(
                "com.example.Foo", "invoke", new javax.management.ObjectName("d:type=Foo"), "invoke");
        check("com.example.Foo#invoke[d:type=Foo]".equals(m4.getName()),
                "4-arg constructor composes the name");
        check("invoke".equals(m4.getActions()), "4-arg actions come from the actions parameter");
        check(m4.implies(new javax.management.MBeanPermission("com.example.Foo", "invoke",
                        new javax.management.ObjectName("d:type=Foo"), "invoke")),
                "4-arg implies an equal 4-arg permission");
        check(!m4.implies(new javax.management.MBeanPermission("com.example.Foo", "getAttribute",
                        new javax.management.ObjectName("d:type=Foo"), "getAttribute")),
                "4-arg does not imply a different action");
    }

    // ==== appended deepening (2026-11): constructor exception matrix ====

    static void deepExceptions() {
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanPermission(null, "invoke"); }
        }, "null name -> IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanPermission("", "invoke"); }
        }, "empty name -> IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanPermission("*", null); }
        }, "null actions -> IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanPermission("*", ""); }
        }, "empty actions -> IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanPermission("*", "bogus"); }
        }, "unknown action -> IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanPermission("*", "INVOKE"); }
        }, "actions are case-sensitive: INVOKE is invalid");
    }

    // ==== appended deepening (2026-11): serialization round-trip ====

    static void deepSerialization() throws Exception {
        javax.management.MBeanPermission p =
                new javax.management.MBeanPermission("com.example.Foo", "invoke,getAttribute");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(p);
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof javax.management.MBeanPermission, "round-trip type");
        check(p.equals(back), "round-trip equals");
        check(p.hashCode() == back.hashCode(), "round-trip hashCode");
    }
}

