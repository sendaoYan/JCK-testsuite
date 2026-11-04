/*
 * @test
 * @summary java.util.Arrays API conformance: sort (primitives, Object natural, with
 *          Comparator, ranges, parallelSort), binarySearch return values, equals/
 *          deepEquals, fill, copyOf/copyOfRange (truncate/pad), asList (fixed-size view),
 *          and toString/deepToString/hashCode.
 * @run main ArraysTest
 */

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
