/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicLongArray conformance: white-box structure plus
 *          black-box behavior of indexed get/set/lazySet/getAndSet/compareAndSet/weakCompareAndSet,
 *          increment/decrement/add, update/accumulate, toString, ordering accessors,
 *          compareAndExchange, copy-on-construct, index bounds, and concurrent per-index updates.
 * @run main AtomicLongArrayTest
 */

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLongArray;

public class AtomicLongArrayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    static void await(java.util.concurrent.CountDownLatch latch) {
        try { latch.await(); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicLongArray");
        check(!c.isInterface(), "AtomicLongArray is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicLongArray is concrete");
        check(c.getSuperclass() == Object.class, "AtomicLongArray extends Object");
        check(Serializable.class.isAssignableFrom(c), "AtomicLongArray implements Serializable");
        c.getConstructor(int.class);
        c.getConstructor(long[].class);
        c.getMethod("length");
        c.getMethod("get", int.class);
        c.getMethod("compareAndSet", int.class, long.class, long.class);
        c.getMethod("addAndGet", int.class, long.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testScalar();
        testUpdateAccessors();
        testConstructionAndBounds();
        testConcurrent();
    }

    static void testScalar() {
        AtomicLongArray a = new AtomicLongArray(3);
        check(a.length() == 3, "length");
        check(a.get(0) == 0L, "initial 0");

        a.set(0, 5L);
        check(a.get(0) == 5L, "set/get");

        check(a.getAndSet(0, 7L) == 5L && a.get(0) == 7L, "getAndSet returns old");

        a.lazySet(0, 1L);
        check(a.get(0) == 1L, "lazySet then get");

        a.set(0, 5L);
        check(a.incrementAndGet(0) == 6L, "incrementAndGet(index)");
        check(a.getAndIncrement(0) == 6L && a.get(0) == 7L, "getAndIncrement(index) returns old");
        check(a.decrementAndGet(0) == 6L, "decrementAndGet(index)");
        check(a.getAndDecrement(0) == 6L && a.get(0) == 5L, "getAndDecrement(index) returns old");

        check(a.getAndAdd(0, 10L) == 5L && a.get(0) == 15L, "getAndAdd(index) returns old");
        check(a.addAndGet(0, -3L) == 12L, "addAndGet(index)");

        check(a.compareAndSet(0, 12L, 99L) && a.get(0) == 99L, "compareAndSet(index) success");
        check(!a.compareAndSet(0, 0L, 1L) && a.get(0) == 99L, "compareAndSet(index) failure returns false");

        check(!a.weakCompareAndSet(0, 0L, 1L) && a.get(0) == 99L, "weakCompareAndSet(index) failure");
        int spins = 0;
        while (!a.weakCompareAndSet(0, 99L, 100L) && spins < 100_000) spins++;
        check(a.get(0) == 100L, "weakCompareAndSet(index) eventually succeeds uncontended");
        check(a.weakCompareAndSetPlain(0, 100L, 101L) && a.get(0) == 101L, "weakCompareAndSetPlain(index) success");
        a.set(0, 100L);
        check(a.weakCompareAndSetVolatile(0, 100L, 102L) && a.get(0) == 102L, "weakCompareAndSetVolatile(index) success");
        a.set(0, 100L);
        check(a.weakCompareAndSetAcquire(0, 100L, 103L) && a.get(0) == 103L, "weakCompareAndSetAcquire(index) success");
        a.set(0, 100L);
        check(a.weakCompareAndSetRelease(0, 100L, 104L) && a.get(0) == 104L, "weakCompareAndSetRelease(index) success");
    }

    static void testUpdateAccessors() {
        AtomicLongArray a = new AtomicLongArray(2);
        a.set(0, 10L);
        check(a.getAndUpdate(0, x -> x + 1L) == 10L && a.get(0) == 11L, "getAndUpdate(index) returns old");
        check(a.updateAndGet(0, x -> x + 1L) == 12L, "updateAndGet(index) returns new");
        check(a.getAndAccumulate(0, 5L, (x, y) -> x + y) == 12L && a.get(0) == 17L, "getAndAccumulate(index)");
        check(a.accumulateAndGet(0, 3L, (x, y) -> x * y) == 51L, "accumulateAndGet(index)");

        a.setPlain(1, 42L);
        check(a.getPlain(1) == 42L, "setPlain/getPlain(index)");
        a.setOpaque(1, 43L);
        check(a.getOpaque(1) == 43L, "setOpaque/getOpaque(index)");
        a.setRelease(1, 44L);
        check(a.getAcquire(1) == 44L, "setRelease/getAcquire(index)");

        a.set(0, 5L);
        check(a.compareAndExchange(0, 5L, 7L) == 5L && a.get(0) == 7L, "compareAndExchange(index) success returns old");
        check(a.compareAndExchange(0, 5L, 9L) == 7L && a.get(0) == 7L, "compareAndExchange(index) failure returns actual");
    }

    static void testConstructionAndBounds() {
        long[] seed = {1L, 2L, 3L};
        AtomicLongArray a = new AtomicLongArray(seed);
        check(a.length() == 3, "length from long[] constructor");
        check(a.get(0) == 1L && a.get(1) == 2L && a.get(2) == 3L, "long[] constructor copies values");
        seed[0] = 99L;
        check(a.get(0) == 1L, "long[] constructor does not alias source array");

        check(new AtomicLongArray(new long[]{1L, 2L, 3L}).toString().equals("[1, 2, 3]"), "toString");
        check(new AtomicLongArray(0).toString().equals("[]"), "toString of empty array");

        assertThrows(IndexOutOfBoundsException.class, () -> a.get(-1), "get negative index throws IOOBE");
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(3), "get index == length throws IOOBE");
        assertThrows(IndexOutOfBoundsException.class, () -> a.set(3, 1L), "set index == length throws IOOBE");
        assertThrows(NegativeArraySizeException.class, () -> new AtomicLongArray(-1), "negative length throws NegativeArraySizeException");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 50_000;
        final AtomicLongArray a = new AtomicLongArray(4);
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int t = 0; t < N; t++) {
            ts[t] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) a.incrementAndGet(j & 3);
            });
            ts[t].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        for (int i = 0; i < 4; i++) {
            check(a.get(i) == N * M / 4, "concurrent per-index incrementAndGet loses no updates");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
