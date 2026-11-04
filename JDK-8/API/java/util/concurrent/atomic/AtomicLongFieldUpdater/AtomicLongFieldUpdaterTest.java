/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicLongFieldUpdater conformance: field updates.
 * @run main AtomicLongFieldUpdaterTest
 */

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class AtomicLongFieldUpdaterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Counter {
        public volatile long value;
    }

    static void testUpdater() {
        AtomicLongFieldUpdater<Counter> u = AtomicLongFieldUpdater.newUpdater(Counter.class, "value");
        Counter c = new Counter();
        check(u.get(c) == 0L, "initial value 0");
        u.set(c, 5L);
        check(u.get(c) == 5L, "set/get");
        check(u.compareAndSet(c, 5L, 7L), "compareAndSet true");
        check(u.get(c) == 7L, "compareAndSet applied");
        check(!u.compareAndSet(c, 5L, 9L), "compareAndSet false");
        check(u.weakCompareAndSet(c, 7L, 8L), "weakCompareAndSet");
        check(u.getAndIncrement(c) == 8L, "getAndIncrement returns old");
        check(u.get(c) == 9L, "incremented");
        check(u.incrementAndGet(c) == 10L, "incrementAndGet");
        check(u.getAndDecrement(c) == 10L, "getAndDecrement returns old");
        check(u.decrementAndGet(c) == 8L, "decrementAndGet");
        check(u.getAndAdd(c, 10L) == 8L, "getAndAdd returns old");
        check(u.addAndGet(c, 1L) == 19L, "addAndGet");
        check(u.getAndSet(c, 100L) == 19L, "getAndSet returns old");
        check(u.get(c) == 100L, "getAndSet applied");
        check(u.getAndUpdate(c, x -> x + 1L) == 100L, "getAndUpdate");
        check(u.get(c) == 101L, "after getAndUpdate");
        check(u.updateAndGet(c, x -> x + 1L) == 102L, "updateAndGet");
        check(u.getAndAccumulate(c, 5L, (a, b) -> a + b) == 102L, "getAndAccumulate");
        check(u.accumulateAndGet(c, 5L, (a, b) -> a + b) == 112L, "accumulateAndGet");
        u.lazySet(c, 42L);
        check(u.get(c) == 42L, "lazySet");
    }

    // ---- 契约深化：newUpdater 反射前置校验矩阵 / 多对象隔离 ----
    static class BadFields {
        public long nonVolatile;              // 非 volatile
        public volatile int intField;         // 类型不匹配
        public static volatile long staticField; // static 字段
        private volatile long privateField;   // 不可见（private）
        public volatile long okField;
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

    static void testNewUpdaterValidation() {
        // JDK-8 实测：非 volatile → IAE("Must be volatile type")
        checkMessage(() -> AtomicLongFieldUpdater.newUpdater(BadFields.class, "nonVolatile"),
                IllegalArgumentException.class, "Must be volatile type", "non-volatile field");
        // 类型不匹配（int 字段）→ IAE("Must be long type")
        checkMessage(() -> AtomicLongFieldUpdater.newUpdater(BadFields.class, "intField"),
                IllegalArgumentException.class, "Must be long type", "int field with long updater");
        // static 字段 → IAE（JDK-8 消息为 null，JDK-9+ 为 "Must not be a static field"）
        assertThrows(IllegalArgumentException.class,
                () -> AtomicLongFieldUpdater.newUpdater(BadFields.class, "staticField"), "static field rejected");
        // private 字段 → RuntimeException（包装 IllegalAccessException）
        // JDK-8 无 nestmate：同文件嵌套类 private 也拒绝
        assertThrows(RuntimeException.class,
                () -> AtomicLongFieldUpdater.newUpdater(BadFields.class, "privateField"), "private field rejected");
        // 跨类 private（java.base）→ RuntimeException
        assertThrows(RuntimeException.class,
                () -> AtomicLongFieldUpdater.newUpdater(java.util.concurrent.atomic.AtomicLong.class, "value"),
                "foreign private field rejected");
        // 不存在的字段 → RuntimeException（包装 NoSuchFieldException）
        assertThrows(RuntimeException.class,
                () -> AtomicLongFieldUpdater.newUpdater(BadFields.class, "noSuchField"), "unknown field rejected");
        // null 字段名 / null 类 → RuntimeException（包装 NPE）
        assertThrows(RuntimeException.class,
                () -> AtomicLongFieldUpdater.newUpdater(Counter.class, null), "null field name rejected");
        assertThrows(RuntimeException.class,
                () -> AtomicLongFieldUpdater.newUpdater(null, "value"), "null class rejected");
    }

    static void testMultiObjectIndependence() {
        AtomicLongFieldUpdater<Counter> u = AtomicLongFieldUpdater.newUpdater(Counter.class, "value");
        Counter c1 = new Counter();
        Counter c2 = new Counter();
        u.set(c1, 11L);
        u.set(c2, 22L);
        check(u.get(c1) == 11L && u.get(c2) == 22L, "two objects hold independent values");
        check(u.compareAndSet(c1, 11L, 33L) && u.get(c1) == 33L, "CAS on c1");
        check(u.get(c2) == 22L, "CAS on c1 leaves c2 untouched");
        check(!u.compareAndSet(c2, 11L, 0L) && u.get(c2) == 22L, "failed CAS on c2 leaves it unchanged");
        u.addAndGet(c1, 1L);
        check(u.get(c1) == 34L && u.get(c2) == 22L, "per-object adds independent");
        // 64 位边界
        u.set(c1, Long.MAX_VALUE);
        check(u.incrementAndGet(c1) == Long.MIN_VALUE, "updater incrementAndGet wraps MAX to MIN");
    }

    public static void main(String[] args) {
        testUpdater();
        testNewUpdaterValidation();
        testMultiObjectIndependence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
