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

    // ---- 契约深化：消息精确断言 / static·private·null 校验 / identity 语义 / 多对象隔离 ----
    static class StaticField {
        static volatile String sv;
    }
    static class PrivateField {
        private volatile String pv;
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
        checkMessage(() -> AtomicReferenceFieldUpdater.newUpdater(PlainField.class, String.class, "value"),
                IllegalArgumentException.class, "Must be volatile type", "non-volatile field message");
        // static 字段 → IAE（JDK-27 实测消息 "Must not be a static field"；JDK-8 消息为 null）
        checkMessage(() -> AtomicReferenceFieldUpdater.newUpdater(StaticField.class, String.class, "sv"),
                IllegalArgumentException.class, "Must not be a static field", "static field message");
        // private 字段（跨类不可见）→ RuntimeException（包装 IllegalAccessException）
        // 注：JDK-11+ 同文件嵌套类是 nestmate，可访问；用 java.base 的 private volatile String 才不可达
        assertThrows(RuntimeException.class,
                () -> AtomicReferenceFieldUpdater.newUpdater(Thread.class, String.class, "name"),
                "foreign private field rejected");
        assertThrows(RuntimeException.class,
                () -> AtomicReferenceFieldUpdater.newUpdater(Holder.class, String.class, null),
                "null field name rejected");
        assertThrows(RuntimeException.class,
                () -> AtomicReferenceFieldUpdater.newUpdater(null, String.class, "value"),
                "null class rejected");
    }

    static void testIdentitySemantics() {
        // CAS 按 ==（引用同一性）匹配
        String a1 = new String("a");
        String a2 = new String("a");
        check(a1.equals(a2) && a1 != a2, "precondition: equals but not same reference");
        AtomicReferenceFieldUpdater<Holder, String> u =
            AtomicReferenceFieldUpdater.newUpdater(Holder.class, String.class, "value");
        Holder h = new Holder();
        u.set(h, a1);
        check(!u.compareAndSet(h, a2, "z"), "CAS with equals-but-not-same expected fails");
        check(u.get(h) == a1, "failed identity CAS leaves same reference");
        check(u.compareAndSet(h, a1, "b"), "CAS with identical (==) expected succeeds");
        // null 期望值
        Holder h2 = new Holder();
        check(u.compareAndSet(h2, null, "n"), "CAS(null, v) on null field succeeds");
    }

    static void testMultiObjectIndependence() {
        AtomicReferenceFieldUpdater<Holder, String> u =
            AtomicReferenceFieldUpdater.newUpdater(Holder.class, String.class, "value");
        Holder h1 = new Holder();
        Holder h2 = new Holder();
        u.set(h1, "o1");
        u.set(h2, "o2");
        check("o1".equals(u.get(h1)) && "o2".equals(u.get(h2)), "two objects hold independent values");
        check(u.compareAndSet(h1, "o1", "n1"), "CAS on h1");
        check("o2".equals(u.get(h2)), "CAS on h1 leaves h2 untouched");
        check(!u.compareAndSet(h2, "o1", "x") && "o2".equals(u.get(h2)), "failed CAS on h2 leaves it unchanged");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testValidationMessages();
        testIdentitySemantics();
        testMultiObjectIndependence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
