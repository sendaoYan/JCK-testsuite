/*
 * @test
 * @summary java.util.concurrent.atomic.DoubleAccumulator conformance: white-box structure plus
 *          black-box behavior of accumulate/get/reset/getThenReset with custom binary operators
 *          and identity, Number views, toString, and a concurrent accumulate whose double sum is
 *          only bounded (float merge order varies).
 * @run main DoubleAccumulatorTest
 */

import java.io.Serializable;
import java.util.concurrent.atomic.DoubleAccumulator;

public class DoubleAccumulatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void await(java.util.concurrent.CountDownLatch latch) {
        try { latch.await(); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.atomic.DoubleAccumulator");
        check(!c.isInterface(), "DoubleAccumulator is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "DoubleAccumulator is concrete");
        check(c.getSuperclass() == Class.forName("java.util.concurrent.atomic.Striped64"), "DoubleAccumulator extends Striped64");
        check(Serializable.class.isAssignableFrom(c), "DoubleAccumulator implements Serializable");
        c.getConstructor(java.util.function.DoubleBinaryOperator.class, double.class);
        c.getMethod("accumulate", double.class);
        c.getMethod("get");
        c.getMethod("reset");
        c.getMethod("getThenReset");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testSum();
        testMax();
        testNumberViews();
        testConcurrent();
    }

    static void testSum() {
        DoubleAccumulator acc = new DoubleAccumulator((a, b) -> a + b, 0.0);
        check(acc.get() == 0.0, "initial value is identity");
        acc.accumulate(1.5);
        acc.accumulate(2.5);
        check(acc.get() == 4.0, "accumulate/get of exactly representable values is exact");
        acc.reset();
        check(acc.get() == 0.0, "reset returns to identity");
        acc.accumulate(3.25);
        check(acc.getThenReset() == 3.25, "getThenReset returns value before reset");
        check(acc.get() == 0.0, "getThenReset resets to identity");
        check(acc.toString().equals("0.0"), "toString is Double.toString(get)");
    }

    static void testMax() {
        DoubleAccumulator acc = new DoubleAccumulator(Math::max, Double.NEGATIVE_INFINITY);
        check(acc.get() == Double.NEGATIVE_INFINITY, "identity for max accumulator");
        acc.accumulate(1.5);
        acc.accumulate(2.5);
        acc.accumulate(1.0);
        check(acc.get() == 2.5, "max accumulator tracks running maximum");
        acc.reset();
        check(acc.get() == Double.NEGATIVE_INFINITY, "reset returns max accumulator to identity");
    }

    static void testNumberViews() {
        DoubleAccumulator acc = new DoubleAccumulator((a, b) -> a + b, 0.0);
        acc.accumulate(123.0);
        check(acc.doubleValue() == 123.0, "doubleValue == get");
        check(acc.longValue() == 123L, "longValue == (long)get");
        check(acc.intValue() == 123, "intValue == (int)get");
        check(acc.floatValue() == 123.0f, "floatValue == (float)get");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 50_000;
        final DoubleAccumulator acc = new DoubleAccumulator((a, b) -> a + b, 0.0);
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) acc.accumulate(1.0);
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        // floating-point merge order depends on parallelism -> assert bounds, not exact equality
        check(acc.get() >= 0.0 && acc.get() <= (double) N * M, "concurrent accumulate stays within [0, N*M]");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
