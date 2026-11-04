/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicIntegerFieldUpdater conformance: field updates.
 * @run main AtomicIntegerFieldUpdaterTest
 */

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class AtomicIntegerFieldUpdaterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Counter {
        public volatile int value;
    }

    static void testUpdater() {
        AtomicIntegerFieldUpdater<Counter> u = AtomicIntegerFieldUpdater.newUpdater(Counter.class, "value");
        Counter c = new Counter();
        check(u.get(c) == 0, "initial value 0");
        u.set(c, 5);
        check(u.get(c) == 5, "set/get");
        check(u.compareAndSet(c, 5, 7), "compareAndSet true");
        check(u.get(c) == 7, "compareAndSet applied");
        check(!u.compareAndSet(c, 5, 9), "compareAndSet false");
        check(u.weakCompareAndSet(c, 7, 8), "weakCompareAndSet");
        check(u.getAndIncrement(c) == 8, "getAndIncrement returns old");
        check(u.get(c) == 9, "incremented");
        check(u.incrementAndGet(c) == 10, "incrementAndGet");
        check(u.getAndDecrement(c) == 10, "getAndDecrement returns old");
        check(u.decrementAndGet(c) == 8, "decrementAndGet");
        check(u.getAndAdd(c, 10) == 8, "getAndAdd returns old");
        check(u.addAndGet(c, 1) == 19, "addAndGet");
        check(u.getAndSet(c, 100) == 19, "getAndSet returns old");
        check(u.get(c) == 100, "getAndSet applied");
        check(u.getAndUpdate(c, x -> x + 1) == 100, "getAndUpdate");
        check(u.get(c) == 101, "after getAndUpdate");
        check(u.updateAndGet(c, x -> x + 1) == 102, "updateAndGet");
        check(u.getAndAccumulate(c, 5, (a, b) -> a + b) == 102, "getAndAccumulate");
        check(u.accumulateAndGet(c, 5, (a, b) -> a + b) == 112, "accumulateAndGet");
        u.lazySet(c, 42);
        check(u.get(c) == 42, "lazySet");
    }

    // ---- 契约深化：newUpdater 反射前置校验矩阵 / 多对象隔离 / CAS 两分支 ----
    static class BadFields {
        public int nonVolatile;          // 非 volatile
        public volatile long longField;  // 类型不匹配
        public static volatile int staticField; // static 字段
        private volatile int privateField;      // 不可见（private）
        public volatile int okField;
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
        checkMessage(() -> AtomicIntegerFieldUpdater.newUpdater(BadFields.class, "nonVolatile"),
                IllegalArgumentException.class, "Must be volatile type", "non-volatile field");
        // 类型不匹配（long 字段）→ IAE("Must be integer type")
        checkMessage(() -> AtomicIntegerFieldUpdater.newUpdater(BadFields.class, "longField"),
                IllegalArgumentException.class, "Must be integer type", "long field with int updater");
        // static 字段 → IAE（JDK-8 消息为 null，JDK-9+ 为 "Must not be a static field"）
        assertThrows(IllegalArgumentException.class,
                () -> AtomicIntegerFieldUpdater.newUpdater(BadFields.class, "staticField"), "static field rejected");
        // private 字段（不可见）→ RuntimeException（包装 IllegalAccessException）
        // JDK-8 无 nestmate：同文件嵌套类 private 也拒绝
        assertThrows(RuntimeException.class,
                () -> AtomicIntegerFieldUpdater.newUpdater(BadFields.class, "privateField"), "private field rejected");
        // 跨类 private（java.base）→ RuntimeException
        assertThrows(RuntimeException.class,
                () -> AtomicIntegerFieldUpdater.newUpdater(java.util.concurrent.atomic.AtomicInteger.class, "value"),
                "foreign private field rejected");
        // 不存在的字段 → RuntimeException（包装 NoSuchFieldException）
        assertThrows(RuntimeException.class,
                () -> AtomicIntegerFieldUpdater.newUpdater(BadFields.class, "noSuchField"), "unknown field rejected");
        // null 字段名 → RuntimeException（包装 NPE）
        assertThrows(RuntimeException.class,
                () -> AtomicIntegerFieldUpdater.newUpdater(Counter.class, null), "null field name rejected");
        // null 类 → RuntimeException（包装 NPE）
        assertThrows(RuntimeException.class,
                () -> AtomicIntegerFieldUpdater.newUpdater(null, "value"), "null class rejected");
    }

    static void testMultiObjectIndependence() {
        // 多对象互不干扰：同一 updater 作用于不同 POJO 实例
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

    public static void main(String[] args) {
        testUpdater();
        testNewUpdaterValidation();
        testMultiObjectIndependence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
