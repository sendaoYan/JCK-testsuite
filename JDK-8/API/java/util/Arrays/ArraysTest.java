/*
 * @test
 * @summary java.util.Arrays API conformance: sort (primitives, Object natural, with
 *          Comparator, ranges, parallelSort), binarySearch return values, equals/
 *          deepEquals, fill, copyOf/copyOfRange (truncate/pad), asList (fixed-size view),
 *          and toString/deepToString/hashCode.
 * @run main ArraysTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;

public class ArraysTest {
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

    static void testSort() {
        int[] a = {5, 2, 4, 1, 3};
        Arrays.sort(a);
        check(Arrays.equals(a, new int[]{1, 2, 3, 4, 5}), "sort(int[])");

        int[] b = {5, 2, 4, 1, 3};
        Arrays.sort(b, 1, 4);
        check(Arrays.equals(b, new int[]{5, 1, 2, 4, 3}), "sort(int[], range)");

        String[] s = {"banana", "apple", "cherry"};
        Arrays.sort(s);
        check(Arrays.equals(s, new String[]{"apple", "banana", "cherry"}),
              "sort(Object[]) natural");

        String[] t = {"banana", "apple", "cherry"};
        Arrays.sort(t, Comparator.comparingInt(String::length));
        check(Arrays.equals(t, new String[]{"apple", "banana", "cherry"}),
              "sort(T[], Comparator) by length");

        int[] p = {5, 2, 4, 1, 3};
        Arrays.parallelSort(p);
        check(Arrays.equals(p, new int[]{1, 2, 3, 4, 5}), "parallelSort(int[])");
    }

    static void testBinarySearch() {
        int[] a = {1, 3, 5, 7, 9};
        check(Arrays.binarySearch(a, 3) == 1, "binarySearch found");
        check(Arrays.binarySearch(a, 2) == -2, "binarySearch insertion point");
        check(Arrays.binarySearch(a, 0) == -1, "binarySearch before first");
        check(Arrays.binarySearch(a, 9) == 4, "binarySearch last");
        check(Arrays.binarySearch(a, 10) == -6, "binarySearch after last");
        check(Arrays.binarySearch(a, 1, 3, 5) == 2, "binarySearch in range");
    }

    static void testEqualsFill() {
        check(Arrays.equals(new int[]{1, 2, 3}, new int[]{1, 2, 3}), "equals int[]");
        check(!Arrays.equals(new int[]{1, 2, 3}, new int[]{1, 2, 4}), "!equals int[]");
        check(Arrays.equals(new String[]{"a", "b"}, new String[]{"a", "b"}), "equals Object[]");
        check(Arrays.deepEquals(new int[][]{{1, 2}, {3}}, new int[][]{{1, 2}, {3}}),
              "deepEquals nested");
        check(!Arrays.deepEquals(new int[][]{{1, 2}}, new int[][]{{1, 3}}),
              "!deepEquals nested");

        int[] f = new int[4];
        Arrays.fill(f, 7);
        check(Arrays.equals(f, new int[]{7, 7, 7, 7}), "fill");
        Arrays.fill(f, 1, 3, 9);
        check(Arrays.equals(f, new int[]{7, 9, 9, 7}), "fill range");
    }

    static void testCopy() {
        int[] a = {1, 2, 3};
        check(Arrays.equals(Arrays.copyOf(a, 5), new int[]{1, 2, 3, 0, 0}),
              "copyOf pads");
        check(Arrays.equals(Arrays.copyOf(a, 2), new int[]{1, 2}), "copyOf truncates");
        check(Arrays.equals(Arrays.copyOfRange(a, 1, 3), new int[]{2, 3}), "copyOfRange");
        check(Arrays.copyOf(a, 3) != a, "copyOf returns new array");
        check(Arrays.copyOf(a, 0).length == 0, "copyOf to length 0");
    }

    static void testAsList() {
        String[] arr = {"a", "b", "c"};
        java.util.List<String> list = Arrays.asList(arr);
        check(list.size() == 3 && list.get(1).equals("b"), "asList content");
        list.set(0, "A");
        check(arr[0].equals("A"), "asList.set reflects in backing array");
        arr[1] = "B";
        check(list.get(1).equals("B"), "backing array change reflects in list");
        try {
            list.add("d");
            throw new AssertionError("asList.add: expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            passed++;
        }
    }

    static void testToStringHash() {
        check(Arrays.toString(new int[]{1, 2, 3}).equals("[1, 2, 3]"), "toString(int[])");
        check(Arrays.toString(new String[]{"a", "b"}).equals("[a, b]"), "toString(Object[])");
        check(Arrays.deepToString(new int[][]{{1, 2}, {3}}).equals("[[1, 2], [3]]"),
              "deepToString");
        check(Arrays.hashCode(new int[]{1, 2}) == Arrays.hashCode(new int[]{1, 2}),
              "hashCode stable");
        check(Arrays.hashCode(new int[]{1, 2}) != Arrays.hashCode(new int[]{1, 3}),
              "hashCode varies");
    }

    // ---- deepened contract coverage ----------------------------------------

    static void testSortEdgeCases() {
        checkThrows(NullPointerException.class, () -> Arrays.sort((int[]) null), "sort((int[])null) NPE");
        checkThrows(NullPointerException.class, () -> Arrays.sort((Object[]) null), "sort((Object[])null) NPE");

        final int[] a = {3, 1, 2};
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> Arrays.sort(a, -1, 2), "sort(-1,2) AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> Arrays.sort(a, 0, 4), "sort(0,4) AIOOBE");
        checkThrows(IllegalArgumentException.class, () -> Arrays.sort(a, 2, 1), "sort(2,1) IAE");

        // parallelSort range boundary
        final int[] p = {5, 2, 4, 1, 3};
        Arrays.parallelSort(p, 1, 4);
        check(Arrays.equals(p, new int[]{5, 1, 2, 4, 3}), "parallelSort range");

        // sort(Object[]) with non-Comparable elements -> ClassCastException
        final Object[] objs = { new Object(), new Object() };
        checkThrows(ClassCastException.class, () -> Arrays.sort(objs), "sort non-Comparable CCE");
    }

    static void testBinarySearchEdgeCases() {
        final int[] a = {1, 3, 5, 7, 9};
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> Arrays.binarySearch(a, -1, 3, 5),
                    "binarySearch(-1,3,5) AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> Arrays.binarySearch(a, 0, 6, 5),
                    "binarySearch(0,6,5) AIOOBE");
        checkThrows(IllegalArgumentException.class, () -> Arrays.binarySearch(a, 3, 2, 5),
                    "binarySearch(3,2,5) IAE");
        checkThrows(NullPointerException.class, () -> Arrays.binarySearch((int[]) null, 1),
                    "binarySearch(null) NPE");

        // binarySearch(Object[], Object) with null key -> NPE
        final String[] s = {"a", "b"};
        checkThrows(NullPointerException.class, () -> Arrays.binarySearch(s, null), "binarySearch null key NPE");
    }

    static void testCopyEdgeCases() {
        final int[] a = {1, 2, 3};
        checkThrows(NegativeArraySizeException.class, () -> Arrays.copyOf(a, -1), "copyOf(-1) NASE");
        checkThrows(IllegalArgumentException.class, () -> Arrays.copyOfRange(a, 2, 1), "copyOfRange(2,1) IAE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> Arrays.copyOfRange(a, -1, 2),
                    "copyOfRange(-1,2) AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> Arrays.copyOfRange(a, 4, 4),
                    "copyOfRange(from>length) AIOOBE");
        checkThrows(NullPointerException.class, () -> Arrays.copyOf((int[]) null, 1), "copyOf(null) NPE");

        // copyOfRange with to > length pads (no exception)
        check(Arrays.equals(Arrays.copyOfRange(a, 0, 4), new int[]{1, 2, 3, 0}), "copyOfRange to>length pads");
        check(Arrays.copyOfRange(a, 1, 1).length == 0, "copyOfRange(i,i) empty");
    }

    static void testFillEdgeCases() {
        final int[] a = new int[4];
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> Arrays.fill(a, -1, 2, 9), "fill(-1,2,9) AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> Arrays.fill(a, 0, 5, 9), "fill(0,5,9) AIOOBE");
        checkThrows(IllegalArgumentException.class, () -> Arrays.fill(a, 2, 1, 9), "fill(2,1,9) IAE");
    }

    static void testEqualsDeepEdge() {
        check(Arrays.equals(new Object[]{null}, new Object[]{null}), "equals arrays with null elements");
        check(!Arrays.equals(new Object[]{null}, new Object[]{"x"}), "!equals null vs value");
        check(Arrays.deepEquals(new Object[]{null}, new Object[]{null}), "deepEquals null elements");
        check(Arrays.deepEquals(new Object[]{new int[]{1}}, new Object[]{new int[]{1}}),
              "deepEquals nested arrays");
        check(Arrays.deepEquals(new Object[]{new int[]{1}}, new Object[]{new int[]{2}}) == false,
              "!deepEquals nested different");
        check(Arrays.hashCode(new int[0]) == 1, "hashCode of empty array is 1");
    }

    static void testAsListEdge() {
        // asList on a primitive array yields a single-element list (List<int[]>)
        int[] prim = {1, 2, 3};
        List<int[]> primList = Arrays.asList(prim);
        check(primList.size() == 1 && primList.get(0) == prim, "asList(primitive) single element");

        String[] arr = {"a", "b", "c"};
        List<String> list = Arrays.asList(arr);
        checkThrows(UnsupportedOperationException.class, () -> list.add("d"), "asList.add UOE");
        checkThrows(UnsupportedOperationException.class, () -> list.remove(0), "asList.remove UOE");
        checkThrows(UnsupportedOperationException.class, () -> list.clear(), "asList.clear UOE");
        check(list.contains("a") && list.indexOf("b") == 1, "asList query methods work");

        checkThrows(NullPointerException.class, () -> Arrays.asList((Object[]) null), "asList(null) NPE");
    }

    static void testToStringHashEdge() {
        check(Arrays.toString(new Object[]{null}).equals("[null]"), "toString with null element");
        check(Arrays.deepToString(new Object[]{null}).equals("[null]"), "deepToString with null");
        check(Arrays.deepToString(new Object[]{new int[]{1, 2}}).equals("[[1, 2]]"), "deepToString nested");
        check(Arrays.hashCode(new int[]{1, 2}) == Arrays.hashCode(new int[]{1, 2}), "hashCode stable");
        check(Arrays.deepHashCode(new Object[]{new int[]{1}}) == Arrays.deepHashCode(new Object[]{new int[]{1}}),
              "deepHashCode stable");
    }

    // ---- deepened contract coverage, round 2 (empirically locked on jdk8u372) --

    static void testSortAllPrimitiveTypes() {
        byte[] b = {3, 1, 2};
        Arrays.sort(b);
        check(Arrays.equals(b, new byte[]{1, 2, 3}), "sort(byte[])");
        short[] sh = {3, 1, 2};
        Arrays.sort(sh);
        check(Arrays.equals(sh, new short[]{1, 2, 3}), "sort(short[])");
        char[] c = {'c', 'a', 'b'};
        Arrays.sort(c);
        check(Arrays.equals(c, new char[]{'a', 'b', 'c'}), "sort(char[])");
        long[] l = {3L, 1L, 2L};
        Arrays.sort(l);
        check(Arrays.equals(l, new long[]{1L, 2L, 3L}), "sort(long[])");
        float[] f = {3f, 1f, 2f};
        Arrays.sort(f);
        check(Arrays.equals(f, new float[]{1f, 2f, 3f}), "sort(float[])");
        double[] d = {3.0, 1.0, 2.0};
        Arrays.sort(d);
        check(Arrays.equals(d, new double[]{1.0, 2.0, 3.0}), "sort(double[])");
        char[] cr = {'d', 'c', 'b', 'a'};
        Arrays.sort(cr, 0, 3);
        check(Arrays.equals(cr, new char[]{'b', 'c', 'd', 'a'}), "sort(char[], from, to)");
        double[] dr = {5.0, 2.0, 4.0, 1.0};
        Arrays.sort(dr, 1, 4);
        check(Arrays.equals(dr, new double[]{5.0, 1.0, 2.0, 4.0}), "sort(double[], from, to)");
        // object sort with comparator range
        String[] s = {"d", "c", "b", "a"};
        Arrays.sort(s, 0, 3, Comparator.reverseOrder());
        check(Arrays.equals(s, new String[]{"d", "c", "b", "a"}), "sort(T[], from, to, cmp)");
    }

    static void testSortStability() {
        // stability: objects with equal keys keep their relative insertion order
        String[][] pairs = {{"1", "first"}, {"2", "x"}, {"1", "second"}, {"2", "y"}, {"1", "third"}};
        Arrays.sort(pairs, Comparator.comparing(p -> p[0]));
        check(pairs[0][1].equals("first") && pairs[1][1].equals("second") && pairs[2][1].equals("third"),
              "object sort stable for equal keys (group 1)");
        check(pairs[3][1].equals("x") && pairs[4][1].equals("y"),
              "object sort stable for equal keys (group 2)");
        String[] st = {"bb", "aa", "c", "d"};
        Arrays.sort(st, Comparator.comparingInt(String::length));
        check(Arrays.equals(st, new String[]{"c", "d", "bb", "aa"}),
              "sort(T[], cmp) stable by length");
        Integer[] parallel = {5, 3, 4, 1, 2};
        Arrays.parallelSort(parallel);
        check(Arrays.equals(parallel, new Integer[]{1, 2, 3, 4, 5}), "parallelSort(T[]) natural");
    }

    static void testBinarySearchAllTypes() {
        check(Arrays.binarySearch(new char[]{'a', 'c'}, 'b') == -2, "binarySearch(char[]) miss");
        check(Arrays.binarySearch(new char[]{'a', 'c'}, 'c') == 1, "binarySearch(char[]) hit");
        check(Arrays.binarySearch(new byte[]{1, 3}, (byte) 2) == -2, "binarySearch(byte[]) miss");
        check(Arrays.binarySearch(new byte[]{1, 3}, (byte) 0) == -1, "binarySearch(byte[]) before first");
        check(Arrays.binarySearch(new short[]{1, 3}, (short) 5) == -3, "binarySearch(short[]) after last");
        check(Arrays.binarySearch(new long[]{1L, 3L}, 4L) == -3, "binarySearch(long[]) after last");
        check(Arrays.binarySearch(new long[]{1L, 3L}, 2L) == -2, "binarySearch(long[]) gap");
        check(Arrays.binarySearch(new float[]{1f, 3f}, 5f) == -3, "binarySearch(float[]) after last");
        check(Arrays.binarySearch(new double[]{1.0, 3.0}, 0.5) == -1, "binarySearch(double[]) before first");
        check(Arrays.binarySearch(new double[]{1.0, 3.0}, 2.0) == -2, "binarySearch(double[]) gap");
        check(Arrays.binarySearch(new String[]{"a", "c"}, "b") == -2, "binarySearch(Object[]) gap");
        check(Arrays.binarySearch(new String[]{"a", "c"}, "a") == 0, "binarySearch(Object[]) hit");
        // reverse-comparator-sorted array
        check(Arrays.binarySearch(new String[]{"c", "a"}, "b", Comparator.reverseOrder()) == -2,
              "binarySearch(Object[], cmp) gap in descending order");
        check(Arrays.binarySearch(new String[]{"c", "a"}, "c", Comparator.reverseOrder()) == 0,
              "binarySearch(Object[], cmp) hit in descending order");
        // full insertion formula on a five-element array
        int[] five = {2, 4, 6, 8, 10};
        check(Arrays.binarySearch(five, 1) == -1, "five: before first");
        check(Arrays.binarySearch(five, 3) == -2, "five: gap 0");
        check(Arrays.binarySearch(five, 5) == -3, "five: gap 1");
        check(Arrays.binarySearch(five, 7) == -4, "five: gap 2");
        check(Arrays.binarySearch(five, 9) == -5, "five: gap 3");
        check(Arrays.binarySearch(five, 11) == -6, "five: after last -(size+1)");
    }

    static void testStreamRanges() {
        check(Arrays.stream(new int[]{1, 2, 3}).sum() == 6, "stream(int[]) sum");
        check(Arrays.stream(new long[]{1, 2}).sum() == 3L, "stream(long[]) sum");
        check(Math.abs(Arrays.stream(new double[]{1.5, 2.5}).sum() - 4.0) < 1e-12, "stream(double[]) sum");
        check(Arrays.stream(new String[]{"a", "b"}).count() == 2, "stream(T[]) count");
        check(Arrays.equals(Arrays.stream(new int[]{1, 2, 3, 4}, 1, 3).toArray(), new int[]{2, 3}),
              "stream(int[], from, to) slice");
        check(Arrays.stream(new String[]{"a", "b", "c"}, 1, 3).reduce("", String::concat).equals("bc"),
              "stream(T[], from, to) slice");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                    () -> Arrays.stream(new int[]{1, 2, 3}, 2, 1),
                    "stream(int[], from>to) AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                    () -> Arrays.stream(new int[]{1, 2, 3}, -1, 2),
                    "stream(int[], from<0) AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                    () -> Arrays.stream(new int[]{1, 2, 3}, 1, 5),
                    "stream(int[], to>length) AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                    () -> Arrays.stream(new String[]{"a"}, 2, 1),
                    "stream(T[], from>to) AIOOBE");
    }

    static void testParallelPrefixSetAll() {
        int[] ia = {1, 2, 3, 4, 5};
        Arrays.parallelPrefix(ia, Integer::sum);
        check(Arrays.equals(ia, new int[]{1, 3, 6, 10, 15}), "parallelPrefix(int[]) cumulative");
        int[] ir = {1, 2, 3, 4, 5};
        Arrays.parallelPrefix(ir, 1, 4, Integer::sum);
        check(Arrays.equals(ir, new int[]{1, 2, 5, 9, 5}), "parallelPrefix(int[], range)");
        long[] la = {1, 2, 3, 4};
        Arrays.parallelPrefix(la, (x, y) -> x * y);
        check(Arrays.equals(la, new long[]{1, 2, 6, 24}), "parallelPrefix(long[]) product");
        double[] da = {1.0, 2.0, 3.0};
        Arrays.parallelPrefix(da, Double::sum);
        check(Arrays.equals(da, new double[]{1.0, 3.0, 6.0}), "parallelPrefix(double[]) sum");
        String[] sa = {"a", "b", "c"};
        Arrays.parallelPrefix(sa, String::concat);
        check(Arrays.equals(sa, new String[]{"a", "ab", "abc"}), "parallelPrefix(Object[]) concat");
        String[] sr = {"a", "b", "c", "d"};
        Arrays.parallelPrefix(sr, 1, 3, String::concat);
        check(Arrays.equals(sr, new String[]{"a", "b", "bc", "d"}), "parallelPrefix(Object[], range)");
        String[] gen = new String[3];
        Arrays.setAll(gen, i -> "n" + i);
        check(Arrays.equals(gen, new String[]{"n0", "n1", "n2"}), "setAll(T[])");
        int[] pg = new int[4];
        Arrays.parallelSetAll(pg, i -> i * i);
        check(Arrays.equals(pg, new int[]{0, 1, 4, 9}), "parallelSetAll(int[])");
    }

    static void testNullArrayContracts() {
        check(Arrays.equals((int[]) null, (int[]) null), "equals(int[] null, null) true");
        check(!Arrays.equals((int[]) null, new int[]{1}), "equals(int[] null, arr) false");
        check(!Arrays.equals(new int[]{1}, (int[]) null), "equals(arr, int[] null) false");
        check(Arrays.equals((Object[]) null, (Object[]) null), "equals(Object[] null, null) true");
        check(!Arrays.equals((String[]) null, new String[]{"a"}), "equals(String[] null, arr) false");
        check(Arrays.equals((double[]) null, (double[]) null), "equals(double[] null, null) true");
        check(Arrays.hashCode((int[]) null) == 0, "hashCode(int[] null) 0");
        check(Arrays.hashCode((double[]) null) == 0, "hashCode(double[] null) 0");
        check(Arrays.hashCode((Object[]) null) == 0, "hashCode(Object[] null) 0");
        check(Arrays.hashCode(new int[0]) == 1, "hashCode(empty int[]) 1");
        check(Arrays.toString((int[]) null).equals("null"), "toString(int[] null) \"null\"");
        check(Arrays.toString((double[]) null).equals("null"), "toString(double[] null) \"null\"");
        check(Arrays.toString((Object[]) null).equals("null"), "toString(Object[] null) \"null\"");
        check(Arrays.deepToString((Object[]) null).equals("null"), "deepToString(null) \"null\"");
        check(Arrays.deepHashCode((Object[]) null) == 0, "deepHashCode(null) 0");
        check(Arrays.deepHashCode(new Object[0]) == 1, "deepHashCode(empty) 1");
        check(Arrays.toString(new boolean[]{true, false}).equals("[true, false]"), "toString(boolean[])");
        check(Arrays.toString(new char[]{'a', 'b'}).equals("[a, b]"), "toString(char[])");
        check(Arrays.toString(new float[]{1.5f}).equals("[1.5]"), "toString(float[])");
    }

    static void testHashCodeFormula() {
        // Arrays.hashCode(int[]): result = 1; result = 31*result + element (Integer.hashCode)
        check(Arrays.hashCode(new int[]{1, 2}) == 31 * (31 * 1 + 1) + 2, "hashCode formula 994");
        check(Arrays.hashCode(new int[]{1, 2}) == 994, "hashCode hand-computed 994");
        // List.hashCode formula agrees: asList("a","b") -> 31*(31*1+97)+98 = 4066
        check(Arrays.asList("a", "b").hashCode() == 4066, "asList hashCode hand-computed 4066");
        check(Arrays.hashCode(new Object[]{"a", null}) == 31 * (31 * 1 + 97) + 0,
              "hashCode(Object[]) with null element");
        check(Arrays.hashCode(new Object[]{"a", "b"})
              == Arrays.hashCode(new String[]{"a", "b"}), "hashCode(Object[]/String[]) agree");
    }

    static void testCopyOfAllTypes() {
        check(Arrays.equals(Arrays.copyOf(new boolean[]{true}, 2), new boolean[]{true, false}),
              "copyOf(boolean[]) pads false");
        check(Arrays.equals(Arrays.copyOf(new char[]{'x'}, 2), new char[]{'x', '\0'}),
              "copyOf(char[]) pads \\0");
        check(Arrays.equals(Arrays.copyOf(new byte[]{1}, 3), new byte[]{1, 0, 0}), "copyOf(byte[]) pads 0");
        check(Arrays.equals(Arrays.copyOf(new String[]{"a"}, 2), new String[]{"a", null}),
              "copyOf(String[]) pads null");
        check(Arrays.equals(Arrays.copyOfRange(new char[]{'a', 'b'}, 0, 4), new char[]{'a', 'b', '\0', '\0'}),
              "copyOfRange(char[]) pads \\0");
        Object[] wider = Arrays.copyOf(new Object[]{"a", "b"}, 4, Object[].class);
        check(wider.length == 4 && wider[0].equals("a") && wider[3] == null,
              "copyOf(T[], int, Class) newType");
        check(Arrays.copyOf(new int[]{1, 2}, 1).length == 1, "copyOf truncates");
    }

    static void testFillAllTypes() {
        boolean[] fb = new boolean[3];
        Arrays.fill(fb, 1, 2, true);
        check(Arrays.equals(fb, new boolean[]{false, true, false}), "fill(boolean[], from, to, val)");
        char[] fc = new char[2];
        Arrays.fill(fc, 'z');
        check(Arrays.equals(fc, new char[]{'z', 'z'}), "fill(char[], val)");
        String[] fo = {"a"};
        Arrays.fill(fo, null);
        check(fo[0] == null, "fill(Object[], null) allowed");
        long[] fl = new long[2];
        Arrays.fill(fl, -1L);
        check(Arrays.equals(fl, new long[]{-1L, -1L}), "fill(long[], val)");
        double[] fd = new double[2];
        Arrays.fill(fd, 0.5);
        check(Arrays.equals(fd, new double[]{0.5, 0.5}), "fill(double[], val)");
    }

    static void testEqualsFloatingSemantics() {
        // Arrays.equals uses Double.doubleToLongBits: NaN equals NaN; +0.0 != -0.0
        check(Arrays.equals(new double[]{Double.NaN}, new double[]{Double.NaN}),
              "equals(double[]) NaN equal to NaN");
        check(!Arrays.equals(new double[]{0.0}, new double[]{-0.0}),
              "equals(double[]) +0.0 != -0.0");
        check(Arrays.equals(new float[]{Float.NaN}, new float[]{Float.NaN}),
              "equals(float[]) NaN equal to NaN");
        check(!Arrays.equals(new float[]{0.0f}, new float[]{-0.0f}),
              "equals(float[]) +0.0 != -0.0");
        check(!Arrays.equals(new int[]{1}, new int[]{1, 2}), "equals(int[]) length mismatch false");
        check(Arrays.equals(new int[0], new int[0]), "equals(int[]) empty equal");
    }

    static void testAsListDeepContract() {
        String[] arr = {"a", "b", "c"};
        List<String> list = Arrays.asList(arr);
        check(list.equals(new ArrayList<>(Arrays.asList("a", "b", "c"))),
              "asList equals ArrayList same content");
        check(new ArrayList<>(Arrays.asList("a", "b", "c")).equals(list),
              "ArrayList equals asList (symmetric)");
        check(list.hashCode() == Arrays.asList("a", "b", "c").hashCode(), "asList hashCode formula");
        check(list.subList(1, 3).equals(Arrays.asList("b", "c")), "asList.subList");
        String[] reused = list.toArray(new String[3]);
        check(reused.length == 3 && reused[2].equals("c"), "asList.toArray(T[]) exact length");
        String[] bigger = list.toArray(new String[5]);
        check(bigger.length == 5 && bigger[3] == null, "asList.toArray(T[]) larger pads null");
        // iterator: remove before next -> ISE (JDK-8 state check fires first); after next -> UOE
        final List<String> l2 = Arrays.asList("a", "b");
        checkThrows(IllegalStateException.class, () -> l2.iterator().remove(),
                    "asList iterator.remove before next ISE (JDK-8)");
        java.util.Iterator<String> it = l2.iterator();
        it.next();
        checkThrows(UnsupportedOperationException.class, () -> it.remove(),
                    "asList iterator.remove after next UOE");
        check(Arrays.asList("a", null, "b").indexOf(null) == 1, "asList.indexOf(null)");
        check(Arrays.asList("a", "b", "a").lastIndexOf("a") == 2, "asList.lastIndexOf");
        check(list.containsAll(Arrays.asList("a", "b")), "asList.containsAll");
        // sort through the view writes the backing array
        String[] s3 = {"b", "a"};
        Arrays.asList(s3).sort(Comparator.naturalOrder());
        check(Arrays.equals(s3, new String[]{"a", "b"}), "asList.sort writes backing array");
        // listIterator.set writes backing array (after next)
        String[] s4 = {"x"};
        java.util.ListIterator<String> s4it = Arrays.asList(s4).listIterator();
        s4it.next();
        s4it.set("y");
        check(s4[0].equals("y"), "asList.listIterator.set writes backing");
        // single null element vs null array
        check(Arrays.asList((Object) null).size() == 1, "asList((Object)null) single null element");
        checkThrows(NullPointerException.class, () -> Arrays.asList((Object[]) null),
                    "asList((Object[])null) NPE");
    }

    static void testSpliteratorCharacteristics() {
        Spliterator<Integer> sp = Arrays.spliterator(new Integer[]{1, 2, 3, 4});
        check(sp.hasCharacteristics(Spliterator.ORDERED), "spliterator(T[]) ORDERED");
        check(sp.hasCharacteristics(Spliterator.SIZED), "spliterator(T[]) SIZED");
        check(sp.hasCharacteristics(Spliterator.SUBSIZED), "spliterator(T[]) SUBSIZED");
        check(sp.hasCharacteristics(Spliterator.IMMUTABLE), "spliterator(T[]) IMMUTABLE");
        check(!sp.hasCharacteristics(Spliterator.NONNULL), "spliterator(T[]) not NONNULL");
        check(sp.getExactSizeIfKnown() == 4, "spliterator(T[]) exact size");
        Spliterator.OfInt si = Arrays.spliterator(new int[]{1, 2, 3, 4});
        check(si.hasCharacteristics(Spliterator.ORDERED | Spliterator.SIZED
                                    | Spliterator.SUBSIZED | Spliterator.IMMUTABLE),
              "spliterator(int[]) ORDERED|SIZED|SUBSIZED|IMMUTABLE");
        check(!si.hasCharacteristics(Spliterator.NONNULL), "spliterator(int[]) not NONNULL (empirical)");
        check(si.getExactSizeIfKnown() == 4, "spliterator(int[]) exact size");
        Spliterator<Integer> front = Arrays.spliterator(new Integer[]{1, 2, 3, 4});
        Spliterator<Integer> back = front.trySplit();
        check(front.getExactSizeIfKnown() + back.getExactSizeIfKnown() == 4,
              "trySplit sizes sum to original");
        java.util.List<Integer> collected = new ArrayList<>();
        back.forEachRemaining(collected::add);
        front.forEachRemaining(collected::add);
        check(collected.contains(1) && collected.contains(4) && collected.size() == 4,
              "trySplit halves cover all elements");
    }

    public static void main(String[] args) {
        testSort();
        testBinarySearch();
        testEqualsFill();
        testCopy();
        testAsList();
        testToStringHash();
        testSortEdgeCases();
        testBinarySearchEdgeCases();
        testCopyEdgeCases();
        testFillEdgeCases();
        testEqualsDeepEdge();
        testAsListEdge();
        testToStringHashEdge();
        testSortAllPrimitiveTypes();
        testSortStability();
        testBinarySearchAllTypes();
        testStreamRanges();
        testParallelPrefixSetAll();
        testNullArrayContracts();
        testHashCodeFormula();
        testCopyOfAllTypes();
        testFillAllTypes();
        testEqualsFloatingSemantics();
        testAsListDeepContract();
        testSpliteratorCharacteristics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
