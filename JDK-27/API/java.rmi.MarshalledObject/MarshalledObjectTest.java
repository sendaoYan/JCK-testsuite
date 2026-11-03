/*
 * @test
 * @summary java.rmi.MarshalledObject conformance: white-box structure + black-box behavior.
 * @run main MarshalledObjectTest
 */

import java.rmi.MarshalledObject;

public class MarshalledObjectTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.MarshalledObject");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "final class");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        java.util.TreeSet<String> m = new java.util.TreeSet<>();
        for (java.lang.reflect.Method mm : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isPublic(mm.getModifiers())) m.add(mm.getName());
        check(m.equals(java.util.Set.of("equals", "get", "hashCode")), "public methods");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MarshalledObject<String> mo = new MarshalledObject<>("hello");
        check("hello".equals(mo.get()), "String round trip");
        check(mo.equals(mo), "equals self");
        check(mo.hashCode() == mo.hashCode(), "hashCode stable");

        MarshalledObject<String> mo2 = new MarshalledObject<>("hello");
        check(mo.equals(mo2), "equals same content");
        check(mo.hashCode() == mo2.hashCode(), "hashCode equal for equal content");
        check(!mo.equals(new MarshalledObject<>("world")), "not equals different content");
        check(!mo.equals(null), "not equals null");
        check(!mo.equals("hello"), "not equals non-MarshalledObject");

        int[] arr = {1, 2, 3};
        MarshalledObject<int[]> moa = new MarshalledObject<int[]>(arr);
        int[] back = moa.get();
        check(back != arr, "get returns a copy, not the same array");
        check(java.util.Arrays.equals(arr, back), "int[] content round trip");
        check(moa.get() != moa.get(), "two gets return distinct copies");

        MarshalledObject<String> mon = new MarshalledObject<String>(null);
        check(mon.get() == null, "null marshalled get -> null");
        check(mon.equals(new MarshalledObject<String>(null)), "two null-marshalled equal");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
