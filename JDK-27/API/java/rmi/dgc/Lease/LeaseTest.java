/*
 * @test
 * @summary java.rmi.dgc.Lease conformance: white-box structure + black-box behavior.
 * @run main LeaseTest
 */

import java.rmi.dgc.Lease;
import java.rmi.dgc.VMID;

public class LeaseTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.dgc.Lease");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "final class");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        java.util.TreeSet<String> m = new java.util.TreeSet<>();
        for (java.lang.reflect.Method mm : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isPublic(mm.getModifiers())) m.add(mm.getName());
        check(m.equals(java.util.Set.of("getVMID", "getValue")), "accessors");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        VMID vmid = new VMID();
        Lease l = new Lease(vmid, 60000L);
        check(l.getVMID() == vmid, "getVMID returns same instance");
        check(l.getValue() == 60000L, "getValue");

        Lease ln = new Lease(null, 0L);
        check(ln.getVMID() == null, "null VMID allowed");
        check(ln.getValue() == 0L, "zero value");

        Lease neg = new Lease(vmid, -1L);
        check(neg.getValue() == -1L, "negative value preserved");

        check(l.equals(l), "equals self");
        check(!l.equals(new Lease(vmid, 60000L)), "no value equality (identity only)");
        check(!l.equals(new Lease(new VMID(), 60000L)), "different VMID not equal");
        check(!l.equals(null), "not equals null");
        check(!l.equals("x"), "not equals non-Lease");

        Lease rt = (Lease) roundtrip(l);
        check(rt.getValue() == 60000L, "serialization preserves value");
        check(vmid.equals(rt.getVMID()), "serialization preserves VMID");
    }

    static Object roundtrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(o); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return ois.readObject();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
