/*
 * @test
 * @summary java.util.PrimitiveIterator API conformance: hasNext/nextInt, and nextInt after
 *          exhaustion throwing NoSuchElementException.
 * @run main PrimitiveIteratorTest
 */

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

public class PrimitiveIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPrimitiveIterator() {
        PrimitiveIterator.OfInt it = IntStream.of(1, 2, 3).iterator();
        check(it.hasNext() && it.nextInt() == 1, "hasNext/nextInt");
        check(it.nextInt() == 2 && it.nextInt() == 3 && !it.hasNext(), "consume to exhaustion");
        try {
            it.nextInt();
            throw new AssertionError("nextInt exhausted: expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            passed++;
        }
    }


    // ===================== 契约深化：三种原生迭代器/混合 next()/forEachRemaining 双重载 =====================
    static void testPrimitiveIteratorContracts() {
        PrimitiveIterator.OfInt ii = java.util.stream.IntStream.of(1, 2, 3).iterator();
        check(ii.nextInt() == 1 && ii.next().equals(Integer.valueOf(2)),
              "OfInt mixes nextInt() and next()");
        java.util.List<Integer> sink = new java.util.ArrayList<Integer>();
        ii.forEachRemaining((java.util.function.IntConsumer) v -> sink.add(v));
        check(sink.size() == 1 && sink.get(0) == 3, "IntConsumer forEachRemaining consumes rest");
        check(!ii.hasNext(), "exhausted after forEachRemaining");
        try { ii.nextInt(); check(false, "OfInt exhausted NSEE"); }
        catch (java.util.NoSuchElementException nsee) { check(true, "OfInt exhausted NSEE"); }
        PrimitiveIterator.OfLong il = java.util.stream.LongStream.of(10L, 20L).iterator();
        check(il.nextLong() == 10L && il.next().equals(Long.valueOf(20L)),
              "OfLong mixes nextLong() and next()");
        try { il.nextLong(); check(false, "OfLong exhausted NSEE"); }
        catch (java.util.NoSuchElementException nsee) { check(true, "OfLong exhausted NSEE"); }
        PrimitiveIterator.OfDouble idd = java.util.stream.DoubleStream.of(0.5, 1.5).iterator();
        check(idd.nextDouble() == 0.5 && idd.next().equals(Double.valueOf(1.5)),
              "OfDouble mixes nextDouble() and next()");
        java.util.List<Double> dsink = new java.util.ArrayList<Double>();
        java.util.stream.DoubleStream.of(2.5).iterator()
              .forEachRemaining((java.util.function.DoubleConsumer) v -> dsink.add(v));
        check(dsink.size() == 1 && dsink.get(0) == 2.5, "DoubleConsumer forEachRemaining");
        PrimitiveIterator.OfInt mixed = java.util.stream.IntStream.of(7).iterator();
        java.util.List<Integer> both = new java.util.ArrayList<Integer>();
        mixed.forEachRemaining((java.util.function.Consumer<Integer>) both::add);
        check(both.size() == 1 && both.get(0) == 7,
              "boxed Consumer overload also drains the iterator");
    }

    // ===================== 契约深化：自定义实现探针/空 NPE/双重载分派/Spliterators 桥接/类型标记 =====================

    /** 固定 int 序列的自定义 OfInt 探针（只实现 hasNext/nextInt）。 */
    static PrimitiveIterator.OfInt ofInt(final int[] v) {
        return new PrimitiveIterator.OfInt() {
            int i = 0;
            public boolean hasNext() { return i < v.length; }
            public int nextInt() {
                if (i >= v.length) throw new java.util.NoSuchElementException();
                return v[i++];
            }
        };
    }

    static void testCustomOfInt() {
        PrimitiveIterator.OfInt it = ofInt(new int[] {10, 20, 30});
        check(it.hasNext(), "custom OfInt hasNext");
        check(it.nextInt() == 10, "custom OfInt nextInt 1");
        check(it.next().equals(Integer.valueOf(20)), "default next() boxes for OfInt");
        final java.util.List<Integer> sink = new java.util.ArrayList<Integer>();
        it.forEachRemaining((java.util.function.IntConsumer) sink::add);
        check(sink.size() == 1 && sink.get(0) == 30, "IntConsumer overload drains rest");
        check(!it.hasNext(), "custom OfInt exhausted");
        // 耗尽后再 forEachRemaining 是 no-op
        final int[] calls = {0};
        it.forEachRemaining((java.util.function.IntConsumer) x -> calls[0]++);
        check(calls[0] == 0, "forEachRemaining after exhaustion no-op");
        try { it.nextInt(); check(false, "exhausted nextInt NSEE"); }
        catch (java.util.NoSuchElementException e) { check(true, "exhausted nextInt NSEE"); }
        // null consumer → NPE
        try {
            ofInt(new int[] {1}).forEachRemaining((java.util.function.IntConsumer) null);
            check(false, "forEachRemaining(null IntConsumer) NPE");
        } catch (NullPointerException e) { check(true, "forEachRemaining(null IntConsumer) NPE"); }
        // boxed Consumer 重载同样排干
        final java.util.List<Integer> boxed = new java.util.ArrayList<Integer>();
        ofInt(new int[] {4, 5}).forEachRemaining((java.util.function.Consumer<Integer>) boxed::add);
        check(boxed.size() == 2 && boxed.get(0) == 4 && boxed.get(1) == 5,
              "boxed Consumer overload drains");
        // 空序列：hasNext false + nextInt NSEE
        PrimitiveIterator.OfInt empty = ofInt(new int[0]);
        check(!empty.hasNext(), "empty custom OfInt");
        try { empty.nextInt(); check(false, "empty custom nextInt NSEE"); }
        catch (java.util.NoSuchElementException e) { check(true, "empty custom nextInt NSEE"); }
        // Iterator.remove 默认实现（无状态）→ UnsupportedOperationException
        PrimitiveIterator.OfInt rm = ofInt(new int[] {1});
        rm.nextInt();
        try { rm.remove(); check(false, "remove default UOE"); }
        catch (UnsupportedOperationException e) { check(true, "remove default UOE"); }
    }

