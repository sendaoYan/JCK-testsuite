/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicIntegerFieldUpdater conformance: white-box structure
 *          plus black-box behavior of field updates (get/set/lazySet/compareAndSet/weakCompareAndSet,
 *          increment/decrement/add/getAndSet/getAndUpdate/updateAndGet/accumulate), the
 *          IllegalArgumentException/RuntimeException/ClassCastException contracts of newUpdater,
 *          and a concurrent incrementAndGet over a shared volatile int field.
 * @run main AtomicIntegerFieldUpdaterTest
 */

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class AtomicIntegerFieldUpdaterTest {
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
        volatile int value;
        volatile int other;
    }
    static class PlainField {
        int value;
    }
    static class LongField {
        volatile long value;
    }
    static class Foreign {
        volatile int value;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicIntegerFieldUpdater");
        check(!c.isInterface(), "AtomicIntegerFieldUpdater is a class, not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicIntegerFieldUpdater is abstract");
        check(c.getSuperclass() == Object.class, "AtomicIntegerFieldUpdater extends Object");
        check(!java.io.Serializable.class.isAssignableFrom(c), "AtomicIntegerFieldUpdater is not Serializable");
        c.getDeclaredConstructor(); // protected no-arg constructor present
        c.getMethod("newUpdater", Class.class, String.class);
        c.getMethod("compareAndSet", Object.class, int.class, int.class);
        c.getMethod("incrementAndGet", Object.class);
        c.getMethod("updateAndGet", Object.class, java.util.function.IntUnaryOperator.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testOperations();
        testExceptions();
        testConcurrent();
    }

    static void testOperations() {
        AtomicIntegerFieldUpdater<Counter> u = AtomicIntegerFieldUpdater.newUpdater(Counter.class, "value");
        Counter c = new Counter();
        check(u.get(c) == 0, "initial value 0");

        u.set(c, 5);
        check(u.get(c) == 5, "set/get");

        check(u.compareAndSet(c, 5, 7), "compareAndSet success");
        check(u.get(c) == 7, "compareAndSet applied");
        check(!u.compareAndSet(c, 5, 9) && u.get(c) == 7, "compareAndSet failure returns false, unchanged");

        check(!u.weakCompareAndSet(c, 5, 9) && u.get(c) == 7, "weakCompareAndSet failure returns false");
        int spins = 0;
        while (!u.weakCompareAndSet(c, 7, 8) && spins < 100_000) spins++;
        check(u.get(c) == 8, "weakCompareAndSet eventually succeeds uncontended");

        check(u.getAndSet(c, 100) == 8 && u.get(c) == 100, "getAndSet returns old");

        check(u.getAndIncrement(c) == 100 && u.get(c) == 101, "getAndIncrement returns old");
        check(u.incrementAndGet(c) == 102, "incrementAndGet returns new");
        check(u.getAndDecrement(c) == 102 && u.get(c) == 101, "getAndDecrement returns old");
        check(u.decrementAndGet(c) == 100, "decrementAndGet returns new");

        check(u.getAndAdd(c, 10) == 100 && u.get(c) == 110, "getAndAdd returns old");
        check(u.addAndGet(c, -5) == 105, "addAndGet returns new");

        check(u.getAndUpdate(c, x -> x + 1) == 105 && u.get(c) == 106, "getAndUpdate returns old");
        check(u.updateAndGet(c, x -> x + 1) == 107, "updateAndGet returns new");
        check(u.getAndAccumulate(c, 5, (a, b) -> a + b) == 107 && u.get(c) == 112, "getAndAccumulate returns old");
        check(u.accumulateAndGet(c, 3, (a, b) -> a * b) == 336, "accumulateAndGet returns new");

        u.lazySet(c, 42);
        check(u.get(c) == 42, "lazySet then get");

        // a second updater on another field of the same object is independent
        AtomicIntegerFieldUpdater<Counter> u2 = AtomicIntegerFieldUpdater.newUpdater(Counter.class, "other");
        u2.set(c, 9);
        check(u2.get(c) == 9 && u.get(c) == 42, "two updaters over different fields are independent");
    }

    static void testExceptions() {
        // non-volatile int field -> IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
            () -> AtomicIntegerFieldUpdater.newUpdater(PlainField.class, "value"),
            "non-volatile field throws IllegalArgumentException");
        // long field -> IllegalArgumentException (must be integer type)
        assertThrows(IllegalArgumentException.class,
            () -> AtomicIntegerFieldUpdater.newUpdater(LongField.class, "value"),
            "long field throws IllegalArgumentException (must be integer type)");
        // unknown field -> RuntimeException (nested NoSuchFieldException)
        assertThrows(RuntimeException.class,
            () -> AtomicIntegerFieldUpdater.newUpdater(Counter.class, "nope"),
            "unknown field throws RuntimeException");

        // wrong instance type -> ClassCastException
        AtomicIntegerFieldUpdater<Counter> u = AtomicIntegerFieldUpdater.newUpdater(Counter.class, "value");
        @SuppressWarnings("rawtypes")
        AtomicIntegerFieldUpdater raw = u;
        Foreign f = new Foreign();
        assertThrows(ClassCastException.class, () -> raw.get(f), "get on wrong instance type throws ClassCastException");
        assertThrows(ClassCastException.class, () -> raw.compareAndSet(f, 0, 1), "compareAndSet on wrong instance type throws ClassCastException");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 100_000;
        final AtomicIntegerFieldUpdater<Counter> u = AtomicIntegerFieldUpdater.newUpdater(Counter.class, "value");
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
        check(u.get(c) == N * M, "concurrent incrementAndGet over volatile field loses no updates");
    }

    // ---- 契约深化：消息精确断言 / static·private·null 校验 / 多对象隔离 ----
    static class StaticField {
        static volatile int sv;
    }
    static class PrivateField {
        private volatile int pv;
    }

    static void checkMessage(Runnable r, Class<? extends Throwable> exType, String expect, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (!exType.isInstance(t)) throw new AssertionError(msg + " (threw " + t + ")");
            check(expect.equals(t.getMessage()), msg + " message <" + t.getMessage() + "> equals <" + expect + ">");
            return;
        }
        throw new AssertionError(msg + " (no exception)");
    }

    static void testValidationMessages() {
        // JDK-27 实测：消息精确断言
        checkMessage(() -> AtomicIntegerFieldUpdater.newUpdater(PlainField.class, "value"),
                IllegalArgumentException.class, "Must be volatile type", "non-volatile field message");
        checkMessage(() -> AtomicIntegerFieldUpdater.newUpdater(LongField.class, "value"),
                IllegalArgumentException.class, "Must be integer type", "long field message");
        // static 字段 → IAE（JDK-27 实测消息 "Must not be a static field"；JDK-8 消息为 null）
        checkMessage(() -> AtomicIntegerFieldUpdater.newUpdater(StaticField.class, "sv"),
                IllegalArgumentException.class, "Must not be a static field", "static field message");
        // private 字段（跨类不可见）→ RuntimeException（包装 IllegalAccessException）
        // 注：JDK-11+ 同文件嵌套类是 nestmate，可访问；用 java.base 的 private volatile int 才不可达
        assertThrows(RuntimeException.class,
                () -> AtomicIntegerFieldUpdater.newUpdater(java.util.concurrent.atomic.AtomicInteger.class, "value"),
                "foreign private field rejected");
        // null 字段名 / null 类 → RuntimeException（包装 NPE）
        assertThrows(RuntimeException.class,
                () -> AtomicIntegerFieldUpdater.newUpdater(Counter.class, null), "null field name rejected");
        assertThrows(RuntimeException.class,
                () -> AtomicIntegerFieldUpdater.newUpdater(null, "value"), "null class rejected");
    }

    static void testMultiObjectIndependence() {
        AtomicIntegerFieldUpdater<Counter> u = AtomicIntegerFieldUpdater.newUpdater(Counter.class, "value");
        Counter c1 = new Counter();
        Counter c2 = new Counter();
        u.set(c1, 11);
        u.set(c2, 22);
        check(u.get(c1) == 11 && u.get(c2) == 22, "two objects hold independent values");
        check(u.compareAndSet(c1, 11, 33) && u.get(c1) == 33, "CAS on c1");
        check(u.get(c2) == 22, "CAS on c1 leaves c2 untouched");
        check(!u.compareAndSet(c2, 11, 0) && u.get(c2) == 22, "failed CAS on c2 leaves it unchanged");
        u.incrementAndGet(c2);
        check(u.get(c2) == 23 && u.get(c1) == 33, "per-object increments independent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testValidationMessages();
        testMultiObjectIndependence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
