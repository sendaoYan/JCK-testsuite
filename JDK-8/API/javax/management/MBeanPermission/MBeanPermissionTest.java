/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanPermission conformance: concrete class reflection.
 * @run main MBeanPermissionTest
 */

public class MBeanPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanPermission");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.security.Permission".equals(c.getSuperclass().getName()), "superclass java.security.Permission");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getActions"), "declares getActions");
        check(hasMethod(c, "hashCode"), "declares hashCode");
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

