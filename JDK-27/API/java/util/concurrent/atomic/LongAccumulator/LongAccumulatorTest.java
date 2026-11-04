/*
 * @test
 * @summary java.util.concurrent.atomic.LongAccumulator conformance: white-box structure plus
 *          black-box behavior of accumulate/get/reset/getThenReset with a custom binary operator
 *          and identity, Number views, toString, and a concurrent accumulate with exact sum.
 * @run main LongAccumulatorTest
 */

import java.io.Serializable;
import java.util.concurrent.atomic.LongAccumulator;

public class LongAccumulatorTest {
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
        Class<?> c = Class.forName("java.util.concurrent.atomic.LongAccumulator");
        check(!c.isInterface(), "LongAccumulator is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "LongAccumulator is concrete");
        check(c.getSuperclass() == Class.forName("java.util.concurrent.atomic.Striped64"), "LongAccumulator extends Striped64");
        check(Serializable.class.isAssignableFrom(c), "LongAccumulator implements Serializable");
        c.getConstructor(java.util.function.LongBinaryOperator.class, long.class);
        c.getMethod("accumulate", long.class);
        c.getMethod("get");
        c.getMethod("reset");
        c.getMethod("getThenReset");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testMultiplication();
        testAdditionAndViews();
        testConcurrent();
    }

    static void testMultiplication() {
        LongAccumulator acc = new LongAccumulator((a, b) -> a * b, 1L);
        check(acc.get() == 1L, "initial value is identity");
        acc.accumulate(2L);
        acc.accumulate(3L);
        check(acc.get() == 6L, "accumulate/get applies operator left-to-right");
        acc.accumulate(4L);
        check(acc.get() == 24L, "further accumulation continues from current value");
        acc.reset();
        check(acc.get() == 1L, "reset returns to identity");
        acc.accumulate(5L);
        check(acc.getThenReset() == 5L, "getThenReset returns value before reset");
        check(acc.get() == 1L, "getThenReset resets to identity");
        check(acc.toString().equals("1"), "toString is Long.toString(get)");
    }

    static void testAdditionAndViews() {
        LongAccumulator acc = new LongAccumulator((a, b) -> a + b, 0L);
        acc.accumulate(10L);
        acc.accumulate(20L);
        acc.accumulate(30L);
        check(acc.get() == 60L, "sum-like accumulator");
        check(acc.longValue() == 60L, "longValue == get");
        check(acc.intValue() == 60, "intValue == (int)get");
        check(acc.floatValue() == 60.0f, "floatValue == (float)get");
        check(acc.doubleValue() == 60.0, "doubleValue == (double)get");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 100_000;
        final LongAccumulator acc = new LongAccumulator((a, b) -> a + b, 0L);
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) acc.accumulate(1L);
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        check(acc.get() == (long) N * M, "concurrent accumulate loses no updates (exact sum)");
    }

    // ---- 契约深化：64 位环绕契约 / max 函数 / getThenReset 链 / 负数 ----
    static void testOverflowWrapContract() {
        // 经典契约点（JDK-27 实测）：get()/longValue() 按补码 64 位环绕，不抛 ArithmeticException
        LongAccumulator acc = new LongAccumulator(Long::sum, 0L);
        acc.accumulate(Long.MAX_VALUE);
        acc.accumulate(Long.MAX_VALUE);
        check(acc.get() == -2L, "MAX_VALUE twice wraps to -2 (no ArithmeticException)");
        check(acc.longValue() == -2L, "longValue matches wrapped sum");
        check(acc.intValue() == -2, "intValue of wrapped sum");
        LongAccumulator neg = new LongAccumulator(Long::sum, 0L);
        neg.accumulate(Long.MIN_VALUE);
        neg.accumulate(-1L);
        check(neg.get() == Long.MAX_VALUE, "MIN_VALUE + (-1) wraps to MAX_VALUE");
        check(neg.doubleValue() == 9.223372036854776E18, "doubleValue of wrapped MAX");
    }

    static void testMaxAccumulator() {
        LongAccumulator max = new LongAccumulator(Math::max, Long.MIN_VALUE);
        check(max.get() == Long.MIN_VALUE, "max identity");
        max.accumulate(5L);
        max.accumulate(3L);
        max.accumulate(9L);
        max.accumulate(-1L);
        check(max.get() == 9L, "Math::max keeps maximum");
        max.accumulate(9L);
        check(max.get() == 9L, "accumulating equal max keeps value");
        max.reset();
        check(max.get() == Long.MIN_VALUE, "reset returns to max identity");
        max.accumulate(-100L);
        check(max.getThenReset() == -100L, "getThenReset on max accumulator");
        check(max.get() == Long.MIN_VALUE, "max accumulator reset to identity");
    }

    static void testSumDetails() {
        LongAccumulator acc = new LongAccumulator(Long::sum, 0L);
        acc.accumulate(-5L);
        acc.accumulate(-10L);
        check(acc.get() == -15L, "negative accumulate exact");
        check(acc.toString().equals("-15"), "toString == Long.toString(get)");
        check(acc.getThenReset() == -15L, "getThenReset returns value");
        check(acc.getThenReset() == 0L, "second getThenReset returns identity");
        check(acc instanceof Number, "LongAccumulator is a Number");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOverflowWrapContract();
        testMaxAccumulator();
        testSumDetails();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
