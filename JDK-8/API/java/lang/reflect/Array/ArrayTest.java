/*
 * @test
 * @summary java.lang.reflect.Array conformance: newInstance/get/set for primitive and object
 *          arrays.
 * @run main ArrayTest
 */

import java.lang.reflect.Array;

public class ArrayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testArray() {
        int[] a = (int[]) Array.newInstance(int.class, 3);
        check(a.length == 3, "newInstance length");
        check(Array.getLength(a) == 3, "getLength");
        Array.setInt(a, 1, 42);
        check(Array.getInt(a, 1) == 42, "setInt/getInt");
        Array.set(a, 0, Integer.valueOf(7));
        check(((Integer) Array.get(a, 0)).intValue() == 7, "set/get Object");

        String[] s = (String[]) Array.newInstance(String.class, 2);
        Array.set(s, 1, "hi");
        check("hi".equals(Array.get(s, 1)), "object array set/get");

        int[][] m = (int[][]) Array.newInstance(int.class, 2, 3);
        check(m.length == 2 && m[0].length == 3, "multi-dimensional newInstance");
    }

    public static void main(String[] args) {
        testArray();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
