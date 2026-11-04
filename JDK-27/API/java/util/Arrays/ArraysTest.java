/*
 * @test
 * @summary java.util.Arrays conformance. White-box: Class.forName + class modifiers/
 *          member shape. Black-box: sort (primitives, Object natural with ClassCastException
 *          for non-Comparable, Comparator, ranges, stability, parallelSort), binarySearch
 *          (natural + Comparator + insertion points), copyOf/copyOfRange (truncate/pad/
 *          negative/from>to/from<0), fill, equals/deepEquals, compare/compareUnsigned/mismatch,
 *          setAll/parallelSetAll, stream, asList fixed-size view, and toString/deepToString/
 *          hashCode/deepHashCode.
 * @run main ArraysTest
 *
 * 设计案：
 * 1. 契约：Arrays 是数组工具类，sort 原地排序（基本类型快排、对象归并稳定）、
 *    binarySearch 要求有序、copyOf/copyOfRange 截断或补零/null、equals/deepEquals 按内容比较、
 *    asList 返回固定大小视图、stream 提供流式遍历。
 * 2. 用户场景：构造数组 → sort 排序 → binarySearch 查找 → copyOf 拷贝/扩容 → fill 填充 →
 *    compare/mismatch 比较 → setAll 生成 → stream 聚合 → asList 视图读写。
 * 3. 组合流程：见各 test*，均为「构造 → 多步组合 → 断言」。
 * 4. 不变量：sort 后元素有序且为原多重集；copyOf 返回新数组；asList 与原数组双向联动。
 * 5. 边界/异常：sort(Object[]) 元素不可比较抛 ClassCastException；copyOf 负长度抛
 *    NegativeArraySizeException；copyOfRange from<0 抛 IndexOutOfBoundsException、from>to
 *    抛 IllegalArgumentException、to 超界则补 null；asList 的 add/remove/clear 抛
 *    UnsupportedOperationException。
 */

import java.util.Arrays;
import java.util.Comparator;

