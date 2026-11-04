/*
 * @test
 * @summary java.util.Spliterators API conformance: array/empty spliterators.
 * @run main SpliteratorsTest
 */

import java.util.Spliterator;
import java.util.Spliterators;

public class SpliteratorsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSpliterators() {
        Spliterator<String> s = Spliterators.spliterator(new String[] { "a", "b", "c" }, 0);
        check(s.estimateSize() == 3, "array spliterator estimateSize");

        Spliterator<Object> empty = Spliterators.emptySpliterator();
        check(empty.estimateSize() == 0, "emptySpliterator estimateSize");
        check(!empty.tryAdvance(x -> { }), "emptySpliterator has no elements");
    }

    // ===================== 契约深化：ranged 数组/越界异常矩阵/原生特化/迭代器桥接/空 spliterator =====================

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    static void testArrayRanged() {
        String[] arr = {"a", "b", "c", "d"};
        Spliterator<String> sp = Spliterators.spliterator(arr, 1, 3, Spliterator.ORDERED);
        check(sp.estimateSize() == 2, "ranged array spliterator size == to-from");
        check(sp.getExactSizeIfKnown() == 2, "ranged array getExactSizeIfKnown");
        check(sp.characteristics() == (Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED),
              "ranged array derives SIZED|SUBSIZED plus given ORDERED (== 16464)");
        java.util.List<String> seen = new java.util.ArrayList<String>();
        sp.forEachRemaining(seen::add);
        check(seen.equals(java.util.Arrays.asList("b", "c")), "ranged array content [from,to)");
        // 全长与空区间
        Spliterator<String> full = Spliterators.spliterator(arr, 0, 4, 0);
        check(full.estimateSize() == 4, "full-range size");
        Spliterator<String> emptyRange = Spliterators.spliterator(arr, 2, 2, 0);
        check(emptyRange.estimateSize() == 0 && !emptyRange.tryAdvance(x -> { }),
              "from == to yields empty spliterator");
    }

    static void testArrayRangedErrors() {
        final String[] arr = {"a", "b", "c"};
        assertThrows(ArrayIndexOutOfBoundsException.class,
              () -> Spliterators.spliterator(arr, 2, 1, 0), "from > to throws AIOOBE");
        assertThrows(ArrayIndexOutOfBoundsException.class,
              () -> Spliterators.spliterator(arr, -1, 2, 0), "from < 0 throws AIOOBE");
        assertThrows(ArrayIndexOutOfBoundsException.class,
              () -> Spliterators.spliterator(arr, 1, 9, 0), "to > length throws AIOOBE");
        assertThrows(NullPointerException.class,
              () -> Spliterators.spliterator((String[]) null, 0), "null array throws NPE");
        assertThrows(NullPointerException.class,
              () -> Spliterators.spliterator((java.util.Collection<String>) null, 0),
              "null collection throws NPE");
    }

    static void testPrimitiveArraySpliterators() {
        Spliterator.OfInt si = Spliterators.spliterator(new int[] {5, 6, 7}, 0);
        check(si.characteristics() == (Spliterator.SIZED | Spliterator.SUBSIZED),
              "int[] spliterator derives SIZED|SUBSIZED (== 16448)");
        check(si.getExactSizeIfKnown() == 3, "int[] exact size");
        final java.util.List<Integer> ints = new java.util.ArrayList<Integer>();
        si.forEachRemaining((java.util.function.IntConsumer) ints::add);
        check(ints.equals(java.util.Arrays.asList(5, 6, 7)), "int[] content");
        Spliterator.OfLong sl = Spliterators.spliterator(new long[] {8L}, 0);
        check(sl.getExactSizeIfKnown() == 1, "long[] exact size");
        Spliterator.OfDouble sd = Spliterators.spliterator(new double[] {1.5}, 0);
        check(sd.getExactSizeIfKnown() == 1, "double[] exact size");
    }

    static void testEmptyPrimitiveSpliterators() {
        Spliterator.OfInt ei = Spliterators.emptyIntSpliterator();
        check(ei.characteristics() == (Spliterator.SIZED | Spliterator.SUBSIZED),
              "emptyIntSpliterator characteristics == 16448");
        check(ei.estimateSize() == 0 && ei.getExactSizeIfKnown() == 0, "emptyInt sizes 0");
        check(!ei.tryAdvance((java.util.function.IntConsumer) x -> { }),
              "emptyIntSpliterator tryAdvance false");
        final int[] calls = {0};
        ei.forEachRemaining((java.util.function.IntConsumer) x -> calls[0]++);
        check(calls[0] == 0, "emptyIntSpliterator forEachRemaining no-op");
        check(Spliterators.emptyLongSpliterator().estimateSize() == 0, "emptyLongSpliterator size 0");
        check(Spliterators.emptyDoubleSpliterator().estimateSize() == 0,
              "emptyDoubleSpliterator size 0");
    }

    static void testPrimitiveIteratorSpliterators() {
        // spliterator(PrimitiveIterator.OfInt, size, characteristics)
        java.util.PrimitiveIterator.OfInt pit = new java.util.PrimitiveIterator.OfInt() {
            int[] v = {5, 6, 7}; int i = 0;
            public int nextInt() { return v[i++]; }
            public boolean hasNext() { return i < v.length; }
        };
        Spliterator.OfInt sp = Spliterators.spliterator(pit, 3,
              Spliterator.ORDERED | Spliterator.SIZED);
        check(sp.getExactSizeIfKnown() == 3, "OfInt-adapter exact size");
        final java.util.List<Integer> seen = new java.util.ArrayList<Integer>();
        sp.forEachRemaining((java.util.function.IntConsumer) seen::add);
        check(seen.equals(java.util.Arrays.asList(5, 6, 7)), "OfInt-adapter content");
        // spliteratorUnknownSize(PrimitiveIterator.OfInt, characteristics)
        java.util.PrimitiveIterator.OfInt uns = new java.util.PrimitiveIterator.OfInt() {
            int[] v = {3, 4}; int i = 0;
            public int nextInt() { return v[i++]; }
            public boolean hasNext() { return i < v.length; }
        };
        Spliterator.OfInt u = Spliterators.spliteratorUnknownSize(uns, Spliterator.ORDERED);
        check(u.estimateSize() == Long.MAX_VALUE, "OfInt unknown-size estimateSize");
        final java.util.List<Integer> useen = new java.util.ArrayList<Integer>();
        u.forEachRemaining((java.util.function.IntConsumer) useen::add);
        check(useen.equals(java.util.Arrays.asList(3, 4)), "OfInt unknown-size content");
    }

    static void testIteratorBridge() {
        Spliterator<String> sp = Spliterators.spliterator(new String[] {"a", "b"}, 0);
        java.util.Iterator<String> it = Spliterators.iterator(sp);
        check(it.hasNext(), "bridge iterator hasNext before start");
        check("a".equals(it.next()) && "b".equals(it.next()), "bridge iterator content");
        check(!it.hasNext(), "bridge iterator exhausted");
        assertThrows(java.util.NoSuchElementException.class, it::next,
              "bridge iterator next after exhaustion NSEE");
        java.util.Iterator<String> rm = Spliterators.iterator(
              Spliterators.spliterator(new String[] {"x"}, 0));
        rm.next();
        assertThrows(UnsupportedOperationException.class, rm::remove,
              "bridge iterator remove unsupported");
        // OfInt 桥接
        Spliterator.OfInt pi = Spliterators.spliterator(new int[] {8, 9}, 0);
        java.util.PrimitiveIterator.OfInt pit = Spliterators.iterator(pi);
        check(pit.nextInt() == 8 && pit.nextInt() == 9, "OfInt bridge content");
        check(pit instanceof java.util.PrimitiveIterator.OfInt, "OfInt bridge type");
    }

    static void testCollectionAndUnknownSize() {
        java.util.List<Integer> list = new java.util.ArrayList<>(java.util.Arrays.asList(1, 2, 3));
        Spliterator<Integer> cs = Spliterators.spliterator(list, Spliterator.ORDERED);
        check(cs.characteristics() == (Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED),
              "collection adapter derives SIZED|SUBSIZED plus given ORDERED");
        check(cs.getExactSizeIfKnown() == 3, "collection adapter exact size");
        java.util.List<Integer> seen = new java.util.ArrayList<>();
        cs.forEachRemaining(seen::add);
        check(seen.equals(java.util.Arrays.asList(1, 2, 3)), "collection adapter content");
        // trySplit 后 SUBSIZED 尺寸相加
        Spliterator<Integer> s2 = Spliterators.spliterator(
              new java.util.ArrayList<>(java.util.Arrays.asList(1, 2, 3, 4)), Spliterator.ORDERED);
        Spliterator<Integer> half = s2.trySplit();
        check(half != null && half.getExactSizeIfKnown() + s2.getExactSizeIfKnown() == 4,
              "collection adapter trySplit sizes add up (SUBSIZED)");
        Spliterator<String> unk = Spliterators.spliteratorUnknownSize(
              java.util.Arrays.asList("x", "y").iterator(), Spliterator.ORDERED);
        check(unk.estimateSize() == Long.MAX_VALUE, "unknown-size estimateSize == Long.MAX_VALUE");
        check(!unk.hasCharacteristics(Spliterator.SIZED), "unknown-size not SIZED");
    }

    public static void main(String[] args) {
        testSpliterators();
        testArrayRanged();
        testArrayRangedErrors();
        testPrimitiveArraySpliterators();
        testEmptyPrimitiveSpliterators();
        testPrimitiveIteratorSpliterators();
        testIteratorBridge();
        testCollectionAndUnknownSize();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
