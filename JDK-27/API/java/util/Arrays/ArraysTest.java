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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Spliterator;

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

    // ---- deepened contract coverage, round 2 (empirically locked on jdk-27.0.1) --

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
        Arrays.parallelSort(dr, 1, 4);
        check(Arrays.equals(dr, new double[]{5.0, 1.0, 2.0, 4.0}), "parallelSort(double[], from, to)");
        String[] s = {"d", "c", "b", "a"};
        Arrays.sort(s, 0, 3, Comparator.reverseOrder());
        check(Arrays.equals(s, new String[]{"d", "c", "b", "a"}), "sort(T[], from, to, cmp)");
        byte[] pb = {3, 1, 2};
        Arrays.parallelSort(pb);
        check(Arrays.equals(pb, new byte[]{1, 2, 3}), "parallelSort(byte[])");
    }

    static void testSortStability() {
        String[][] pairs = {{"1", "first"}, {"2", "x"}, {"1", "second"}, {"2", "y"}, {"1", "third"}};
        Arrays.sort(pairs, Comparator.comparing(p -> p[0]));
        check(pairs[0][1].equals("first") && pairs[1][1].equals("second") && pairs[2][1].equals("third"),
              "object sort stable for equal keys (group 1)");
        check(pairs[3][1].equals("x") && pairs[4][1].equals("y"),
              "object sort stable for equal keys (group 2)");
    }

    static void testBinarySearchAllTypes() {
        check(Arrays.binarySearch(new char[]{'a', 'c'}, 'b') == -2, "binarySearch(char[]) miss");
        check(Arrays.binarySearch(new byte[]{1, 3}, (byte) 0) == -1, "binarySearch(byte[]) before first");
        check(Arrays.binarySearch(new short[]{1, 3}, (short) 5) == -3, "binarySearch(short[]) after last");
        check(Arrays.binarySearch(new long[]{1L, 3L}, 2L) == -2, "binarySearch(long[]) gap");
        check(Arrays.binarySearch(new float[]{1f, 3f}, 5f) == -3, "binarySearch(float[]) after last");
        check(Arrays.binarySearch(new double[]{1.0, 3.0}, 0.5) == -1, "binarySearch(double[]) before first");
        check(Arrays.binarySearch(new String[]{"a", "c"}, "b") == -2, "binarySearch(Object[]) gap");
        check(Arrays.binarySearch(new String[]{"c", "a"}, "b", Comparator.reverseOrder()) == -2,
              "binarySearch(Object[], cmp) gap descending");
        check(Arrays.binarySearch(new String[]{"c", "a"}, "c", Comparator.reverseOrder()) == 0,
              "binarySearch(Object[], cmp) hit descending");
        int[] five = {2, 4, 6, 8, 10};
        check(Arrays.binarySearch(five, 1) == -1, "five: before first");
        check(Arrays.binarySearch(five, 3) == -2, "five: gap 0");
        check(Arrays.binarySearch(five, 5) == -3, "five: gap 1");
        check(Arrays.binarySearch(five, 7) == -4, "five: gap 2");
        check(Arrays.binarySearch(five, 9) == -5, "five: gap 3");
        check(Arrays.binarySearch(five, 11) == -6, "five: after last -(size+1)");
        Integer[] boxed = {2, 4, 6, 8, 10};
        check(Arrays.binarySearch(boxed, 5, Comparator.naturalOrder()) == -3,
              "boxed comparator gap -> -3");
        check(Arrays.binarySearch(boxed, 11, Comparator.naturalOrder()) == -6,
              "boxed comparator after last");
    }

    static void testStreamRangeViolations() {
        expect(ArrayIndexOutOfBoundsException.class,
               () -> Arrays.stream(new int[]{1, 2, 3}, 2, 1), "stream(int[], from>to) AIOOBE");
        expect(ArrayIndexOutOfBoundsException.class,
               () -> Arrays.stream(new int[]{1, 2, 3}, -1, 2), "stream(int[], from<0) AIOOBE");
        expect(ArrayIndexOutOfBoundsException.class,
               () -> Arrays.stream(new int[]{1, 2, 3}, 1, 5), "stream(int[], to>length) AIOOBE");
        expect(ArrayIndexOutOfBoundsException.class,
               () -> Arrays.stream(new String[]{"a"}, 2, 1), "stream(T[], from>to) AIOOBE");
        expect(ArrayIndexOutOfBoundsException.class,
               () -> Arrays.stream(new long[]{1}, -1, 1), "stream(long[], from<0) AIOOBE");
        check(Arrays.stream(new String[]{"a", "b", "c"}, 1, 3).reduce("", String::concat).equals("bc"),
              "stream(T[], from, to) slice");
    }

    static void testNullArrayContracts() {
        check(Arrays.equals((int[]) null, (int[]) null), "equals(int[] null, null) true");
        check(!Arrays.equals((int[]) null, new int[]{1}), "equals(int[] null, arr) false");
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
        check(Arrays.hashCode(new int[]{1, 2}) == 31 * (31 * 1 + 1) + 2, "hashCode formula 994");
        check(Arrays.hashCode(new int[]{1, 2}) == 994, "hashCode hand-computed 994");
        check(Arrays.asList("a", "b").hashCode() == 4066, "asList hashCode hand-computed 4066");
        check(Arrays.hashCode(new Object[]{"a", null}) == 31 * (31 * 1 + 97) + 0,
              "hashCode(Object[]) with null element");
    }

    static void testCopyFillAllTypes() {
        check(Arrays.equals(Arrays.copyOf(new boolean[]{true}, 2), new boolean[]{true, false}),
              "copyOf(boolean[]) pads false");
        check(Arrays.equals(Arrays.copyOf(new char[]{'x'}, 2), new char[]{'x', '\0'}),
              "copyOf(char[]) pads \\0");
        check(Arrays.equals(Arrays.copyOf(new byte[]{1}, 3), new byte[]{1, 0, 0}), "copyOf(byte[]) pads 0");
        check(Arrays.equals(Arrays.copyOf(new String[]{"a"}, 2), new String[]{"a", null}),
              "copyOf(String[]) pads null");
        check(Arrays.equals(Arrays.copyOfRange(new char[]{'a', 'b'}, 0, 4),
                            new char[]{'a', 'b', '\0', '\0'}),
              "copyOfRange(char[]) pads \\0");
        Object[] wider = Arrays.copyOf(new Object[]{"a", "b"}, 4, Object[].class);
        check(wider.length == 4 && wider[0].equals("a") && wider[3] == null,
              "copyOf(T[], int, Class) newType");
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
    }

    static void testEqualsFloatingSemantics() {
        check(Arrays.equals(new double[]{Double.NaN}, new double[]{Double.NaN}),
              "equals(double[]) NaN equal to NaN");
        check(!Arrays.equals(new double[]{0.0}, new double[]{-0.0}),
              "equals(double[]) +0.0 != -0.0");
        check(Arrays.equals(new float[]{Float.NaN}, new float[]{Float.NaN}),
              "equals(float[]) NaN equal to NaN");
        check(!Arrays.equals(new float[]{0.0f}, new float[]{-0.0f}),
              "equals(float[]) +0.0 != -0.0");
        check(!Arrays.equals(new int[]{1}, new int[]{1, 2}), "equals(int[]) length mismatch false");
    }

    @SuppressWarnings("unchecked")
    static void testCompareMismatchJDK9() {
        // ranged equals / mismatch
        check(Arrays.equals(new int[]{1, 2, 3, 4}, 1, 3, new int[]{9, 2, 3, 9}, 1, 3),
              "equals(int[], range) true");
        check(!Arrays.equals(new int[]{1, 2, 3, 4}, 1, 3, new int[]{9, 2, 9, 9}, 1, 3),
              "equals(int[], range) false on differing element");
        check(!Arrays.equals(new int[]{1, 2}, 0, 2, new int[]{1}, 0, 1),
              "equals(int[], range) different range lengths false");
        check(Arrays.mismatch(new int[]{1, 2, 3, 4}, 0, 3, new int[]{1, 2, 9, 4}, 0, 3) == 2,
              "mismatch(int[], range) index");
        // Comparator-based equals/mismatch/compare
        Comparator<String> allEq = (x, y) -> 0;
        check(Arrays.equals(new String[]{"a", "b"}, new String[]{"x", "y"}, allEq),
              "equals(T[],T[],cmp) all-equal comparator true");
        check(!Arrays.equals(new String[]{"a"}, new String[]{"a", "b"}, allEq),
              "equals(T[],T[],cmp) different lengths false regardless");
        check(Arrays.mismatch(new String[]{"a"}, new String[]{"x"}, allEq) == -1,
              "mismatch(T[],T[],cmp) -1 when comparator says equal");
        check(Arrays.mismatch(new String[]{"a", "b"}, new String[]{"a", "x"}, allEq) == -1,
              "mismatch(T[],T[],cmp) ignores elements comparator deems equal");
        check(Arrays.compare(new String[]{"a", "b"}, new String[]{"a", "c"},
                             Comparator.reverseOrder()) > 0,
              "compare(T[],T[],cmp) reversed sign");
        check(Arrays.compare(new String[]{"a"}, new String[]{"a", "b"}) < 0,
              "compare(T[],T[]) prefix shorter first");
        expect(NullPointerException.class,
               () -> Arrays.equals(new String[]{"a"}, new String[]{"a"}, null),
               "equals(T[],T[],null cmp) NPE");
        expect(NullPointerException.class,
               () -> Arrays.compare(new String[]{"a"}, new String[]{"a"}, null),
               "compare(T[],T[],null cmp) NPE");
        expect(NullPointerException.class,
               () -> Arrays.mismatch(new String[]{"a"}, new String[]{"a"}, null),
               "mismatch(T[],T[],null cmp) NPE");
        // lexicographic compare semantics: Double.compare per element
        check(Arrays.compare(new double[]{0.0}, new double[]{-0.0}) > 0,
              "compare(double[]) +0.0 > -0.0");
        check(Arrays.compare(new double[]{-0.0}, new double[]{0.0}) < 0,
              "compare(double[]) -0.0 < +0.0");
        check(Arrays.compare(new double[]{Double.NaN}, new double[]{Double.POSITIVE_INFINITY}) > 0,
              "compare(double[]) NaN greatest");
        check(Arrays.compareUnsigned(new int[]{-1}, new int[]{1}) > 0,
              "compareUnsigned(int[]) 0xFFFFFFFF > 1");
        check(Arrays.compareUnsigned(new short[]{-1}, new short[]{1}) > 0,
              "compareUnsigned(short[]) 0xFFFF > 1");
        check(Arrays.compareUnsigned(new long[]{-1L}, new long[]{1L}) > 0,
              "compareUnsigned(long[]) unsigned greater");
        check(Arrays.compareUnsigned(new byte[]{-1}, new byte[]{1}) > 0,
              "compareUnsigned(byte[]) 255 > 1");
    }

    static void testAsListDeepContract() {
        String[] arr = {"a", "b", "c"};
        java.util.List<String> list = Arrays.asList(arr);
        check(list.equals(new ArrayList<>(Arrays.asList("a", "b", "c"))),
              "asList equals ArrayList same content");
        check(list.hashCode() == 31 * 4066 + "c".hashCode(), "asList hashCode extends formula");
        check(list.subList(1, 3).equals(Arrays.asList("b", "c")), "asList.subList");
        String[] reused = list.toArray(new String[3]);
        check(reused.length == 3 && reused[2].equals("c"), "asList.toArray(T[]) exact length");
        String[] bigger = list.toArray(new String[5]);
        check(bigger.length == 5 && bigger[3] == null, "asList.toArray(T[]) larger pads null");
        // iterator: JDK-27 checks remove permission before the cursor state
        final java.util.List<String> l2 = Arrays.asList("a", "b");
        expect(UnsupportedOperationException.class, () -> l2.iterator().remove(),
               "asList iterator.remove before next UOE (JDK-27 checks permission first)");
        java.util.Iterator<String> it = l2.iterator();
        it.next();
        expect(UnsupportedOperationException.class, () -> it.remove(),
               "asList iterator.remove after next UOE");
        check(Arrays.asList("a", null, "b").indexOf(null) == 1, "asList.indexOf(null)");
        check(Arrays.asList("a", "b", "a").lastIndexOf("a") == 2, "asList.lastIndexOf");
        check(list.containsAll(Arrays.asList("a", "b")), "asList.containsAll");
        String[] s3 = {"b", "a"};
        Arrays.asList(s3).sort(Comparator.naturalOrder());
        check(Arrays.equals(s3, new String[]{"a", "b"}), "asList.sort writes backing array");
        String[] s4 = {"x"};
        java.util.ListIterator<String> s4it = Arrays.asList(s4).listIterator();
        s4it.next();
        s4it.set("y");
        check(s4[0].equals("y"), "asList.listIterator.set writes backing");
        check(Arrays.asList((Object) null).size() == 1, "asList((Object)null) single null element");
        expect(NullPointerException.class, () -> Arrays.asList((Object[]) null),
               "asList((Object[])null) NPE");
    }

    static void testSpliteratorCharacteristics() {
        Spliterator<Integer> sp = Arrays.spliterator(new Integer[]{1, 2, 3, 4});
        check(sp.hasCharacteristics(Spliterator.ORDERED | Spliterator.SIZED
                                    | Spliterator.SUBSIZED | Spliterator.IMMUTABLE),
              "spliterator(T[]) ORDERED|SIZED|SUBSIZED|IMMUTABLE");
        check(!sp.hasCharacteristics(Spliterator.NONNULL), "spliterator(T[]) not NONNULL");
        check(sp.getExactSizeIfKnown() == 4, "spliterator(T[]) exact size");
        Spliterator.OfInt si = Arrays.spliterator(new int[]{1, 2, 3, 4});
        check(si.hasCharacteristics(Spliterator.ORDERED | Spliterator.SIZED
                                    | Spliterator.SUBSIZED | Spliterator.IMMUTABLE),
              "spliterator(int[]) ORDERED|SIZED|SUBSIZED|IMMUTABLE");
        check(!si.hasCharacteristics(Spliterator.NONNULL), "spliterator(int[]) not NONNULL (empirical)");
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
        testSortAllPrimitiveTypes();
        testSortStability();
        testBinarySearchAllTypes();
        testStreamRangeViolations();
        testNullArrayContracts();
        testCopyFillAllTypes();
        testEqualsFloatingSemantics();
        testCompareMismatchJDK9();
        testAsListDeepContract();
        testSpliteratorCharacteristics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
