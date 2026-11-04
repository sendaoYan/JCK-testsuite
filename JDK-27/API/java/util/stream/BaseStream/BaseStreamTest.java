/*
 * @test
 * @summary java.util.stream.BaseStream contract: sequential/parallel/isParallel return
 *          identity, unordered, onClose ordering + null->NPE, close idempotency, and
 *          iterator/spliterator single-use.
 * @run main BaseStreamTest
 */

import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

public class BaseStreamTest {
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

    static void testSequentialParallel() {
        BaseStream<Integer, Stream<Integer>> bs = Stream.of(1, 2, 3);
        check(!bs.isParallel(), "starts sequential");
        check(bs.sequential() == bs, "sequential() returns this");
        check(bs.parallel() == bs, "parallel() returns this");
        check(bs.isParallel(), "parallel() makes it parallel");
        check(bs.sequential() == bs && !bs.isParallel(), "sequential() back to sequential");
        check(bs.unordered().count() == 3, "unordered equivalent stream");
    }

    static void testIteratorSpliterator() {
        Iterator<Integer> it = Stream.of(1, 2, 3).iterator();
        check(it.hasNext() && it.next() == 1, "iterator first");
        check(it.next() == 2 && it.next() == 3 && !it.hasNext(), "iterator exhausts");
        Spliterator<Integer> sp = Stream.of(1, 2, 3).spliterator();
        check(sp.estimateSize() == 3, "spliterator size");
    }

    static void testOnClose() {
        final int[] order = new int[2];
        final int[] step = new int[1];
        Stream<Integer> s = Stream.of(1, 2)
                .onClose(() -> { order[0] = step[0]++; })
                .onClose(() -> { order[1] = step[0]++; });
        check(step[0] == 0, "handlers not run before close");
        s.close();
        check(step[0] == 2, "both handlers run");
        check(order[0] == 0 && order[1] == 1, "handlers run in registration order");
        checkThrows(NullPointerException.class, () -> Stream.of(1).onClose(null), "onClose(null) NPE");
    }

    static void testCloseIdempotent() {
        final int[] count = new int[1];
        Stream<Integer> s = Stream.of(1).onClose(() -> count[0]++);
        s.close();
        s.close();
        check(count[0] == 1, "close handler runs exactly once");
    }

    static void testSingleUse() {
        Stream<Integer> s = Stream.of(1, 2, 3);
        s.iterator();
        checkThrows(IllegalStateException.class, () -> s.count(), "terminal after iterator -> ISE");
    }

    public static void main(String[] args) {
        testSequentialParallel();
        testIteratorSpliterator();
        testOnClose();
        testCloseIdempotent();
        testSingleUse();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
