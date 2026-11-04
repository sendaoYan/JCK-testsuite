/*
 * @test
 * @summary java.rmi.dgc.VMID conformance: white-box structure + black-box behavior.
 * @run main VMIDTest
 */

import java.rmi.dgc.VMID;

public class VMIDTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.dgc.VMID");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "final class");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        java.util.TreeSet<String> m = new java.util.TreeSet<>();
        for (java.lang.reflect.Method mm : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isPublic(mm.getModifiers())) m.add(mm.getName());
        check(m.equals(java.util.Set.of("equals", "hashCode", "isUnique", "toString")), "public methods");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        VMID v = new VMID();
        String s = v.toString();
        check(s != null && s.length() > 0, "toString non-empty");
        check(s.indexOf(':') >= 0, "toString contains ':'");

        check(VMID.isUnique(), "isUnique true");
        check(v.hashCode() == v.hashCode(), "hashCode stable");
        check(v.equals(v), "equals self");
        check(!v.equals(new VMID()), "distinct VMIDs not equal");
        check(!v.equals(null), "not equals null");
        check(!v.equals("x"), "not equals non-VMID");

        VMID v2 = new VMID();
        check(!v.toString().equals(v2.toString()), "distinct toStrings");

        VMID rt = (VMID) roundtrip(v);
        check(v.equals(rt), "serialization round trip preserves equality");
        check(v.hashCode() == rt.hashCode(), "serialization preserves hashCode");
        check(v.toString().equals(rt.toString()), "serialization preserves toString");
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
