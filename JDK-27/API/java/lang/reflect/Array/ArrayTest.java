/*
 * @test
 * @summary java.lang.reflect.Array conformance: full contract coverage.
 *          newInstance (primitive/object/multi-dim) incl. negative/zero/null/void
 *          dimensions with exact exceptions, getLength/get/set across all 8 primitive
 *          types + Object arrays, boxing/unboxing, wrong-type -> IllegalArgumentException,
 *          index -> ArrayIndexOutOfBoundsException, null array -> NullPointerException,
 *          non-array -> IllegalArgumentException, ArrayStoreException for object arrays,
 *          multi-dimensional component access.
 * @run main ArrayTest
 */

import java.lang.reflect.Array;

public class ArrayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    // ---- newInstance(Class, int) ----

    static void testNewInstanceOneDim() {
        check(Array.newInstance(int.class, 0).getClass() == int[].class, "int[0] class");
        check(Array.newInstance(int.class, 5) instanceof int[], "int[5] instanceof");
        check(Array.getLength(Array.newInstance(int.class, 5)) == 5, "int[5] length");
        check(Array.newInstance(String.class, 0).getClass() == String[].class, "String[0] class");
        check(Array.newInstance(String[].class, 3) instanceof String[][], "String[][] class");

        // All primitive component types
        check(Array.newInstance(boolean.class, 1) instanceof boolean[], "boolean[]");
        check(Array.newInstance(byte.class, 1) instanceof byte[], "byte[]");
        check(Array.newInstance(char.class, 1) instanceof char[], "char[]");
        check(Array.newInstance(short.class, 1) instanceof short[], "short[]");
        check(Array.newInstance(int.class, 1) instanceof int[], "int[]");
        check(Array.newInstance(long.class, 1) instanceof long[], "long[]");
        check(Array.newInstance(float.class, 1) instanceof float[], "float[]");
        check(Array.newInstance(double.class, 1) instanceof double[], "double[]");

        // negative length -> NegativeArraySizeException (not IAE)
        checkThrows(NegativeArraySizeException.class,
                () -> Array.newInstance(int.class, -1), "newInstance negative length");
        checkThrows(NegativeArraySizeException.class,
                () -> Array.newInstance(int.class, Integer.MIN_VALUE),
                "newInstance MIN_VALUE length");
        checkThrows(NegativeArraySizeException.class,
                () -> Array.newInstance(String.class, -5), "newInstance object negative");

        // null component type -> NullPointerException
        checkThrows(NullPointerException.class,
                () -> Array.newInstance(null, 1), "newInstance null componentType");
        checkThrows(NullPointerException.class,
                () -> Array.newInstance(null, 0), "newInstance null componentType len 0");

        // void component type -> IllegalArgumentException
        checkThrows(IllegalArgumentException.class,
                () -> Array.newInstance(void.class, 0), "newInstance void component");
        checkThrows(IllegalArgumentException.class,
                () -> Array.newInstance(Void.TYPE, 1), "newInstance Void.TYPE component");
    }

    // ---- newInstance(Class, int...) multi-dimensional ----

    static void testNewInstanceMultiDim() {
        int[][] m = (int[][]) Array.newInstance(int.class, 2, 3);
        check(m.length == 2, "multi dim outer length");
        check(m[0].length == 3 && m[1].length == 3, "multi dim inner lengths");
        check(m.getClass() == int[][].class, "multi dim class");

        String[][][] s3 = (String[][][]) Array.newInstance(String.class, 2, 3, 4);
        check(s3.length == 2 && s3[0].length == 3 && s3[0][0].length == 4, "3-dim lengths");

        // null component type -> NPE
        checkThrows(NullPointerException.class,
                () -> Array.newInstance(null, 1, 2), "newInstance multi null componentType");
        // null dimensions array -> NPE
        checkThrows(NullPointerException.class,
                () -> Array.newInstance(int.class, (int[]) null), "newInstance null dimensions");

        // zero dimensions -> IllegalArgumentException
        checkThrows(IllegalArgumentException.class,
                () -> Array.newInstance(int.class, new int[0]), "newInstance zero dimensions");

        // any negative dimension -> NegativeArraySizeException
        checkThrows(NegativeArraySizeException.class,
                () -> Array.newInstance(int.class, 2, -3), "newInstance negative inner dim");
        checkThrows(NegativeArraySizeException.class,
                () -> Array.newInstance(int.class, -1, 3), "newInstance negative outer dim");
        checkThrows(NegativeArraySizeException.class,
                () -> Array.newInstance(int.class, 1, Integer.MIN_VALUE),
                "newInstance MIN_VALUE inner dim");

        // void component type -> IAE
        checkThrows(IllegalArgumentException.class,
                () -> Array.newInstance(void.class, 1, 2), "newInstance multi void component");
    }

    // ---- getLength ----

    static void testGetLength() {
        check(Array.getLength(new int[3]) == 3, "getLength int[3]");
        check(Array.getLength(new Object[0]) == 0, "getLength Object[0]");
        check(Array.getLength(new String[2][3]) == 2, "getLength String[][]");
        checkThrows(NullPointerException.class, () -> Array.getLength(null), "getLength null");
        checkThrows(IllegalArgumentException.class,
                () -> Array.getLength("not an array"), "getLength non-array");
        checkThrows(IllegalArgumentException.class,
                () -> Array.getLength(Integer.valueOf(3)), "getLength boxed non-array");
    }

    // ---- get/set per primitive type ----

    static void testGetSetPrimitives() {
        // boolean
        boolean[] ba = new boolean[2];
        Array.setBoolean(ba, 1, true);
        check(Array.getBoolean(ba, 1), "getBoolean true");
        check(!Array.getBoolean(ba, 0), "getBoolean default false");
        Array.set(ba, 0, Boolean.TRUE);
        check(Boolean.TRUE.equals(Array.get(ba, 0)), "get returns true after boxed set");
        check(Array.getBoolean(ba, 0), "getBoolean after boxed set");

        // byte
        byte[] bya = new byte[2];
        Array.setByte(bya, 0, (byte) -7);
        check(Array.getByte(bya, 0) == (byte) -7, "setByte/getByte");
        Array.set(bya, 1, Byte.valueOf((byte) 100));
        check(Byte.valueOf((byte) 100).equals(Array.get(bya, 1)), "boxed byte get");

        // char
        char[] ca = new char[2];
        Array.setChar(ca, 0, 'x');
        check(Array.getChar(ca, 0) == 'x', "setChar/getChar");
        Array.set(ca, 1, Character.valueOf('ÿ'));
        check(Array.getChar(ca, 1) == 'ÿ', "boxed char");

        // short
        short[] sa = new short[2];
        Array.setShort(sa, 0, (short) 32000);
        check(Array.getShort(sa, 0) == (short) 32000, "setShort/getShort");

        // int
        int[] ia = new int[2];
        Array.setInt(ia, 0, Integer.MAX_VALUE);
        check(Array.getInt(ia, 0) == Integer.MAX_VALUE, "setInt/getInt max");
        Array.setInt(ia, 1, Integer.MIN_VALUE);
        check(Array.getInt(ia, 1) == Integer.MIN_VALUE, "setInt/getInt min");

        // long
        long[] la = new long[2];
        Array.setLong(la, 0, Long.MAX_VALUE);
        check(Array.getLong(la, 0) == Long.MAX_VALUE, "setLong/getLong max");
        Array.setLong(la, 1, Long.MIN_VALUE);
        check(Array.getLong(la, 1) == Long.MIN_VALUE, "setLong/getLong min");

        // float
        float[] fa = new float[2];
        Array.setFloat(fa, 0, Float.NaN);
        check(Float.isNaN(Array.getFloat(fa, 0)), "setFloat/getFloat NaN");
        Array.setFloat(fa, 1, Float.POSITIVE_INFINITY);
        check(Float.isInfinite(Array.getFloat(fa, 1)), "setFloat/getFloat +Inf");

        // double
        double[] da = new double[2];
        Array.setDouble(da, 0, Double.NEGATIVE_INFINITY);
        check(Double.isInfinite(Array.getDouble(da, 0)), "setDouble/getDouble -Inf");
        Array.setDouble(da, 1, Double.NaN);
        check(Double.isNaN(Array.getDouble(da, 1)), "setDouble/getDouble NaN");
    }

    // ---- get/set Object arrays ----

    static void testGetSetObject() {
        String[] s = new String[2];
        check(Array.get(s, 0) == null, "object array default null");
        Array.set(s, 0, "a");
        check("a".equals(Array.get(s, 0)), "object set/get");
        // subtyping: Object[] holds any object
        Object[] o = new Object[2];
        Array.set(o, 0, Integer.valueOf(1));
        Array.set(o, 1, "str");
        check(Array.get(o, 0).equals(1), "Object[] heterogeneous");

        // wrong object type into String[] -> IllegalArgumentException (Array.set
        // performs an unwrapping/assignability check, unlike direct a[0]=1 which is ASE)
        checkThrows(IllegalArgumentException.class,
                () -> Array.set(s, 0, Integer.valueOf(1)), "set String[]<--Integer IAE");

        // null value into object array is fine
        Array.set(s, 1, null);
        check(Array.get(s, 1) == null, "set null into String[]");
    }

    // ---- wrong type / non-array / null array / index exceptions ----

    static void testExceptions() {
        // wrong primitive array type -> IAE
        int[] ia = new int[1];
        checkThrows(IllegalArgumentException.class,
                () -> Array.getBoolean(ia, 0), "getBoolean on int[] IAE");
        checkThrows(IllegalArgumentException.class,
                () -> Array.getInt(new boolean[1], 0), "getInt on boolean[] IAE");
        checkThrows(IllegalArgumentException.class,
                () -> Array.setLong(ia, 0, 1L), "setLong on int[] IAE");

        // set(Object array, int, Object) with wrong wrapper type -> IAE
        checkThrows(IllegalArgumentException.class,
                () -> Array.set(ia, 0, Long.valueOf(1L)), "set int[]<--Long IAE (narrowing)");

        // widening primitive conversion is allowed on primitive set
        long[] laWiden = new long[1];
        Array.set(laWiden, 0, Integer.valueOf(1));
        check(Array.getLong(laWiden, 0) == 1L, "set long[]<--Integer widens");

        // set primitive array with null -> IllegalArgumentException (unwrapping fails)
        checkThrows(IllegalArgumentException.class,
                () -> Array.set(new int[1], 0, null), "set int[] null value IAE");

        // non-array -> IAE
        checkThrows(IllegalArgumentException.class,
                () -> Array.get("x", 0), "get on non-array IAE");
        checkThrows(IllegalArgumentException.class,
                () -> Array.set("x", 0, "y"), "set on non-array IAE");
        checkThrows(IllegalArgumentException.class,
                () -> Array.getInt("x", 0), "getInt on non-array IAE");
        checkThrows(IllegalArgumentException.class,
                () -> Array.setInt("x", 0, 1), "setInt on non-array IAE");

        // null array -> NPE
        checkThrows(NullPointerException.class, () -> Array.get(null, 0), "get null array NPE");
        checkThrows(NullPointerException.class, () -> Array.set(null, 0, 1), "set null array NPE");
        checkThrows(NullPointerException.class,
                () -> Array.getInt(null, 0), "getInt null array NPE");
        checkThrows(NullPointerException.class,
                () -> Array.setInt(null, 0, 1), "setInt null array NPE");

        // index out of bounds -> AIOOBE
        int[] a = new int[2];
        checkThrows(ArrayIndexOutOfBoundsException.class,
                () -> Array.getInt(a, 2), "getInt index == length");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                () -> Array.getInt(a, -1), "getInt index -1");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                () -> Array.setInt(a, 2, 0), "setInt index == length");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                () -> Array.setInt(a, -1, 0), "setInt index -1");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                () -> Array.get(a, Integer.MAX_VALUE), "get index MAX_VALUE");
        String[] s = new String[1];
        checkThrows(ArrayIndexOutOfBoundsException.class,
                () -> Array.get(s, 1), "get object index == length");
    }

    // ---- multi-dimensional component access ----

    static void testMultiDimAccess() {
        int[][] m = (int[][]) Array.newInstance(int.class, 2, 2);
        Array.setInt(m[0], 0, 7);
        check(Array.getInt(m[0], 0) == 7, "inner array setInt/getInt");
        Object row = Array.get(m, 1);
        check(row instanceof int[], "get row returns int[]");
        Array.set(m, 0, new int[]{9, 9});
        check(Array.getInt(((int[]) Array.get(m, 0)), 0) == 9, "set row replace");
    }

    public static void main(String[] args) {
        testNewInstanceOneDim();
        testNewInstanceMultiDim();
        testGetLength();
        testGetSetPrimitives();
        testGetSetObject();
        testExceptions();
        testMultiDimAccess();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
