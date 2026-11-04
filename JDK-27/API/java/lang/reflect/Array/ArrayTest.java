/*
 * @test
 * @summary java.lang.reflect.Array conformance: white-box structure + black-box behavior.
 * @run main ArrayTest
 */

import java.lang.reflect.Array;

public class ArrayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
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
        check(java.lang.reflect.Modifier.isFinal(Array.class.getModifiers())
              && java.lang.reflect.Modifier.isPublic(Array.class.getModifiers()),
              "Array is public final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. creation: primitives, boxed, object, multi-dim, and component-of-array
        int[] ints = (int[]) Array.newInstance(int.class, 4);
        check(Array.getLength(ints) == 4, "int array length");
        boolean[] bools = (boolean[]) Array.newInstance(boolean.class, 2);
        double[] doubles = (double[]) Array.newInstance(double.class, 3);
        String[] strs = (String[]) Array.newInstance(String.class, 5);
        check(strs.getClass() == String[].class, "component type String[]");
        Integer[][] nested = (Integer[][]) Array.newInstance(Integer[].class, 2);
        check(nested.getClass() == Integer[][].class, "component Integer[] gives 2-D");
        int[][] md = (int[][]) Array.newInstance(int.class, 2, 3);
        check(md.length == 2 && md[1].length == 3, "dims (2,3)");
        int[][] md2 = (int[][]) Array.newInstance(int.class, new int[]{2, 3});
        check(md2.length == 2 && md2[0].length == 3, "dims varargs array form");

        // 2. full typed get/set family on primitives
        Array.setBoolean(bools, 0, true);
        check(Array.getBoolean(bools, 0), "setBoolean/getBoolean");
        byte[] bytes = (byte[]) Array.newInstance(byte.class, 1);
        Array.setByte(bytes, 0, (byte) 3);
        check(Array.getByte(bytes, 0) == 3, "setByte/getByte");
        Array.setByte(ints, 0, (byte) 5);
        check(Array.getInt(ints, 0) == 5, "setByte widening into int[]");
        short[] shorts = (short[]) Array.newInstance(short.class, 1);
        Array.setShort(shorts, 0, (short) 9);
        check(Array.getShort(shorts, 0) == 9, "setShort/getShort");
        Array.setShort(ints, 1, (short) 8);
        check(Array.getInt(ints, 1) == 8, "setShort widening into int[]");
        char[] chars = (char[]) Array.newInstance(char.class, 1);
        Array.setChar(chars, 0, 'x');
        check(Array.getChar(chars, 0) == 'x', "setChar/getChar");
        Array.setChar(ints, 2, 'x');
        check(Array.getInt(ints, 2) == 'x', "setChar widening into int[]");
        Array.setInt(ints, 3, 77);
        check(Array.getInt(ints, 3) == 77, "setInt/getInt");
        long[] longs = (long[]) Array.newInstance(long.class, 1);
        Array.setLong(longs, 0, 123456789012L);
        check(Array.getLong(longs, 0) == 123456789012L, "setLong/getLong");
        float[] floats = (float[]) Array.newInstance(float.class, 1);
        Array.setFloat(floats, 0, 1.5f);
        check(Array.getFloat(floats, 0) == 1.5f, "setFloat/getFloat");
        Array.setFloat(doubles, 0, 1.5f);
        check(Array.getDouble(doubles, 0) == 1.5, "setFloat widening into double[]");
        Array.setDouble(doubles, 1, Math.PI);
        check(Array.getDouble(doubles, 1) == Math.PI, "setDouble/getDouble");

        // 3. boxed get/set with widening reads
        Array.set(longs, 0, Integer.valueOf(42));
        check(Array.getLong(longs, 0) == 42L, "set boxed Integer into long[]");
        check(Array.getDouble(ints, 3) == 77.0, "getDouble widening from int element");
        check(Array.get(ints, 3) instanceof Integer, "get boxes primitive element");

        // 4. object arrays
        Array.set(strs, 0, "a");
        Array.set(strs, 1, "b");
        check("a".equals(Array.get(strs, 0)) && "b".equals(Array.get(strs, 1)), "object get/set");
        check(Array.getLength(strs) == 5, "object array length");
        Array.set(nested, 0, new Integer[]{1, 2});
        check(((Integer[]) Array.get(nested, 0))[1] == 2, "nested array element");

        // 5. length and bounds
        try { Array.getInt(ints, -1); check(false, "negative index must fail"); }
        catch (ArrayIndexOutOfBoundsException e) { check(true, "AIOOBE negative index"); }
        try { Array.setInt(ints, 4, 1); check(false, "index == length must fail"); }
        catch (ArrayIndexOutOfBoundsException e) { check(true, "AIOOBE index == length"); }
        try { Array.get(strs, 99); check(false, "object index oob must fail"); }
        catch (ArrayIndexOutOfBoundsException e) { check(true, "AIOOBE object array"); }

        // 6. negative array size
        try { Array.newInstance(int.class, -1); check(false, "negative size must fail"); }
        catch (NegativeArraySizeException e) { check(true, "NegativeArraySizeException"); }

        // 7. non-array receivers
        try { Array.getLength("not-array"); check(false, "getLength non-array must fail"); }
        catch (IllegalArgumentException e) { check(true, "IAE getLength non-array"); }
        try { Array.get(new Object(), 0); check(false, "get non-array must fail"); }
        catch (IllegalArgumentException e) { check(true, "IAE get non-array"); }
        try { Array.setInt("str", 0, 1); check(false, "set non-array must fail"); }
        catch (IllegalArgumentException e) { check(true, "IAE set non-array"); }

        // 8. element type mismatches
        try { Array.set(ints, 0, "str"); check(false, "String into int[] must fail"); }
        catch (IllegalArgumentException e) { check(true, "IAE wrong element type boxed"); }
        try { Array.set(ints, 0, null); check(false, "null into int[] must fail"); }
        catch (IllegalArgumentException e) { check(true, "IAE null into primitive array"); }
        try { Array.setInt(strs, 0, 1); check(false, "setInt on String[] must fail"); }
        catch (IllegalArgumentException e) { check(true, "IAE setInt on object array"); }
        try { Array.setBoolean(ints, 0, true); check(false, "setBoolean on int[] must fail"); }
        catch (IllegalArgumentException e) { check(true, "IAE setBoolean on int[]"); }
        try { Array.getInt(longs, 0); check(false, "getInt on long[] must fail"); }
        catch (IllegalArgumentException e) { check(true, "IAE narrowing getInt"); }
        check(Array.getLong(ints, 3) == 77L, "getLong widening from int[]");

        // 9. zero-length arrays
        int[] empty = (int[]) Array.newInstance(int.class, 0);
        check(Array.getLength(empty) == 0, "zero length");
        try { Array.get(empty, 0); check(false, "empty get must fail"); }
        catch (ArrayIndexOutOfBoundsException e) { check(true, "AIOOBE on empty"); }
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
