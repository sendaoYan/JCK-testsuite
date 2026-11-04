/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicLongFieldUpdater conformance: white-box structure
 *          plus black-box behavior of field updates (get/set/lazySet/compareAndSet/weakCompareAndSet,
 *          increment/decrement/add/getAndSet/getAndUpdate/updateAndGet/accumulate), the
 *          IllegalArgumentException/RuntimeException/ClassCastException contracts of newUpdater,
 *          and a concurrent incrementAndGet over a shared volatile long field.
 * @run main AtomicLongFieldUpdaterTest
 */

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class AtomicLongFieldUpdaterTest {
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

    static class Counter {
        volatile long value;
        volatile long other;
    }
    static class PlainField {
        long value;
    }
    static class IntField {
        volatile int value;
    }
    static class Foreign {
        volatile long value;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicLongFieldUpdater");
        check(!c.isInterface(), "AtomicLongFieldUpdater is a class, not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicLongFieldUpdater is abstract");
        check(c.getSuperclass() == Object.class, "AtomicLongFieldUpdater extends Object");
        check(!java.io.Serializable.class.isAssignableFrom(c), "AtomicLongFieldUpdater is not Serializable");
        c.getDeclaredConstructor();
        c.getMethod("newUpdater", Class.class, String.class);
        c.getMethod("compareAndSet", Object.class, long.class, long.class);
        c.getMethod("incrementAndGet", Object.class);
        c.getMethod("updateAndGet", Object.class, java.util.function.LongUnaryOperator.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testOperations();
        testExceptions();
        testConcurrent();
    }

    static void testOperations() {
        AtomicLongFieldUpdater<Counter> u = AtomicLongFieldUpdater.newUpdater(Counter.class, "value");
        Counter c = new Counter();
        check(u.get(c) == 0L, "initial value 0");

        u.set(c, 5L);
        check(u.get(c) == 5L, "set/get");

        check(u.compareAndSet(c, 5L, 7L), "compareAndSet success");
        check(u.get(c) == 7L, "compareAndSet applied");
        check(!u.compareAndSet(c, 5L, 9L) && u.get(c) == 7L, "compareAndSet failure returns false, unchanged");

        check(!u.weakCompareAndSet(c, 5L, 9L) && u.get(c) == 7L, "weakCompareAndSet failure returns false");
        int spins = 0;
        while (!u.weakCompareAndSet(c, 7L, 8L) && spins < 100_000) spins++;
        check(u.get(c) == 8L, "weakCompareAndSet eventually succeeds uncontended");

        check(u.getAndSet(c, 100L) == 8L && u.get(c) == 100L, "getAndSet returns old");

        check(u.getAndIncrement(c) == 100L && u.get(c) == 101L, "getAndIncrement returns old");
        check(u.incrementAndGet(c) == 102L, "incrementAndGet returns new");
        check(u.getAndDecrement(c) == 102L && u.get(c) == 101L, "getAndDecrement returns old");
        check(u.decrementAndGet(c) == 100L, "decrementAndGet returns new");

        check(u.getAndAdd(c, 10L) == 100L && u.get(c) == 110L, "getAndAdd returns old");
        check(u.addAndGet(c, -5L) == 105L, "addAndGet returns new");

        check(u.getAndUpdate(c, x -> x + 1L) == 105L && u.get(c) == 106L, "getAndUpdate returns old");
        check(u.updateAndGet(c, x -> x + 1L) == 107L, "updateAndGet returns new");
        check(u.getAndAccumulate(c, 5L, (a, b) -> a + b) == 107L && u.get(c) == 112L, "getAndAccumulate returns old");
        check(u.accumulateAndGet(c, 3L, (a, b) -> a * b) == 336L, "accumulateAndGet returns new");

        u.lazySet(c, 42L);
        check(u.get(c) == 42L, "lazySet then get");

        AtomicLongFieldUpdater<Counter> u2 = AtomicLongFieldUpdater.newUpdater(Counter.class, "other");
        u2.set(c, 9L);
        check(u2.get(c) == 9L && u.get(c) == 42L, "two updaters over different fields are independent");
    }

    static void testExceptions() {
        assertThrows(IllegalArgumentException.class,
            () -> AtomicLongFieldUpdater.newUpdater(PlainField.class, "value"),
            "non-volatile field throws IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
            () -> AtomicLongFieldUpdater.newUpdater(IntField.class, "value"),
            "int field throws IllegalArgumentException (must be long type)");
        assertThrows(RuntimeException.class,
            () -> AtomicLongFieldUpdater.newUpdater(Counter.class, "nope"),
            "unknown field throws RuntimeException");

        AtomicLongFieldUpdater<Counter> u = AtomicLongFieldUpdater.newUpdater(Counter.class, "value");
        @SuppressWarnings("rawtypes")
        AtomicLongFieldUpdater raw = u;
        Foreign f = new Foreign();
        assertThrows(ClassCastException.class, () -> raw.get(f), "get on wrong instance type throws ClassCastException");
        assertThrows(ClassCastException.class, () -> raw.compareAndSet(f, 0L, 1L), "compareAndSet on wrong instance type throws ClassCastException");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 100_000;
        final AtomicLongFieldUpdater<Counter> u = AtomicLongFieldUpdater.newUpdater(Counter.class, "value");
        final Counter c = new Counter();
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) u.incrementAndGet(c);
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        check(u.get(c) == N * M, "concurrent incrementAndGet over volatile long field loses no updates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
