/*
 * @test
 * @summary java.security.PermissionCollection conformance: white-box structure + black-box behavior.
 * @run main PermissionCollectionTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class PermissionCollectionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.PermissionCollection");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkInterfaces(c, new String[]{"java.io.Serializable"});
        checkMethods(c, new String[]{"add", "elements", "elementsAsStream", "implies", "isReadOnly", "setReadOnly", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Minimal concrete collection drives the abstract contract.
        class VecCollection extends PermissionCollection {
            final Vector<Permission> v = new Vector<>();
            public void add(Permission p) {
                if (isReadOnly()) throw new SecurityException("collection is read-only");
                v.add(p);
            }
            public boolean implies(Permission p) {
                for (Permission q : v) if (q.implies(p)) return true;
                return false;
            }
            public Enumeration<Permission> elements() { return v.elements(); }
        }
        VecCollection pc = new VecCollection();
        pc.add(new SecurityPermission("a.b"));
        pc.add(new AllPermission());
        check(pc.implies(new SecurityPermission("a.b")), "implies member");
        check(pc.implies(new RuntimePermission("anything")), "implies AllPermission");
        Enumeration<Permission> e = pc.elements();
        int n = 0;
        while (e.hasMoreElements()) { Permission p = e.nextElement(); check(p != null, "element " + n); n++; }
        check(n == 2, "elements count");
        check(!pc.isReadOnly(), "initially writable");
        pc.setReadOnly();
        check(pc.isReadOnly(), "readOnly set");
        try {
            pc.add(new SecurityPermission("c"));
            check(false, "add on read-only should throw");
        } catch (SecurityException se) {
            check(true, "add on read-only throws SecurityException");
        }
        // toString of a PermissionCollection is never null
        check(pc.toString() != null && !pc.toString().isEmpty(), "toString non-empty");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening (JDK-27): real collections, readOnly message, serialization, stream ----
    static void testContractDeep() throws Exception {
        Permission sp = new SecurityPermission("a.b");
        PermissionCollection pc = sp.newPermissionCollection();
        check(pc != null, "newPermissionCollection non-null");
        check(!pc.implies(sp), "empty collection implies false");
        check(!pc.implies(null), "implies(null) false (no NPE)");
        pc.add(sp);
        pc.add(new SecurityPermission("c.*"));
        check(pc.implies(new SecurityPermission("a.b")), "implies exact member");
        check(pc.implies(new SecurityPermission("c.d")), "implies wildcard member");
        check(!pc.implies(new SecurityPermission("c")), "wildcard does not imply parent name");
        check(!pc.implies(new SecurityPermission("z.z")), "non-member false");
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
            pc.add(new SecurityPermission("d.e"));
            check(false, "real collection add after readonly must throw");
        } catch (SecurityException se) {
            check("attempt to add a Permission to a readonly PermissionCollection".equals(se.getMessage()),
                    "readonly add exact message");
        }
        check(pc.toString().contains("a.b"), "toString lists permissions");

        // JDK-9+ default method elementsAsStream
        long n = pc.elementsAsStream().count();
        check(n == 2, "elementsAsStream count");
        check(pc.elementsAsStream().allMatch(p -> p instanceof SecurityPermission), "elementsAsStream element type");

        // serialization round-trip preserves contents and readOnly flag
        PermissionCollection rt = (PermissionCollection) roundTrip(pc);
        check(rt.isReadOnly(), "round-trip preserves readOnly");
        check(rt.implies(new SecurityPermission("a.b")), "round-trip preserves implies exact");
        check(rt.implies(new SecurityPermission("c.d")), "round-trip preserves implies wildcard");
        check(!rt.implies(new SecurityPermission("z.z")), "round-trip preserves non-member");
        check(rt.elementsAsStream().count() == 2, "round-trip stream count");

        // UnresolvedPermissionCollection: add does not enforce readOnly (unlike BasicPermissionCollection)
        UnresolvedPermission up = new UnresolvedPermission("java.security.SecurityPermission", "getPolicy", null, null);
        PermissionCollection upc = up.newPermissionCollection();
        check(upc != null, "UP newPermissionCollection non-null");
        upc.setReadOnly();
        upc.add(new UnresolvedPermission("java.security.SecurityPermission", "setLog", null, null));
        check(true, "UnresolvedPermissionCollection.add after setReadOnly does not throw");
        check(!upc.implies(new SecurityPermission("getPolicy")), "direct UPC implies always false");
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
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
