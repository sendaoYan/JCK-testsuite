/*
 * @test
 * @summary com.sun.security.jgss.InquireSecContextPermission conformance: white-box structure + black-box behavior.
 * @run main InquireSecContextPermissionTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class InquireSecContextPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.jgss.InquireSecContextPermission");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() == java.security.BasicPermission.class, "extends BasicPermission");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated (for removal)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        com.sun.security.jgss.InquireSecContextPermission p =
            new com.sun.security.jgss.InquireSecContextPermission("KRB5_GET_SESSION_KEY");
        check(p.getName().equals("KRB5_GET_SESSION_KEY"), "getName()");
        check(p.getActions().equals(""), "getActions() empty string");

        // implies() semantics (BasicPermission).
        check(p.implies(p), "implies(self)");
        check(p.implies(new com.sun.security.jgss.InquireSecContextPermission("KRB5_GET_SESSION_KEY")), "implies(same name)");
        check(!p.implies(new com.sun.security.jgss.InquireSecContextPermission("KRB5_GET_TKT_FLAGS")), "!implies(other name)");

        com.sun.security.jgss.InquireSecContextPermission wildcard =
            new com.sun.security.jgss.InquireSecContextPermission("*");
        check(wildcard.implies(p), "wildcard implies specific");
        check(!p.implies(wildcard), "specific does not imply wildcard");

        // Permission collection.
        java.security.PermissionCollection coll = p.newPermissionCollection();
        check(coll != null, "newPermissionCollection() non-null");
        coll.add(p);
        check(coll.implies(p), "collection implies added permission");
        check(!coll.implies(wildcard), "collection does not imply wildcard");

        // Serialization round-trip.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(p);
        }
        Object o;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            o = ois.readObject();
        }
        check(o instanceof com.sun.security.jgss.InquireSecContextPermission, "deserialized type");
        com.sun.security.jgss.InquireSecContextPermission p2 =
            (com.sun.security.jgss.InquireSecContextPermission) o;
        check(p2.equals(p), "equals after round-trip");
        check(p2.getName().equals(p.getName()), "name preserved after round-trip");

        // Constructor argument validation (from BasicPermission).
        boolean iae = false;
        try { new com.sun.security.jgss.InquireSecContextPermission(""); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "empty name -> IllegalArgumentException");

        boolean npe = false;
        try { new com.sun.security.jgss.InquireSecContextPermission(null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "null name -> NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
