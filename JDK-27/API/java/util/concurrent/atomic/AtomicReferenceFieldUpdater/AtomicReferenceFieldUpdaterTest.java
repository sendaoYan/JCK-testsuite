/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicReferenceFieldUpdater conformance: white-box structure
 *          plus black-box behavior of field updates (get/set/lazySet/compareAndSet/weakCompareAndSet,
 *          getAndSet/getAndUpdate/updateAndGet/accumulate), the IllegalArgumentException/
 *          ClassCastException/RuntimeException contracts of newUpdater, and a concurrent
 *          updateAndGet over a shared volatile reference field.
 * @run main AtomicReferenceFieldUpdaterTest
 */

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class AtomicReferenceFieldUpdaterTest {
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

    static class Holder {
        volatile String value;
        volatile String other;
    }
    static class IntCounter {
        volatile Integer value;
    }
    static class PlainField {
        String value;
    }
    static class PrimitiveField {
        volatile int value;
    }
    static class Foreign {
        volatile String value;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicReferenceFieldUpdater");
        check(!c.isInterface(), "AtomicReferenceFieldUpdater is a class, not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicReferenceFieldUpdater is abstract");
        check(c.getSuperclass() == Object.class, "AtomicReferenceFieldUpdater extends Object");
        check(!java.io.Serializable.class.isAssignableFrom(c), "AtomicReferenceFieldUpdater is not Serializable");
        c.getDeclaredConstructor();
        c.getMethod("newUpdater", Class.class, Class.class, String.class);
        c.getMethod("compareAndSet", Object.class, Object.class, Object.class);
        c.getMethod("updateAndGet", Object.class, java.util.function.UnaryOperator.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testOperations();
        testExceptions();
        testConcurrent();
    }

    static void testOperations() {
        AtomicReferenceFieldUpdater<Holder, String> u =
            AtomicReferenceFieldUpdater.newUpdater(Holder.class, String.class, "value");
        Holder h = new Holder();
        check(u.get(h) == null, "initial value null");

        u.set(h, "a");
        check("a".equals(u.get(h)), "set/get");

        check(u.compareAndSet(h, "a", "b"), "compareAndSet success");
        check("b".equals(u.get(h)), "compareAndSet applied");
        check(!u.compareAndSet(h, "a", "c") && "b".equals(u.get(h)), "compareAndSet failure returns false, unchanged");

        check(!u.weakCompareAndSet(h, "a", "c") && "b".equals(u.get(h)), "weakCompareAndSet failure returns false");
        int spins = 0;
        while (!u.weakCompareAndSet(h, "b", "c") && spins < 100_000) spins++;
        check("c".equals(u.get(h)), "weakCompareAndSet eventually succeeds uncontended");

        check("c".equals(u.getAndSet(h, "d")) && "d".equals(u.get(h)), "getAndSet returns old");

        check("d".equals(u.getAndUpdate(h, s -> s + "1")) && "d1".equals(u.get(h)), "getAndUpdate returns old");
        check("d11".equals(u.updateAndGet(h, s -> s + "1")), "updateAndGet returns new");
        check("d11".equals(u.getAndAccumulate(h, "x", (s, t) -> s + t)) && "d11x".equals(u.get(h)), "getAndAccumulate returns old");
        check("d11xy".equals(u.accumulateAndGet(h, "y", (s, t) -> s + t)), "accumulateAndGet returns new");

        u.lazySet(h, "z");
        check("z".equals(u.get(h)), "lazySet then get");

        AtomicReferenceFieldUpdater<Holder, String> u2 =
            AtomicReferenceFieldUpdater.newUpdater(Holder.class, String.class, "other");
        u2.set(h, "o");
        check("o".equals(u2.get(h)) && "z".equals(u.get(h)), "two updaters over different fields are independent");
    }

    static void testExceptions() {
        // non-volatile field -> IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
            () -> AtomicReferenceFieldUpdater.newUpdater(PlainField.class, String.class, "value"),
            "non-volatile field throws IllegalArgumentException");
        // primitive field (int) -> IllegalArgumentException (must be reference type)
        assertThrows(IllegalArgumentException.class,
            () -> AtomicReferenceFieldUpdater.newUpdater(PrimitiveField.class, int.class, "value"),
            "primitive field throws IllegalArgumentException (must be reference type)");
        // reference type mismatch -> ClassCastException
        assertThrows(ClassCastException.class,
            () -> AtomicReferenceFieldUpdater.newUpdater(Holder.class, Integer.class, "value"),
            "mismatched reference type throws ClassCastException");
        // unknown field -> RuntimeException
        assertThrows(RuntimeException.class,
            () -> AtomicReferenceFieldUpdater.newUpdater(Holder.class, String.class, "nope"),
            "unknown field throws RuntimeException");

        AtomicReferenceFieldUpdater<Holder, String> u =
            AtomicReferenceFieldUpdater.newUpdater(Holder.class, String.class, "value");
        @SuppressWarnings("rawtypes")
        AtomicReferenceFieldUpdater raw = u;
        Foreign f = new Foreign();
        assertThrows(ClassCastException.class, () -> raw.get(f), "get on wrong instance type throws ClassCastException");
        assertThrows(ClassCastException.class, () -> raw.compareAndSet(f, "a", "b"), "compareAndSet on wrong instance type throws ClassCastException");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 100_000;
        final AtomicReferenceFieldUpdater<IntCounter, Integer> u =
            AtomicReferenceFieldUpdater.newUpdater(IntCounter.class, Integer.class, "value");
        final IntCounter c = new IntCounter();
        c.value = 0;
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) u.updateAndGet(c, x -> x + 1);
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        check(u.get(c) == N * M, "concurrent updateAndGet over volatile reference field loses no updates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