public class ArraysTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                passed++;
            } else {
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getName(), t);
            }
        }
    }

    // ---- white-box: 结构/反射断言 ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Arrays");
        check(!c.isInterface(), "Arrays is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "Arrays public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Arrays final");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Arrays concrete");
        check(java.lang.reflect.Modifier.isPrivate(c.getDeclaredConstructor().getModifiers()),
              "Arrays private constructor");
        c.getMethod("sort", int[].class);
        c.getMethod("binarySearch", int[].class, int.class);
        c.getMethod("copyOf", int[].class, int.class);
        c.getMethod("equals", int[].class, int[].class);
        check(true, "key public static methods present");
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

        String[] st = {"bb", "aa", "c", "d"};
        Arrays.sort(st, Comparator.comparingInt(String::length));
        check(Arrays.equals(st, new String[]{"c", "d", "bb", "aa"}),
              "Object sort stable for equal keys");

        int[] p = {5, 2, 4, 1, 3};
        Arrays.parallelSort(p);
        check(Arrays.equals(p, new int[]{1, 2, 3, 4, 5}), "parallelSort(int[])");

        String[] ps = {"bb", "aa", "c"};
        Arrays.parallelSort(ps, Comparator.comparingInt(String::length));
        check(Arrays.equals(ps, new String[]{"c", "bb", "aa"}), "parallelSort(T[], Comparator)");

        Object[] nc = {new Object(), new Object()};
        expect(ClassCastException.class, () -> Arrays.sort(nc),
              "sort non-Comparable throws ClassCastException");
    }

    static void testBinarySearch() {
        int[] a = {1, 3, 5, 7, 9};
        check(Arrays.binarySearch(a, 3) == 1, "binarySearch found");
        check(Arrays.binarySearch(a, 2) == -2, "binarySearch insertion point");
        check(Arrays.binarySearch(a, 0) == -1, "binarySearch before first");
        check(Arrays.binarySearch(a, 9) == 4, "binarySearch last");
        check(Arrays.binarySearch(a, 10) == -6, "binarySearch after last");
        check(Arrays.binarySearch(a, 1, 3, 5) == 2, "binarySearch in range");

        Integer[] ia = {1, 3, 5, 7, 9};
        check(Arrays.binarySearch(ia, 5, Comparator.naturalOrder()) == 2,
              "binarySearch with Comparator");
        check(Arrays.binarySearch(ia, 2, Comparator.naturalOrder()) == -2,
              "binarySearch with Comparator insertion point");
    }

    static void testEqualsFillCompare() {
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

        check(Arrays.compare(new int[]{1, 2}, new int[]{1, 3}) < 0, "compare lexicographic <");
        check(Arrays.compare(new int[]{1, 2}, new int[]{1, 1}) > 0, "compare lexicographic >");
        check(Arrays.compare(new int[]{1, 2}, new int[]{1, 2}) == 0, "compare equal");
        check(Arrays.compare(new int[]{-1}, new int[]{0}) < 0, "compare signed -1 < 0");
        check(Arrays.compareUnsigned(new int[]{-1}, new int[]{0}) > 0,
              "compareUnsigned 0xFFFFFFFF > 0");
        check(Arrays.compareUnsigned(new byte[]{-1}, new byte[]{0}) > 0,
              "compareUnsigned byte 255 > 0");

        check(Arrays.mismatch(new int[]{1, 2, 3}, new int[]{1, 2, 4}) == 2,
              "mismatch index");
        check(Arrays.mismatch(new int[]{1, 2, 3}, new int[]{1, 2, 3}) == -1,
              "mismatch equal -1");
        check(Arrays.mismatch(new int[]{1, 2}, new int[]{1, 2, 3}) == 2,
              "mismatch prefix returns min length");
    }

    static void testCopy() {
        int[] a = {1, 2, 3};
        check(Arrays.equals(Arrays.copyOf(a, 5), new int[]{1, 2, 3, 0, 0}),
              "copyOf pads");
        check(Arrays.equals(Arrays.copyOf(a, 2), new int[]{1, 2}), "copyOf truncates");
        check(Arrays.equals(Arrays.copyOfRange(a, 1, 3), new int[]{2, 3}), "copyOfRange");
        check(Arrays.copyOf(a, 3) != a, "copyOf returns new array");
        check(Arrays.copyOf(a, 0).length == 0, "copyOf to length 0");

        expect(NegativeArraySizeException.class, () -> Arrays.copyOf(a, -1),
              "copyOf negative length");

        expect(IllegalArgumentException.class, () -> Arrays.copyOfRange(a, 3, 2),
              "copyOfRange from>to IAE");
        expect(IndexOutOfBoundsException.class, () -> Arrays.copyOfRange(a, -1, 2),
              "copyOfRange from<0 IOOBE");
        String[] padded = Arrays.copyOfRange(new String[]{"a"}, 0, 3);
        check(Arrays.equals(padded, new String[]{"a", null, null}),
              "copyOfRange to>length pads null");
    }

    static void testSetAllStream() {
        int[] a = new int[5];
        Arrays.setAll(a, i -> i * i);
        check(Arrays.equals(a, new int[]{0, 1, 4, 9, 16}), "setAll(int[])");
        String[] s = new String[3];
        Arrays.setAll(s, i -> "n" + i);
        check(Arrays.equals(s, new String[]{"n0", "n1", "n2"}), "setAll(T[])");
        int[] p = new int[5];
        Arrays.parallelSetAll(p, i -> i + 1);
        check(Arrays.equals(p, new int[]{1, 2, 3, 4, 5}), "parallelSetAll");

        check(Arrays.stream(new int[]{1, 2, 3}).sum() == 6, "stream(int[]) sum");
        check(Arrays.equals(Arrays.stream(new int[]{1, 2, 3, 4}, 1, 3).toArray(),
                            new int[]{2, 3}), "stream(int[], range)");
        check(Arrays.stream(new String[]{"a", "b"}).count() == 2, "stream(T[]) count");
        check(Arrays.stream(new String[]{"a", "b"}).reduce("", (x, y) -> x + y).equals("ab"),
              "stream(T[]) reduce");
    }

    static void testAsList() {
        String[] arr = {"a", "b", "c"};
        java.util.List<String> list = Arrays.asList(arr);
        check(list.size() == 3 && list.get(1).equals("b"), "asList content");
        list.set(0, "A");
        check(arr[0].equals("A"), "asList.set reflects in backing array");
        arr[1] = "B";
        check(list.get(1).equals("B"), "backing array change reflects in list");

        expect(UnsupportedOperationException.class, () -> list.add("d"), "asList.add UOE");
        expect(UnsupportedOperationException.class, () -> list.remove(0), "asList.remove UOE");
        expect(UnsupportedOperationException.class, list::clear, "asList.clear UOE");
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
        check(Arrays.deepHashCode(new int[][]{{1, 2}}) == Arrays.deepHashCode(new int[][]{{1, 2}}),
              "deepHashCode stable");
    }

    static void testSortSearchBoundaries() {
        // empty arrays
        int[] empty = {};
        Arrays.sort(empty);
        check(empty.length == 0, "sort empty int[]");
        check(Arrays.binarySearch(empty, 1) == -1, "binarySearch empty -1");
        // single element
        int[] single = {5};
        Arrays.sort(single);
        check(Arrays.binarySearch(single, 5) == 0, "binarySearch single found");
        check(Arrays.binarySearch(single, 3) == -1, "binarySearch single miss");
        // sorted / reverse / duplicates
        int[] dups = {3, 1, 3, 2, 1};
        Arrays.sort(dups);
        check(Arrays.equals(dups, new int[]{1, 1, 2, 3, 3}), "sort duplicates");
        int dupIdx = Arrays.binarySearch(dups, 3);
        check(dupIdx >= 0 && dups[dupIdx] == 3, "binarySearch duplicate hit");
        // null element in Object[] sort throws NPE; nullsFirst sorts deterministically
        String[] withNull = {"b", null, "a"};
        expect(NullPointerException.class, () -> Arrays.sort(withNull), "sort Object[] with null NPE");
        Arrays.sort(withNull, Comparator.nullsFirst(Comparator.naturalOrder()));
        check(Arrays.equals(withNull, new String[]{null, "a", "b"}), "sort nullsFirst");
        // compare boundaries: empty vs empty / empty vs non-empty / non-empty vs empty
        check(Arrays.compare(new int[]{}, new int[]{}) == 0, "compare empty==empty");
        check(Arrays.compare(new int[]{}, new int[]{1}) < 0, "compare empty < non-empty");
        check(Arrays.compare(new int[]{1}, new int[]{}) > 0, "compare non-empty > empty");
        check(Arrays.compare(new int[]{Integer.MIN_VALUE}, new int[]{Integer.MAX_VALUE}) < 0,
              "compare MIN_VALUE < MAX_VALUE");
        // equals / mismatch empty
        check(Arrays.equals(new int[]{}, new int[]{}), "equals empty arrays");
        check(Arrays.mismatch(new int[]{}, new int[]{}) == -1, "mismatch empty==empty -1");
        check(Arrays.mismatch(new int[]{}, new int[]{1}) == 0, "mismatch empty vs non-empty 0");
        // copyOf empty / copyOfRange empty
        check(Arrays.copyOf(new int[]{}, 3).length == 3, "copyOf empty pads");
        check(Arrays.copyOfRange(new int[]{1, 2, 3}, 1, 1).length == 0, "copyOfRange empty range");
        // binarySearch with fromIndex == toIndex (empty range)
        check(Arrays.binarySearch(new int[]{1, 3, 5}, 1, 1, 2) == -2, "binarySearch empty range");
    }

    static void testBlackBox() {
        // parallelPrefix (full array + range) for int/long/double/Object
        int[] ia = {1, 2, 3, 4, 5};
        Arrays.parallelPrefix(ia, (a, b) -> a + b);
        check(Arrays.equals(ia, new int[]{1, 3, 6, 10, 15}), "parallelPrefix(int[], op) cumulative sum");

        int[] ir = {1, 2, 3, 4, 5};
        Arrays.parallelPrefix(ir, 1, 4, (a, b) -> a + b);
        check(Arrays.equals(ir, new int[]{1, 2, 5, 9, 5}), "parallelPrefix(int[], range, op)");

        long[] la = {1, 2, 3, 4};
        Arrays.parallelPrefix(la, (a, b) -> a * b);
        check(Arrays.equals(la, new long[]{1, 2, 6, 24}), "parallelPrefix(long[], op) cumulative product");

        long[] lr = {1, 2, 3, 4};
        Arrays.parallelPrefix(lr, 0, 3, (a, b) -> a * b);
        check(Arrays.equals(lr, new long[]{1, 2, 6, 4}), "parallelPrefix(long[], range, op)");

        double[] da = {1.0, 2.0, 3.0, 4.0};
        Arrays.parallelPrefix(da, (a, b) -> a + b);
        check(Arrays.equals(da, new double[]{1.0, 3.0, 6.0, 10.0}), "parallelPrefix(double[], op)");

        double[] dr = {1.0, 2.0, 3.0, 4.0};
        Arrays.parallelPrefix(dr, 1, 3, (a, b) -> a + b);
        check(Arrays.equals(dr, new double[]{1.0, 2.0, 5.0, 4.0}), "parallelPrefix(double[], range, op)");

        String[] sa = {"a", "b", "c"};
        Arrays.parallelPrefix(sa, (a, b) -> a + b);
        check(Arrays.equals(sa, new String[]{"a", "ab", "abc"}), "parallelPrefix(Object[], op) concat");

        String[] sr = {"a", "b", "c", "d"};
        Arrays.parallelPrefix(sr, 1, 3, (a, b) -> a + b);
        check(Arrays.equals(sr, new String[]{"a", "b", "bc", "d"}), "parallelPrefix(Object[], range, op)");

        // spliterator over primitive/object array ranges
        java.util.Spliterator.OfDouble dsp = Arrays.spliterator(new double[]{1, 2, 3, 4, 5}, 1, 4);
        check(dsp.estimateSize() == 3, "spliterator(double[], range) estimateSize");
        check(dsp.hasCharacteristics(java.util.Spliterator.ORDERED | java.util.Spliterator.SIZED
                | java.util.Spliterator.SUBSIZED | java.util.Spliterator.IMMUTABLE),
              "spliterator(double[], range) characteristics");
        double[] dacc = new double[3];
        final int[] di = {0};
        dsp.forEachRemaining((double x) -> dacc[di[0]++] = x);
        check(Arrays.equals(dacc, new double[]{2, 3, 4}), "spliterator(double[], range) elements");

        java.util.Spliterator.OfLong lsp = Arrays.spliterator(new long[]{1, 2, 3, 4, 5}, 2, 5);
        check(lsp.estimateSize() == 3, "spliterator(long[], range) estimateSize");
        long[] lacc = new long[3];
        final int[] li = {0};
        lsp.forEachRemaining((long x) -> lacc[li[0]++] = x);
        check(Arrays.equals(lacc, new long[]{3, 4, 5}), "spliterator(long[], range) elements");

        java.util.Spliterator<String> osp = Arrays.spliterator(new String[]{"a", "b", "c", "d"}, 1, 3);
        check(osp.estimateSize() == 2, "spliterator(Object[], range) estimateSize");
        java.util.List<String> olist = new java.util.ArrayList<>();
        osp.forEachRemaining(olist::add);
        check(olist.equals(Arrays.asList("b", "c")), "spliterator(Object[], range) elements");

        java.util.Spliterator<String> all = Arrays.spliterator(new String[]{"x", "y"});
        check(all.estimateSize() == 2, "spliterator(Object[]) estimateSize");
        java.util.List<String> allList = new java.util.ArrayList<>();
        all.forEachRemaining(allList::add);
        check(allList.equals(Arrays.asList("x", "y")), "spliterator(Object[]) elements");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSort();
        testBinarySearch();
        testEqualsFillCompare();
        testCopy();
        testSetAllStream();
        testAsList();
        testToStringHash();
        testSortSearchBoundaries();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