    static void testCustomOfLongOfDouble() {
        PrimitiveIterator.OfLong ol = new PrimitiveIterator.OfLong() {
            long[] v = {100L, 200L}; int i = 0;
            public boolean hasNext() { return i < v.length; }
            public long nextLong() {
                if (i >= v.length) throw new java.util.NoSuchElementException();
                return v[i++];
            }
        };
        check(ol.nextLong() == 100L && ol.next().equals(Long.valueOf(200L)),
              "custom OfLong nextLong/next mix");
        try { ol.nextLong(); check(false, "custom OfLong exhausted NSEE"); }
        catch (java.util.NoSuchElementException e) { check(true, "custom OfLong exhausted NSEE"); }
        try {
            new PrimitiveIterator.OfLong() {
                public boolean hasNext() { return false; }
                public long nextLong() { throw new java.util.NoSuchElementException(); }
            }.forEachRemaining((java.util.function.LongConsumer) null);
            check(false, "OfLong forEachRemaining(null) NPE");
        } catch (NullPointerException e) { check(true, "OfLong forEachRemaining(null) NPE"); }

        PrimitiveIterator.OfDouble od = new PrimitiveIterator.OfDouble() {
            double[] v = {1.25, 2.5}; int i = 0;
            public boolean hasNext() { return i < v.length; }
            public double nextDouble() {
                if (i >= v.length) throw new java.util.NoSuchElementException();
                return v[i++];
            }
        };
        check(od.nextDouble() == 1.25 && od.next().equals(Double.valueOf(2.5)),
              "custom OfDouble nextDouble/next mix");
        final java.util.List<Double> drained = new java.util.ArrayList<Double>();
        new PrimitiveIterator.OfDouble() {
            double[] v = {0.5}; int i = 0;
            public boolean hasNext() { return i < v.length; }
            public double nextDouble() { return v[i++]; }
        }.forEachRemaining((java.util.function.DoubleConsumer) drained::add);
        check(drained.size() == 1 && drained.get(0) == 0.5, "OfDouble forEachRemaining drains");
        try { od.nextDouble(); check(false, "custom OfDouble exhausted NSEE"); }
        catch (java.util.NoSuchElementException e) { check(true, "custom OfDouble exhausted NSEE"); }
    }

    static void testSpliteratorsBridge() {
        // Spliterators.iterator(OfInt) 桥接：内容与类型
        PrimitiveIterator.OfInt pit = java.util.Spliterators.iterator(
              java.util.Spliterators.spliterator(new int[] {8, 9}, java.util.Spliterator.ORDERED));
        check(pit.nextInt() == 8 && pit.nextInt() == 9, "Spliterators.iterator(OfInt) content");
        check(!pit.hasNext(), "bridged OfInt exhausted");
        // 反向：Spliterators.spliterator(PrimitiveIterator.OfInt, size, chars)
        java.util.Spliterator.OfInt sp = java.util.Spliterators.spliterator(
              ofInt(new int[] {3, 4, 5}), 3,
              java.util.Spliterator.ORDERED | java.util.Spliterator.SIZED);
        check(sp.getExactSizeIfKnown() == 3, "adapter spliterator exact size");
        final java.util.List<Integer> seen = new java.util.ArrayList<Integer>();
        sp.forEachRemaining((java.util.function.IntConsumer) seen::add);
        check(seen.size() == 3 && seen.get(0) == 3 && seen.get(2) == 5, "adapter spliterator content");
    }

    static void testNotPrimitive() {
        // 普通装箱迭代器不是 PrimitiveIterator
        java.util.Iterator<Integer> plain = new java.util.ArrayList<Integer>(
              java.util.Arrays.asList(1, 2)).iterator();
        check(!(plain instanceof PrimitiveIterator.OfInt), "ArrayList iterator is not OfInt");
        check(!(plain instanceof PrimitiveIterator), "ArrayList iterator is not PrimitiveIterator");
    }

    public static void main(String[] args) {
        testPrimitiveIterator();
        testPrimitiveIteratorContracts();
        testCustomOfInt();
        testCustomOfLongOfDouble();
        testSpliteratorsBridge();
        testNotPrimitive();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
