/*
 * @test
 * @summary java.lang.reflect.Array conformance: full contract coverage.
 *          newInstance (primitive/object/multi-dim) incl. negative/zero/null/void
 *          dimensions with exact exceptions, getLength/get/set across all 8 primitive
 *          types + Object arrays, boxing/unboxing, wrong-type -> IllegalArgumentException,
 *          index -> ArrayIndexOutOfBoundsException, null array -> NullPointerException,
 *          non-array -> IllegalArgumentException, ArrayStoreException for object arrays,
 *          multi-dimensional component access.
 *          Deepening batch: int[]-dims-array newInstance forms, boxed get/set matrix for
 *          all 8 primitives, the full JLS widening-getter/widening-setter lattice with
 *          exact values, narrowing refusals with exact "argument type mismatch" /
 *          "Argument is not an array" messages, and index-bounds exactness (AIOOBE,
 *          measured message-null on JDK-8 and JDK-27).
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

    /** Exact exception class AND message containing the measured fragment (2 assertions). */
    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
            String fragment, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            check(expected.isInstance(t), msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName());
            check(t.getMessage() != null && t.getMessage().contains(fragment),
                    msg + ": message [" + t.getMessage() + "] missing \"" + fragment + "\"");
            return;
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    /** AIOOBE exactly (subclass relationship + measured message-less form). */
    static void checkAioobe(ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            check(t.getClass() == ArrayIndexOutOfBoundsException.class, msg + " exact AIOOBE class");
            check(t instanceof IndexOutOfBoundsException, msg + " is an IndexOutOfBoundsException");
            check(t.getMessage() == null, msg + " carries no message detail (measured)");
            return;
        }
        throw new AssertionError(msg + ": expected ArrayIndexOutOfBoundsException but nothing was thrown");
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

    // ---- deepening batch: newInstance via int[] dimensions ----

    static void testNewInstanceDimsForm() {
        Object one = Array.newInstance(int.class, new int[]{3});
        check(one.getClass() == int[].class, "dims-array single-dim class");
        check(Array.getLength(one) == 3, "dims-array single-dim length");
        int[][] m = (int[][]) Array.newInstance(int.class, new int[]{2, 3});
        check(m.getClass() == int[][].class, "dims-array 2-dim class");
        check(m.length == 2 && m[0].length == 3 && m[1].length == 3, "dims-array 2-dim shape");
        String[][] sm = (String[][]) Array.newInstance(String.class, new int[]{2, 3});
        check(sm.getClass() == String[][].class, "dims-array String 2-dim class");
        check(sm[0] instanceof String[], "dims-array String inner type");
        Array.set(sm[1], 2, "z");
        check("z".equals(sm[1][2]), "dims-array element write/read");
        int[][] zeroInner = (int[][]) Array.newInstance(int.class, new int[]{2, 0});
        check(zeroInner.length == 2 && zeroInner[0].length == 0, "zero inner dimension allowed");
    }

    // ---- deepening batch: boxed get/set matrix for all 8 primitives ----

    static void testBoxedGetSetMatrix() {
        boolean[] ba = {true};
        check(Array.get(ba, 0) instanceof Boolean && (Boolean) Array.get(ba, 0), "get(boolean[]) boxed");
        byte[] bya = {(byte) -3};
        check(Array.get(bya, 0) instanceof Byte
                && ((Byte) Array.get(bya, 0)).byteValue() == (byte) -3, "get(byte[]) boxed");
        char[] ca = {'q'};
        check(Array.get(ca, 0) instanceof Character
                && ((Character) Array.get(ca, 0)).charValue() == 'q', "get(char[]) boxed");
        short[] sha = {(short) -300};
        check(Array.get(sha, 0) instanceof Short
                && ((Short) Array.get(sha, 0)).shortValue() == (short) -300, "get(short[]) boxed");
        int[] ia = {-70000};
        check(Array.get(ia, 0) instanceof Integer
                && ((Integer) Array.get(ia, 0)).intValue() == -70000, "get(int[]) boxed");
        long[] la = {-1L << 40};
        check(Array.get(la, 0) instanceof Long
                && ((Long) Array.get(la, 0)).longValue() == (-1L << 40), "get(long[]) boxed");
        float[] fa = {0.25f};
        check(Array.get(fa, 0) instanceof Float
                && ((Float) Array.get(fa, 0)).floatValue() == 0.25f, "get(float[]) boxed");
        double[] da = {0.125};
        check(Array.get(da, 0) instanceof Double
                && ((Double) Array.get(da, 0)).doubleValue() == 0.125, "get(double[]) boxed");

        // boxed writes read back through the matching typed accessor
        Array.set(ba, 0, Boolean.FALSE);
        check(!Array.getBoolean(ba, 0), "set(boolean[],Boolean) round-trip");
        Array.set(bya, 0, Byte.valueOf((byte) 9));
        check(Array.getByte(bya, 0) == 9, "set(byte[],Byte) round-trip");
        Array.set(ca, 0, Character.valueOf('Z'));
        check(Array.getChar(ca, 0) == 'Z', "set(char[],Character) round-trip");
        Array.set(sha, 0, Short.valueOf((short) 1234));
        check(Array.getShort(sha, 0) == 1234, "set(short[],Short) round-trip");
        Array.set(ia, 0, Integer.valueOf(-98765));
        check(Array.getInt(ia, 0) == -98765, "set(int[],Integer) round-trip");
        Array.set(la, 0, Long.valueOf(Long.MIN_VALUE));
        check(Array.getLong(la, 0) == Long.MIN_VALUE, "set(long[],Long) round-trip");
        Array.set(fa, 0, Float.valueOf(6.75f));
        check(Array.getFloat(fa, 0) == 6.75f, "set(float[],Float) round-trip");
        Array.set(da, 0, Double.valueOf(-2.25));
        check(Array.getDouble(da, 0) == -2.25, "set(double[],Double) round-trip");
    }

    // ---- deepening batch: widening-getter lattice (JLS 5.1.2) ----

    static void testWideningGetters() {
        byte[] bya = {(byte) -5};
        check(Array.getShort(bya, 0) == (short) -5, "byte->short");
        check(Array.getInt(bya, 0) == -5, "byte->int");
        check(Array.getLong(bya, 0) == -5L, "byte->long");
        check(Array.getFloat(bya, 0) == -5f, "byte->float");
        check(Array.getDouble(bya, 0) == -5.0, "byte->double");

        short[] sha = {(short) -1000};
        check(Array.getInt(sha, 0) == -1000, "short->int");
        check(Array.getLong(sha, 0) == -1000L, "short->long");
        check(Array.getFloat(sha, 0) == -1000f, "short->float");
        check(Array.getDouble(sha, 0) == -1000.0, "short->double");

        char[] ca = {'ሴ'};
        check(Array.getInt(ca, 0) == 0x1234, "char->int");
        check(Array.getLong(ca, 0) == 0x1234L, "char->long");
        check(Array.getFloat(ca, 0) == (float) 0x1234, "char->float");
        check(Array.getDouble(ca, 0) == (double) 0x1234, "char->double");

        int[] ia = {-100000};
        check(Array.getLong(ia, 0) == -100000L, "int->long");
        check(Array.getFloat(ia, 0) == -100000f, "int->float");
        check(Array.getDouble(ia, 0) == -100000.0, "int->double");

        long[] la = {1L << 33};
        check(Array.getFloat(la, 0) == (float) (1L << 33), "long->float");
        check(Array.getDouble(la, 0) == (double) (1L << 33), "long->double");

        float[] fa = {1.5f};
        check(Array.getDouble(fa, 0) == 1.5, "float->double");
    }

    // ---- deepening batch: widening-setter lattice + boxed widening ----

    static void testWideningSetters() {
        long[] la = new long[1];
        Array.setByte(la, 0, (byte) 1);
        check(la[0] == 1L, "long[]<--byte");
        Array.setShort(la, 0, (short) 2);
        check(la[0] == 2L, "long[]<--short");
        Array.setChar(la, 0, 'c');
        check(la[0] == 99L, "long[]<--char");
        Array.setInt(la, 0, 4);
        check(la[0] == 4L, "long[]<--int");

        int[] ia = new int[1];
        Array.setByte(ia, 0, (byte) 5);
        check(ia[0] == 5, "int[]<--byte");
        Array.setShort(ia, 0, (short) 6);
        check(ia[0] == 6, "int[]<--short");
        Array.setChar(ia, 0, 'x');
        check(ia[0] == 120, "int[]<--char");

        short[] sa = new short[1];
        Array.setByte(sa, 0, (byte) 7);
        check(sa[0] == 7, "short[]<--byte");

        float[] fa = new float[1];
        Array.setByte(fa, 0, (byte) 1);
        check(fa[0] == 1f, "float[]<--byte");
        Array.setShort(fa, 0, (short) 2);
        check(fa[0] == 2f, "float[]<--short");
        Array.setChar(fa, 0, 'c');
        check(fa[0] == 99f, "float[]<--char");
        Array.setInt(fa, 0, 3);
        check(fa[0] == 3f, "float[]<--int");
        Array.setLong(fa, 0, 4L);
        check(fa[0] == 4f, "float[]<--long");

        double[] da = new double[1];
        Array.setByte(da, 0, (byte) 1);
        check(da[0] == 1.0, "double[]<--byte");
        Array.setShort(da, 0, (short) 2);
        check(da[0] == 2.0, "double[]<--short");
        Array.setChar(da, 0, 'c');
        check(da[0] == 99.0, "double[]<--char");
        Array.setInt(da, 0, 3);
        check(da[0] == 3.0, "double[]<--int");
        Array.setLong(da, 0, 4L);
        check(da[0] == 4.0, "double[]<--long");
        Array.setFloat(da, 0, 5.5f);
        check(da[0] == 5.5, "double[]<--float");

        // boxed widening through Array.set(Object,int,Object)
        Array.set(la, 0, Integer.valueOf(11));
        check(la[0] == 11L, "long[]<--Integer box");
        Array.set(ia, 0, Byte.valueOf((byte) 12));
        check(ia[0] == 12, "int[]<--Byte box");
        Array.set(ia, 0, Character.valueOf('x'));
        check(ia[0] == 120, "int[]<--Character box");
        Array.set(sa, 0, Byte.valueOf((byte) 13));
        check(sa[0] == 13, "short[]<--Byte box");
        Array.set(da, 0, Long.MAX_VALUE);
        check(da[0] == 9.223372036854776E18, "double[]<--Long box");
    }

    // ---- deepening batch: narrowing refusals with exact messages ----

    static void testNarrowingRefusalMessages() {
        long[] la = {1L};
        double[] da = {1.0};
        boolean[] ba = {true};
        int[] ia = {1};
        byte[] bya = {1};
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.getInt(la, 0),
                "argument type mismatch", "getInt(long[]) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.getFloat(da, 0),
                "argument type mismatch", "getFloat(double[]) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.getChar(da, 0),
                "argument type mismatch", "getChar(double[]) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.getInt(ba, 0),
                "argument type mismatch", "getInt(boolean[]) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.setBoolean(ia, 0, true),
                "argument type mismatch", "setBoolean(int[]) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.setFloat(ia, 0, 1f),
                "argument type mismatch", "setFloat(int[]) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.set(ia, 0, "x"),
                "argument type mismatch", "set(int[],String) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.set(ia, 0, Long.valueOf(5L)),
                "argument type mismatch", "set(int[],Long) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.setInt(bya, 0, 1),
                "argument type mismatch", "setInt(byte[]) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.set(bya, 0, Integer.valueOf(1)),
                "argument type mismatch", "set(byte[],Integer) message");
    }

    // ---- deepening batch: non-array argument messages ----

    static void testNonArrayMessages() {
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.getLength("x"),
                "Argument is not an array", "getLength(String) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.get("x", 0),
                "Argument is not an array", "get(String) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.set("x", 0, "y"),
                "Argument is not an array", "set(String) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.getInt("x", 0),
                "Argument is not an array", "getInt(String) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.getBoolean("x", 0),
                "Argument is not an array", "getBoolean(String) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.setDouble("x", 0, 1.0),
                "Argument is not an array", "setDouble(String) message");
        checkThrowsMsg(IllegalArgumentException.class, () -> Array.getLength(new Object()),
                "Argument is not an array", "getLength(Object) message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> Array.getLength(java.util.Collections.emptyList()),
                "Argument is not an array", "getLength(List) message");
    }

    // ---- deepening batch: index bounds exactness ----

    static void testIndexBoundsExact() {
        int[] a = {0, 0};
        checkAioobe(() -> Array.get(a, -1), "get(a, -1)");
        checkAioobe(() -> Array.set(a, -1, Integer.valueOf(1)), "set(a, -1)");
        checkAioobe(() -> Array.getInt(a, 2), "getInt(a, 2)");
        checkAioobe(() -> Array.setInt(a, 2, 0), "setInt(a, 2)");
        checkAioobe(() -> Array.getBoolean(new boolean[1], -3), "getBoolean neg index");
        checkAioobe(() -> Array.setDouble(new double[1], 1, 0.0), "setDouble at length");
        checkAioobe(() -> Array.get(a, Integer.MAX_VALUE), "get MAX_VALUE index");
        checkAioobe(() -> Array.getInt(a, Integer.MIN_VALUE), "getInt MIN_VALUE index");
        checkAioobe(() -> Array.get(new String[1], 1), "object array at length");
        checkAioobe(() -> Array.getChar(new char[0], 0), "empty array index 0");
    }

    // ---- deepening batch: null array NPE across the full accessor family ----

    static void testNullArrayNpeMatrix() {
        checkThrows(NullPointerException.class, () -> Array.getBoolean(null, 0), "getBoolean null NPE");
        checkThrows(NullPointerException.class, () -> Array.getByte(null, 0), "getByte null NPE");
        checkThrows(NullPointerException.class, () -> Array.getChar(null, 0), "getChar null NPE");
        checkThrows(NullPointerException.class, () -> Array.getShort(null, 0), "getShort null NPE");
        checkThrows(NullPointerException.class, () -> Array.getLong(null, 0), "getLong null NPE");
        checkThrows(NullPointerException.class, () -> Array.getFloat(null, 0), "getFloat null NPE");
        checkThrows(NullPointerException.class, () -> Array.getDouble(null, 0), "getDouble null NPE");
        checkThrows(NullPointerException.class, () -> Array.setBoolean(null, 0, true), "setBoolean null NPE");
        checkThrows(NullPointerException.class, () -> Array.setByte(null, 0, (byte) 1), "setByte null NPE");
        checkThrows(NullPointerException.class, () -> Array.setChar(null, 0, 'c'), "setChar null NPE");
        checkThrows(NullPointerException.class, () -> Array.setShort(null, 0, (short) 1), "setShort null NPE");
        checkThrows(NullPointerException.class, () -> Array.setFloat(null, 0, 1f), "setFloat null NPE");
        checkThrows(NullPointerException.class, () -> Array.setDouble(null, 0, 1.0), "setDouble null NPE");
    }

    // ---- deepening batch: multi-dimensional model ----

    static void testMultiDimModel() {
        String[][][] cube = (String[][][]) Array.newInstance(String.class, new int[]{1, 2, 3});
        check(cube.length == 1 && cube[0].length == 2 && cube[0][0].length == 3,
                "3-dim dims-array shape");
        Object plane = Array.get(cube, 0);
        check(plane instanceof String[][], "outer get returns String[][]");
        check(Array.getLength(plane) == 2, "plane length via reflective getLength");
        Object line = Array.get(plane, 1);
        check(line instanceof String[] && Array.getLength(line) == 3,
                "row via chained reflective get");
        Array.set((Object[]) plane, 0, new String[]{"a", "b", "c"});
        check("c".equals(cube[0][0][2]), "row replaced via Array.set visible in Java read");
        double[][] dd = (double[][]) Array.newInstance(double.class, 2, 2);
        Array.setDouble(dd[1], 1, 0.5);
        check(Array.getDouble((double[]) Array.get(dd, 1), 1) == 0.5,
                "nested typed write/read through reflective get");
        check(Array.get(dd, 0) instanceof double[], "primitive 2-dim outer get type");
    }

    public static void main(String[] args) {
        testNewInstanceOneDim();
        testNewInstanceMultiDim();
        testGetLength();
        testGetSetPrimitives();
        testGetSetObject();
        testExceptions();
        testMultiDimAccess();
        testNewInstanceDimsForm();
        testBoxedGetSetMatrix();
        testWideningGetters();
        testWideningSetters();
        testNarrowingRefusalMessages();
        testNonArrayMessages();
        testIndexBoundsExact();
        testNullArrayNpeMatrix();
        testMultiDimModel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
