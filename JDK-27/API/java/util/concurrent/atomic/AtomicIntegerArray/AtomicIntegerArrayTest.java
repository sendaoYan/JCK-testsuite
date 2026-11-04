/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicIntegerArray conformance: white-box structure plus
 *          black-box behavior of indexed get/set/lazySet/getAndSet/compareAndSet/weakCompareAndSet,
 *          increment/decrement/add, update/accumulate, toString, ordering accessors,
 *          compareAndExchange, copy-on-construct, index bounds, and concurrent per-index updates.
 * @run main AtomicIntegerArrayTest
 */

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicIntegerArrayTest {
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
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicIntegerArray");
        check(!c.isInterface(), "AtomicIntegerArray is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicIntegerArray is concrete");
        check(c.getSuperclass() == Object.class, "AtomicIntegerArray extends Object");
        check(Serializable.class.isAssignableFrom(c), "AtomicIntegerArray implements Serializable");
        c.getConstructor(int.class);
        c.getConstructor(int[].class);
        c.getMethod("length");
        c.getMethod("get", int.class);
        c.getMethod("compareAndSet", int.class, int.class, int.class);
        c.getMethod("addAndGet", int.class, int.class);
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
        AtomicIntegerArray a = new AtomicIntegerArray(3);
        check(a.length() == 3, "length");
        check(a.get(0) == 0, "initial 0");
        check(a.get(1) == 0, "initial 0 (index 1)");

        a.set(0, 5);
        check(a.get(0) == 5, "set/get");

        check(a.getAndSet(0, 7) == 5 && a.get(0) == 7, "getAndSet returns old");

        a.lazySet(0, 1);
        check(a.get(0) == 1, "lazySet then get");

        a.set(0, 5);
        check(a.incrementAndGet(0) == 6, "incrementAndGet(index)");
        check(a.getAndIncrement(0) == 6 && a.get(0) == 7, "getAndIncrement(index) returns old");
        check(a.decrementAndGet(0) == 6, "decrementAndGet(index)");
        check(a.getAndDecrement(0) == 6 && a.get(0) == 5, "getAndDecrement(index) returns old");

        check(a.getAndAdd(0, 10) == 5 && a.get(0) == 15, "getAndAdd(index) returns old");
        check(a.addAndGet(0, -3) == 12, "addAndGet(index)");

        check(a.compareAndSet(0, 12, 99) && a.get(0) == 99, "compareAndSet(index) success");
        check(!a.compareAndSet(0, 0, 1) && a.get(0) == 99, "compareAndSet(index) failure returns false");

        check(!a.weakCompareAndSet(0, 0, 1) && a.get(0) == 99, "weakCompareAndSet(index) failure");
        int spins = 0;
        while (!a.weakCompareAndSet(0, 99, 100) && spins < 100_000) spins++;
        check(a.get(0) == 100, "weakCompareAndSet(index) eventually succeeds uncontended");
        check(a.weakCompareAndSetPlain(0, 100, 101) && a.get(0) == 101, "weakCompareAndSetPlain(index) success");
        a.set(0, 100);
        check(a.weakCompareAndSetVolatile(0, 100, 102) && a.get(0) == 102, "weakCompareAndSetVolatile(index) success");
        a.set(0, 100);
        check(a.weakCompareAndSetAcquire(0, 100, 103) && a.get(0) == 103, "weakCompareAndSetAcquire(index) success");
        a.set(0, 100);
        check(a.weakCompareAndSetRelease(0, 100, 104) && a.get(0) == 104, "weakCompareAndSetRelease(index) success");
    }

    static void testUpdateAccessors() {
        AtomicIntegerArray a = new AtomicIntegerArray(2);
        a.set(0, 10);
        check(a.getAndUpdate(0, x -> x + 1) == 10 && a.get(0) == 11, "getAndUpdate(index) returns old");
        check(a.updateAndGet(0, x -> x + 1) == 12, "updateAndGet(index) returns new");
        check(a.getAndAccumulate(0, 5, (x, y) -> x + y) == 12 && a.get(0) == 17, "getAndAccumulate(index)");
        check(a.accumulateAndGet(0, 3, (x, y) -> x * y) == 51, "accumulateAndGet(index)");

        // ordering accessors
        a.setPlain(1, 42);
        check(a.getPlain(1) == 42, "setPlain/getPlain(index)");
        a.setOpaque(1, 43);
        check(a.getOpaque(1) == 43, "setOpaque/getOpaque(index)");
        a.setRelease(1, 44);
        check(a.getAcquire(1) == 44, "setRelease/getAcquire(index)");

        // compareAndExchange(index) returns witness value
        a.set(0, 5);
        check(a.compareAndExchange(0, 5, 7) == 5 && a.get(0) == 7, "compareAndExchange(index) success returns old");
        check(a.compareAndExchange(0, 5, 9) == 7 && a.get(0) == 7, "compareAndExchange(index) failure returns actual");
    }

    static void testConstructionAndBounds() {
        // copy-on-construct from an int[] array
        int[] seed = {1, 2, 3};
        AtomicIntegerArray a = new AtomicIntegerArray(seed);
        check(a.length() == 3, "length from int[] constructor");
        check(a.get(0) == 1 && a.get(1) == 2 && a.get(2) == 3, "int[] constructor copies values");
        seed[0] = 99;
        check(a.get(0) == 1, "int[] constructor does not alias source array");

        check(new AtomicIntegerArray(new int[]{1, 2, 3}).toString().equals("[1, 2, 3]"), "toString");
        check(new AtomicIntegerArray(0).toString().equals("[]"), "toString of empty array");

        // index bounds
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(-1), "get negative index throws IOOBE");
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(3), "get index == length throws IOOBE");
        assertThrows(IndexOutOfBoundsException.class, () -> a.set(3, 1), "set index == length throws IOOBE");
        assertThrows(NegativeArraySizeException.class, () -> new AtomicIntegerArray(-1), "negative length throws NegativeArraySizeException");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 50_000;
        final AtomicIntegerArray a = new AtomicIntegerArray(4);
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
