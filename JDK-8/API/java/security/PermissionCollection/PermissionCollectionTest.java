/*
 * @test
 * @summary java.security.PermissionCollection conformance: abstract class hierarchy and key methods.
 * @run main PermissionCollectionTest
 */

public class PermissionCollectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.PermissionCollection");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("add"), "declares add");
        check(ms.contains("elements"), "declares elements");
        check(ms.contains("implies"), "declares implies");
        check(ms.contains("isReadOnly"), "declares isReadOnly");
        check(ms.contains("setReadOnly"), "declares setReadOnly");
        check(ms.contains("toString"), "declares toString");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording implementation + real collections ----
    static class VecCollection extends java.security.PermissionCollection {
        final java.util.Vector<java.security.Permission> v = new java.util.Vector<>();
        public void add(java.security.Permission p) {
            if (isReadOnly()) throw new SecurityException("collection is read-only");
            v.add(p);
        }
        public boolean implies(java.security.Permission p) {
            for (java.security.Permission q : v) if (q.implies(p)) return true;
            return false;
        }
        public java.util.Enumeration<java.security.Permission> elements() { return v.elements(); }
    }

    static void testContractDeep() throws Exception {
        // recording implementation: add/implies/elements round-trip
        VecCollection vc = new VecCollection();
        java.security.Permission sp = new java.security.SecurityPermission("a.b");
        vc.add(sp);
        check(vc.implies(sp), "recording implies member");
        check(!vc.implies(new java.security.SecurityPermission("z.z")), "recording implies non-member");
        java.util.Enumeration<java.security.Permission> e = vc.elements();
        check(e.hasMoreElements() && e.nextElement() == sp, "elements returns added permission");
        check(!e.hasMoreElements(), "elements exhausted");
        check(!vc.isReadOnly(), "initially writable");
        vc.setReadOnly();
        check(vc.isReadOnly(), "readOnly after setReadOnly");
        try {
            vc.add(sp);
            check(false, "add after setReadOnly must throw");
        } catch (SecurityException se) {
            check(true, "add after setReadOnly throws SecurityException");
        }
        check(vc.toString() != null && !vc.toString().isEmpty(), "toString non-empty");

        // real BasicPermissionCollection from SecurityPermission
        java.security.PermissionCollection pc = sp.newPermissionCollection();
        check(pc != null, "newPermissionCollection non-null");
        check(!pc.implies(sp), "empty collection implies false");
        check(!pc.implies(null), "implies(null) false (no NPE)");
        pc.add(sp);
        pc.add(new java.security.SecurityPermission("c.*"));
        check(pc.implies(new java.security.SecurityPermission("a.b")), "implies exact member");
        check(pc.implies(new java.security.SecurityPermission("c.d")), "implies wildcard member");
        check(!pc.implies(new java.security.SecurityPermission("c")), "wildcard does not imply parent name");
        check(!pc.implies(new java.security.SecurityPermission("z.z")), "non-member false");
        try {
            pc.add(null);
            check(false, "add(null) must throw");
        } catch (IllegalArgumentException iae) {
            check(true, "add(null) -> IllegalArgumentException");
        }
        pc.setReadOnly();
        pc.setReadOnly();
        check(pc.isReadOnly(), "setReadOnly idempotent");
        try {
            pc.add(new java.security.SecurityPermission("d.e"));
            check(false, "real collection add after readonly must throw");
        } catch (SecurityException se) {
            check("attempt to add a Permission to a readonly PermissionCollection".equals(se.getMessage()),
                    "readonly add exact message");
        }
        check(pc.toString().contains("a.b"), "toString lists permissions");

        // serialization round-trip preserves contents and readOnly flag
        java.security.PermissionCollection rt = (java.security.PermissionCollection) roundTrip(pc);
        check(rt instanceof java.security.PermissionCollection, "round-trip type");
        check(rt.isReadOnly(), "round-trip preserves readOnly");
        check(rt.implies(new java.security.SecurityPermission("a.b")), "round-trip preserves implies exact");
        check(rt.implies(new java.security.SecurityPermission("c.d")), "round-trip preserves implies wildcard");
        check(!rt.implies(new java.security.SecurityPermission("z.z")), "round-trip preserves non-member");
        int n = 0;
        java.util.Enumeration<java.security.Permission> e2 = rt.elements();
        while (e2.hasMoreElements()) { check(e2.nextElement() != null, "ser element " + n); n++; }
        check(n == 2, "round-trip element count");
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
