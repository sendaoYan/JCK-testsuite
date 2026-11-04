/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicInteger conformance: white-box structure plus
 *          black-box behavior of get/set/lazySet/getAndSet/compareAndSet/weakCompareAndSet,
 *          increment/decrement/add, update/accumulate, Number views, overflow wrapping,
 *          serialization round-trip, and a concurrent incrementAndGet.
 * @run main AtomicIntegerTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerTest {
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
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicInteger");
        check(!c.isInterface(), "AtomicInteger is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicInteger is concrete");
        check(c.getSuperclass() == Number.class, "AtomicInteger extends java.lang.Number");
        check(Serializable.class.isAssignableFrom(c), "AtomicInteger implements Serializable");
        c.getMethod("get");
        c.getMethod("incrementAndGet");
        c.getMethod("getAndAdd", int.class);
        c.getMethod("updateAndGet", java.util.function.IntUnaryOperator.class);
        c.getMethod("intValue");
        c.getMethod("doubleValue");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testScalar();
        testUpdateAndNumber();
        testOverflow();
        testSerialization();
        testConcurrent();
    }

    static void testScalar() {
        AtomicInteger a = new AtomicInteger();
        check(a.get() == 0, "default constructor initial 0");
        check(new AtomicInteger(5).get() == 5, "constructor(5)");

        a.set(5);
        check(a.get() == 5, "set/get");
        check(a.getAndSet(7) == 5 && a.get() == 7, "getAndSet returns old, sets new");
        check(a.toString().equals("7"), "toString");

        a.lazySet(1);
        // lazySet only promises eventual visibility; force a full ordering with a
        // subsequent volatile read-modify-write before observing.
        a.getAndAdd(0);
        check(a.get() == 1, "lazySet then ordered read");

        // increment / decrement family
        a.set(5);
        check(a.incrementAndGet() == 6, "incrementAndGet");
        check(a.getAndIncrement() == 6 && a.get() == 7, "getAndIncrement returns old");
        check(a.decrementAndGet() == 6, "decrementAndGet");
        check(a.getAndDecrement() == 6 && a.get() == 5, "getAndDecrement returns old");

        // add family
        check(a.getAndAdd(10) == 5 && a.get() == 15, "getAndAdd returns old");
        check(a.addAndGet(-3) == 12, "addAndGet");

        // compareAndSet
        check(a.compareAndSet(12, 99) && a.get() == 99, "compareAndSet success");
        check(!a.compareAndSet(0, 1) && a.get() == 99, "compareAndSet failure returns false, unchanged");

        // weakCompareAndSet failure deterministic, success retried (bounded)
        check(!a.weakCompareAndSet(0, 1) && a.get() == 99, "weakCompareAndSet failure returns false");
        int spins = 0;
        while (!a.weakCompareAndSet(99, 100) && spins < 100_000) spins++;
        check(a.get() == 100, "weakCompareAndSet eventually succeeds uncontended");
    }

    static void testUpdateAndNumber() {
        AtomicInteger a = new AtomicInteger(10);
        check(a.getAndUpdate(x -> x + 1) == 10 && a.get() == 11, "getAndUpdate returns old");
        check(a.updateAndGet(x -> x + 1) == 12, "updateAndGet returns new");
        check(a.getAndAccumulate(5, (x, y) -> x + y) == 12 && a.get() == 17, "getAndAccumulate returns old");
        check(a.accumulateAndGet(3, (x, y) -> x * y) == 51, "accumulateAndGet returns new");

        // Number views
        a.set(123);
        check(a.intValue() == 123, "intValue");
        check(a.longValue() == 123L, "longValue");
        check(a.floatValue() == 123.0f, "floatValue");
        check(a.doubleValue() == 123.0, "doubleValue");
    }

    static void testOverflow() {
        AtomicInteger a = new AtomicInteger(Integer.MAX_VALUE);
        check(a.incrementAndGet() == Integer.MIN_VALUE, "incrementAndGet wraps MAX_VALUE to MIN_VALUE");
        check(a.getAndDecrement() == Integer.MIN_VALUE && a.get() == Integer.MAX_VALUE, "decrement wraps MIN_VALUE to MAX_VALUE");
        AtomicInteger b = new AtomicInteger(Integer.MIN_VALUE);
        check(b.decrementAndGet() == Integer.MAX_VALUE, "decrementAndGet wraps MIN_VALUE to MAX_VALUE");
    }

    @SuppressWarnings("unchecked")
    static void testSerialization() throws Exception {
        AtomicInteger a = new AtomicInteger(42);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(a);
        }
        AtomicInteger copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            copy = (AtomicInteger) ois.readObject();
        }
        check(copy.get() == 42, "serialization round-trip preserves value");
        copy.compareAndSet(42, 43);
        check(copy.get() == 43, "deserialized instance remains operational");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 100_000;
        final AtomicInteger a = new AtomicInteger(0);
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) a.incrementAndGet();
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        check(a.get() == N * M, "concurrent incrementAndGet loses no updates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
