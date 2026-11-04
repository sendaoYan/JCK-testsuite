/*
 * @test
 * @summary java.util.Spliterators conformance: white-box structure plus black-box behavior of
 *          array/empty spliterators, spliterator(Collection), spliteratorUnknownSize, and
 *          iterator conversion.
 * @run main SpliteratorsTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;

public class SpliteratorsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Spliterators");
        check(!c.isInterface(), "Spliterators is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Spliterators is final");
        c.getMethod("spliterator", Object[].class, int.class);
        c.getMethod("spliteratorUnknownSize", Iterator.class, int.class);
        c.getMethod("emptySpliterator");
        c.getMethod("iterator", Spliterator.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testArray();
        testEmpty();
        testCollection();
        testUnknownSize();
        testIteratorConversion();
    }

    static void testArray() {
        Spliterator<String> s = Spliterators.spliterator(new String[] { "a", "b", "c" }, 0);
        check(s.estimateSize() == 3, "array spliterator estimateSize");
        List<String> seen = new ArrayList<>();
        s.forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList("a", "b", "c")), "array spliterator iteration");
    }

    static void testEmpty() {
        Spliterator<Object> empty = Spliterators.emptySpliterator();
        check(empty.estimateSize() == 0, "emptySpliterator estimateSize");
        check(!empty.tryAdvance(x -> { }), "emptySpliterator has no elements");
    }

    static void testCollection() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
        Spliterator<Integer> s = Spliterators.spliterator(list, Spliterator.ORDERED);
        check(s.estimateSize() == 3, "collection spliterator estimateSize");
        check(s.hasCharacteristics(Spliterator.ORDERED), "collection spliterator ORDERED");
    }

    static void testUnknownSize() {
        Iterator<String> it = Arrays.asList("x", "y", "z").iterator();
        Spliterator<String> s = Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED);
        check(s.estimateSize() == Long.MAX_VALUE, "unknownSize estimateSize == Long.MAX_VALUE");
        List<String> seen = new ArrayList<>();
        s.forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList("x", "y", "z")), "unknownSize iteration");
    }

    static void testIteratorConversion() {
        Spliterator<String> sp = Spliterators.spliterator(new String[] { "a", "b" }, 0);
        Iterator<String> it = Spliterators.iterator(sp);
        check("a".equals(it.next()) && "b".equals(it.next()), "iterator from spliterator");
        check(!it.hasNext(), "iterator exhausted");
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
        List<String> seen = new ArrayList<>();
        sp.forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList("b", "c")), "ranged array content [from,to)");
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
        final List<Integer> ints = new ArrayList<>();
        si.forEachRemaining((java.util.function.IntConsumer) ints::add);
        check(ints.equals(Arrays.asList(5, 6, 7)), "int[] content");
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
        PrimitiveIterator.OfInt pit = new PrimitiveIterator.OfInt() {
            int[] v = {5, 6, 7}; int i = 0;
            public int nextInt() { return v[i++]; }
            public boolean hasNext() { return i < v.length; }
        };
        Spliterator.OfInt sp = Spliterators.spliterator(pit, 3,
              Spliterator.ORDERED | Spliterator.SIZED);
        check(sp.getExactSizeIfKnown() == 3, "OfInt-adapter exact size");
        final List<Integer> seen = new ArrayList<>();
        sp.forEachRemaining((java.util.function.IntConsumer) seen::add);
        check(seen.equals(Arrays.asList(5, 6, 7)), "OfInt-adapter content");
        PrimitiveIterator.OfInt uns = new PrimitiveIterator.OfInt() {
            int[] v = {3, 4}; int i = 0;
            public int nextInt() { return v[i++]; }
            public boolean hasNext() { return i < v.length; }
        };
        Spliterator.OfInt u = Spliterators.spliteratorUnknownSize(uns, Spliterator.ORDERED);
        check(u.estimateSize() == Long.MAX_VALUE, "OfInt unknown-size estimateSize");
        final List<Integer> useen = new ArrayList<>();
        u.forEachRemaining((java.util.function.IntConsumer) useen::add);
        check(useen.equals(Arrays.asList(3, 4)), "OfInt unknown-size content");
    }

    static void testIteratorBridge() {
        Spliterator<String> sp = Spliterators.spliterator(new String[] {"a", "b"}, 0);
        Iterator<String> it = Spliterators.iterator(sp);
        check(it.hasNext(), "bridge iterator hasNext before start");
        check("a".equals(it.next()) && "b".equals(it.next()), "bridge iterator content");
        check(!it.hasNext(), "bridge iterator exhausted");
        assertThrows(NoSuchElementException.class, it::next,
              "bridge iterator next after exhaustion NSEE");
        Iterator<String> rm = Spliterators.iterator(
              Spliterators.spliterator(new String[] {"x"}, 0));
        rm.next();
        assertThrows(UnsupportedOperationException.class, rm::remove,
              "bridge iterator remove unsupported");
        Spliterator.OfInt pi = Spliterators.spliterator(new int[] {8, 9}, 0);
        PrimitiveIterator.OfInt pit = Spliterators.iterator(pi);
        check(pit.nextInt() == 8 && pit.nextInt() == 9, "OfInt bridge content");
        check(pit instanceof PrimitiveIterator.OfInt, "OfInt bridge type");
    }

    static void testCollectionAndUnknownSize() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
        Spliterator<Integer> cs = Spliterators.spliterator(list, Spliterator.ORDERED);
        check(cs.characteristics() == (Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED),
              "collection adapter derives SIZED|SUBSIZED plus given ORDERED");
        check(cs.getExactSizeIfKnown() == 3, "collection adapter exact size");
        List<Integer> seen = new ArrayList<>();
        cs.forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList(1, 2, 3)), "collection adapter content");
        Spliterator<Integer> s2 = Spliterators.spliterator(
              new ArrayList<>(Arrays.asList(1, 2, 3, 4)), Spliterator.ORDERED);
        Spliterator<Integer> half = s2.trySplit();
        check(half != null && half.getExactSizeIfKnown() + s2.getExactSizeIfKnown() == 4,
              "collection adapter trySplit sizes add up (SUBSIZED)");
        Spliterator<String> unk = Spliterators.spliteratorUnknownSize(
              Arrays.asList("x", "y").iterator(), Spliterator.ORDERED);
        check(unk.estimateSize() == Long.MAX_VALUE, "unknown-size estimateSize == Long.MAX_VALUE");
        check(!unk.hasCharacteristics(Spliterator.SIZED), "unknown-size not SIZED");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
