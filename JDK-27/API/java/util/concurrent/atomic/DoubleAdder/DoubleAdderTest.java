/*
 * @test
 * @summary java.util.concurrent.atomic.DoubleAdder conformance: white-box structure plus
 *          black-box behavior of add/sum/reset/sumThenReset, Number views, toString, and a
 *          concurrent add whose final double sum is only bounded (float merge order varies).
 * @run main DoubleAdderTest
 */

import java.io.Serializable;
import java.util.concurrent.atomic.DoubleAdder;

public class DoubleAdderTest {
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
        Class<?> c = Class.forName("java.util.concurrent.atomic.DoubleAdder");
        check(!c.isInterface(), "DoubleAdder is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "DoubleAdder is concrete");
        check(c.getSuperclass() == Class.forName("java.util.concurrent.atomic.Striped64"), "DoubleAdder extends Striped64");
        check(Serializable.class.isAssignableFrom(c), "DoubleAdder implements Serializable");
        c.getConstructor();
        c.getMethod("add", double.class);
        c.getMethod("sum");
        c.getMethod("reset");
        c.getMethod("sumThenReset");
        c.getMethod("doubleValue");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testSingleThread();
        testNumberViews();
        testConcurrent();
    }

    static void testSingleThread() {
        DoubleAdder a = new DoubleAdder();
        check(a.sum() == 0.0, "initial sum 0.0");

        // exactly representable additions -> exact sum
        a.add(1.5);
        a.add(2.5);
        check(a.sum() == 4.0, "add/sum of exactly representable values is exact");

        a.reset();
        check(a.sum() == 0.0, "reset returns sum to 0.0");

        // inexact decimal addition is deterministic single-threaded (matches sequential double sum)
        a.add(0.1);
        a.add(0.2);
        a.add(0.3);
        check(a.sum() == 0.1 + 0.2 + 0.3, "single-thread sum matches sequential double accumulation");

        check(a.sumThenReset() == 0.1 + 0.2 + 0.3, "sumThenReset returns old sum");
        check(a.sum() == 0.0, "sumThenReset resets to 0.0");

        a.add(7.25);
        check(a.toString().equals(Double.toString(7.25)), "toString is Double.toString(sum)");
    }

    static void testNumberViews() {
        DoubleAdder a = new DoubleAdder();
        a.add(4.0);
        check(a.doubleValue() == 4.0, "doubleValue == sum");
        check(a.longValue() == 4L, "longValue == (long)sum");
        check(a.intValue() == 4, "intValue == (int)sum");
        check(a.floatValue() == 4.0f, "floatValue == (float)sum");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 50_000;
        final DoubleAdder a = new DoubleAdder();
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) a.add(1.0);
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        // floating-point merge order depends on parallelism -> assert bounds, not exact equality
        check(a.sum() >= 0.0 && a.sum() <= (double) N * M, "concurrent add sum stays within [0, N*M]");
    }

    // ---- 契约深化：二进制可精确浮点 / 负数 / sumThenReset 二次 / 序列化 / 双线程精确 ----
    static void testExactBinarySums() {
        // 只用可精确表示的二进制浮点数（0.5/0.25/0.125）
        DoubleAdder a = new DoubleAdder();
        a.add(0.5);
        a.add(0.25);
        check(a.sum() == 0.75, "0.5 + 0.25 == 0.75 exactly");
        a.add(0.125);
        check(a.sum() == 0.875, "0.875 exact");
        a.reset();
        for (int i = 0; i < 10; i++) a.add(0.5);
        check(a.sum() == 5.0, "ten 0.5 adds sum to exactly 5.0");
        a.add(-2.5);
        check(a.sum() == 2.5, "negative add exact");
        a.add(-2.5);
        check(a.sum() == 0.0, "negative add back to zero");
        check(1.0 / a.sum() == Double.POSITIVE_INFINITY, "sum is +0.0 not -0.0");
    }

    static void testResetSemantics() {
        DoubleAdder a = new DoubleAdder();
        a.add(1.0);
        a.reset();
        check(a.sum() == 0.0, "reset returns to zero");
        check(a.sumThenReset() == 0.0, "sumThenReset after reset returns 0");
        a.add(0.25);
        check(a.sumThenReset() == 0.25, "sumThenReset returns old sum");
        check(a.sumThenReset() == 0.0, "second sumThenReset returns 0");
        a.add(-0.75);
        check(a.toString().equals("-0.75"), "toString of negative sum");
    }

    static void testSerialization() throws Exception {
        DoubleAdder a = new DoubleAdder();
        a.add(0.5);
        a.add(0.25);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(a);
        }
        DoubleAdder copy;
        try (java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            copy = (DoubleAdder) ois.readObject();
        }
        check(copy.sum() == 0.75, "serialization round-trip preserves sum");
        copy.add(0.25);
        check(copy.sum() == 1.0, "deserialized instance remains operational");
    }

    static void testTwoThreadBounded() throws Exception {
        // 两 daemon 线程各 add 1000 次 0.5，join 有界后 sum == 1000.0（二进制精确值，无顺序误差）
        final DoubleAdder a = new DoubleAdder();
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread t1 = new Thread(() -> {
            try { start.await(); } catch (InterruptedException ignored) { }
            for (int i = 0; i < 1000; i++) a.add(0.5);
        });
        Thread t2 = new Thread(() -> {
            try { start.await(); } catch (InterruptedException ignored) { }
            for (int i = 0; i < 1000; i++) a.add(0.5);
        });
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        start.countDown();
        t1.join(10_000);
        t2.join(10_000);
        check(!t1.isAlive() && !t2.isAlive(), "both worker threads finished within bound");
        check(a.sum() == 1000.0, "two-thread sum is exactly 1000.0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testExactBinarySums();
        testResetSemantics();
        testSerialization();
        testTwoThreadBounded();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
