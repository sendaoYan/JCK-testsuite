/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanTrustPermission conformance: concrete class reflection.
 * @run main MBeanTrustPermissionTest
 */

public class MBeanTrustPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanTrustPermission");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.security.BasicPermission".equals(c.getSuperclass().getName()), "superclass java.security.BasicPermission");
        deepTargetsImplies();
        deepExceptions();
        deepEqualsSerialCollection();

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

    // ==== appended deepening (2026-11): targets + implies ====

    static void deepTargetsImplies() {
        javax.management.MBeanTrustPermission reg = new javax.management.MBeanTrustPermission("register");
        javax.management.MBeanTrustPermission star = new javax.management.MBeanTrustPermission("*");
        check("register".equals(reg.getName()), "register name");
        check("*".equals(star.getName()), "star name");
        check("".equals(reg.getActions()) && "".equals(star.getActions()),
                "BasicPermission: getActions always empty");
        check(reg.implies(reg), "reflexive");
        check(reg.implies(new javax.management.MBeanTrustPermission("register")), "register implies register");
        check(!reg.implies(star), "register does not imply *");
        check(star.implies(reg), "* implies register");
        check(!reg.implies(null), "implies(null) is false");
    }

    // ==== appended deepening (2026-11): constructor exception matrix ====

    static void deepExceptions() {
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanTrustPermission(null); }
        }, "null name -> NPE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanTrustPermission("bogus"); }
        }, "unknown target -> IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new javax.management.MBeanTrustPermission("register", "foo"); }
        }, "non-null actions -> IAE");
        check(new javax.management.MBeanTrustPermission("register", null) != null,
                "2-arg constructor with null actions is accepted");
    }

    // ==== appended deepening (2026-11): equals/hashCode/serialization/collection ====

    static void deepEqualsSerialCollection() throws Exception {
        javax.management.MBeanTrustPermission reg = new javax.management.MBeanTrustPermission("register");
        check(reg.equals(new javax.management.MBeanTrustPermission("register")), "equals same name");
        check(!reg.equals(new javax.management.MBeanTrustPermission("*")), "equals differs by name");
        check(!reg.equals(null) && !reg.equals("x"), "not equal to null or unrelated type");
        check(reg.hashCode() == new javax.management.MBeanTrustPermission("register").hashCode(),
                "hashCode stable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(reg);
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(reg.equals(back) && reg.hashCode() == back.hashCode(), "serialization round-trip");
        java.security.PermissionCollection pc = reg.newPermissionCollection();
        check(pc != null, "newPermissionCollection non-null");
        pc.add(new javax.management.MBeanTrustPermission("*"));
        check(pc.implies(new javax.management.MBeanTrustPermission("register")),
                "collection with * implies register");
        java.security.PermissionCollection pc2 = reg.newPermissionCollection();
        pc2.add(reg);
        check(pc2.implies(new javax.management.MBeanTrustPermission("register")),
                "collection with register implies register");
    }
}

