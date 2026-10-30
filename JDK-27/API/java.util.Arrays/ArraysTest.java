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

public class ArraysTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    public static void main(String[] args) {
        testSort();
        testBinarySearch();
        testEqualsFill();
        testCopy();
        testAsList();
        testToStringHash();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
