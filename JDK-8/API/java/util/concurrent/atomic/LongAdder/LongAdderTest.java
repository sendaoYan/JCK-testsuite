/*
 * @test
 * @summary java.util.concurrent.atomic.LongAdder conformance: white-box structure plus
 *          black-box behavior of add/increment/decrement/sum/reset/sumThenReset, Number views,
 *          toString, serialization round-trip, and a concurrent add with an exact final sum.
 * @run main LongAdderTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.LongAdder;

public class LongAdderTest {
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
        Class<?> c = Class.forName("java.util.concurrent.atomic.LongAdder");
        check(!c.isInterface(), "LongAdder is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "LongAdder is concrete");
        check(c.getSuperclass() == Class.forName("java.util.concurrent.atomic.Striped64"), "LongAdder extends Striped64");
        check(Serializable.class.isAssignableFrom(c), "LongAdder implements Serializable");
        c.getConstructor();
        c.getMethod("add", long.class);
        c.getMethod("increment");
        c.getMethod("decrement");
        c.getMethod("sum");
        c.getMethod("sumThenReset");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testSingleThread();
        testNumberViews();
        testSerialization();
        testConcurrent();
    }

    static void testSingleThread() {
        LongAdder a = new LongAdder();
        check(a.sum() == 0L, "initial sum 0");

        a.add(5L);
        a.add(7L);
        check(a.sum() == 12L, "add/sum");

        a.increment();
        check(a.sum() == 13L, "increment");
        a.decrement();
        check(a.sum() == 12L, "decrement");

        a.reset();
        check(a.sum() == 0L, "reset returns sum to 0");

        a.add(42L);
        check(a.sumThenReset() == 42L, "sumThenReset returns old sum");
        check(a.sum() == 0L, "sumThenReset resets to 0");

        a.add(9L);
        check(a.toString().equals("9"), "toString is Long.toString(sum)");
    }

    static void testNumberViews() {
        LongAdder a = new LongAdder();
        a.add(123L);
        check(a.longValue() == 123L, "longValue == sum");
        check(a.intValue() == 123, "intValue == (int)sum");
        check(a.floatValue() == 123.0f, "floatValue == (float)sum");
        check(a.doubleValue() == 123.0, "doubleValue == (double)sum");
    }

    @SuppressWarnings("unchecked")
    static void testSerialization() throws Exception {
        LongAdder a = new LongAdder();
        a.add(7L);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(a);
        }
        LongAdder copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            copy = (LongAdder) ois.readObject();
        }
        check(copy.sum() == 7L, "serialization round-trip preserves sum");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 100_000;
        final LongAdder a = new LongAdder();
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) a.add(1L);
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        check(a.sum() == (long) N * M, "concurrent add loses no updates (exact long sum)");
    }

    // ---- 契约深化：负数 add / decrement 越零 / sumThenReset 二次调用 / 双线程有界并发 ----
    static void testNegativeAndReset() {
        LongAdder a = new LongAdder();
        a.add(-5L);
        check(a.sum() == -5L, "negative add");
        a.add(-7L);
        check(a.sum() == -12L, "second negative add accumulates exactly");
        a.increment();
        check(a.sum() == -11L, "increment from negative");
        a.decrement();
        a.decrement();
        check(a.sum() == -13L, "decrement below zero");
        // sumThenReset 二次调用：第二次返回 0
        check(a.sumThenReset() == -13L, "first sumThenReset returns old sum");
        check(a.sumThenReset() == 0L, "second sumThenReset returns 0 after reset");
        check(a.sum() == 0L, "sum stays 0");
        // toString == sum 的字符串（含负值）
        a.add(-9L);
        check(a.toString().equals("-9"), "toString of negative sum");
        a.reset();
        check(a.toString().equals("0"), "toString of zero sum");
        // 0 与极值
        a.add(Long.MAX_VALUE);
        check(a.sum() == Long.MAX_VALUE, "sum of Long.MAX_VALUE");
        a.add(1L);
        check(a.sum() == Long.MIN_VALUE, "long sum wraps (no exception)");
    }

    static void testTwoThreadBounded() throws Exception {
        // 两 daemon 线程各 add 1000 次，join 有界后 sum == 2000（确定性结果断言）
        final LongAdder a = new LongAdder();
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread t1 = new Thread(() -> {
            try { start.await(); } catch (InterruptedException ignored) { }
            for (int i = 0; i < 1000; i++) a.add(1L);
        });
        Thread t2 = new Thread(() -> {
            try { start.await(); } catch (InterruptedException ignored) { }
            for (int i = 0; i < 1000; i++) a.increment();
        });
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        start.countDown();
        t1.join(10_000);
        t2.join(10_000);
        check(!t1.isAlive() && !t2.isAlive(), "both worker threads finished within bound");
        check(a.sum() == 2000L, "two-thread add total is exactly 2000");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNegativeAndReset();
        testTwoThreadBounded();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
