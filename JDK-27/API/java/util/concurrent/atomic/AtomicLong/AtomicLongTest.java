/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicLong conformance: white-box structure plus
 *          black-box behavior of get/set/lazySet/getAndSet/compareAndSet/weakCompareAndSet,
 *          increment/decrement/add, update/accumulate, Number views, ordering accessors,
 *          compareAndExchange, and a concurrent addAndGet.
 * @run main AtomicLongTest
 */

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicLongTest {
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
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicLong");
        check(!c.isInterface(), "AtomicLong is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicLong is concrete");
        check(c.getSuperclass() == Number.class, "AtomicLong extends java.lang.Number");
        check(Serializable.class.isAssignableFrom(c), "AtomicLong implements Serializable");
        c.getMethod("get");
        c.getMethod("getAndAdd", long.class);
        c.getMethod("addAndGet", long.class);
        c.getMethod("updateAndGet", java.util.function.LongUnaryOperator.class);
        c.getMethod("longValue");
        c.getMethod("doubleValue");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testScalar();
        testUpdateAndNumber();
        testAccessors();
        testConcurrent();
    }

    static void testScalar() {
        AtomicLong a = new AtomicLong();
        check(a.get() == 0L, "default constructor initial 0");
        check(new AtomicLong(5L).get() == 5L, "constructor(5L)");

        a.set(5L);
        check(a.get() == 5L, "set/get");
        check(a.getAndSet(7L) == 5L && a.get() == 7L, "getAndSet returns old, sets new");
        check(a.toString().equals("7"), "toString");

        a.lazySet(1L);
        check(a.get() == 1L, "lazySet then get");

        a.set(5L);
        check(a.incrementAndGet() == 6L, "incrementAndGet");
        check(a.getAndIncrement() == 6L && a.get() == 7L, "getAndIncrement returns old");
        check(a.decrementAndGet() == 6L, "decrementAndGet");
        check(a.getAndDecrement() == 6L && a.get() == 5L, "getAndDecrement returns old");

        check(a.getAndAdd(10L) == 5L && a.get() == 15L, "getAndAdd returns old");
        check(a.addAndGet(-3L) == 12L, "addAndGet");

        check(a.compareAndSet(12L, 99L) && a.get() == 99L, "compareAndSet success");
        check(!a.compareAndSet(0L, 1L) && a.get() == 99L, "compareAndSet failure returns false, unchanged");

        check(!a.weakCompareAndSet(0L, 1L) && a.get() == 99L, "weakCompareAndSet failure returns false");
        int spins = 0;
        while (!a.weakCompareAndSet(99L, 100L) && spins < 100_000) spins++;
        check(a.get() == 100L, "weakCompareAndSet eventually succeeds uncontended");
        check(a.weakCompareAndSetPlain(100L, 101L) && a.get() == 101L, "weakCompareAndSetPlain success");
        a.set(100L);
        check(a.weakCompareAndSetVolatile(100L, 102L) && a.get() == 102L, "weakCompareAndSetVolatile success");
        a.set(100L);
        check(a.weakCompareAndSetAcquire(100L, 103L) && a.get() == 103L, "weakCompareAndSetAcquire success");
        a.set(100L);
        check(a.weakCompareAndSetRelease(100L, 104L) && a.get() == 104L, "weakCompareAndSetRelease success");
    }

    static void testUpdateAndNumber() {
        AtomicLong a = new AtomicLong(10L);
        check(a.getAndUpdate(x -> x + 1L) == 10L && a.get() == 11L, "getAndUpdate returns old");
        check(a.updateAndGet(x -> x + 1L) == 12L, "updateAndGet returns new");
        check(a.getAndAccumulate(5L, (x, y) -> x + y) == 12L && a.get() == 17L, "getAndAccumulate returns old");
        check(a.accumulateAndGet(3L, (x, y) -> x * y) == 51L, "accumulateAndGet returns new");

        a.set(123L);
        check(a.intValue() == 123, "intValue");
        check(a.longValue() == 123L, "longValue");
        check(a.floatValue() == 123.0f, "floatValue");
        check(a.doubleValue() == 123.0, "doubleValue");
    }

    static void testAccessors() {
        AtomicLong a = new AtomicLong(0L);

        a.set(5L);
        check(a.compareAndExchange(5L, 7L) == 5L && a.get() == 7L, "compareAndExchange success returns old");
        check(a.compareAndExchange(5L, 9L) == 7L && a.get() == 7L, "compareAndExchange failure returns actual");
        a.set(5L);
        check(a.compareAndExchangeAcquire(5L, 8L) == 5L && a.get() == 8L, "compareAndExchangeAcquire");
        a.set(5L);
        check(a.compareAndExchangeRelease(5L, 6L) == 5L && a.get() == 6L, "compareAndExchangeRelease");

        a.setPlain(42L);
        check(a.getPlain() == 42L, "setPlain/getPlain");
        a.setOpaque(43L);
        check(a.getOpaque() == 43L, "setOpaque/getOpaque");
        a.setRelease(44L);
        check(a.getAcquire() == 44L, "setRelease/getAcquire");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 100_000;
        final AtomicLong a = new AtomicLong(0L);
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) a.addAndGet(1L);
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        check(a.get() == (long) N * M, "concurrent addAndGet loses no updates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
