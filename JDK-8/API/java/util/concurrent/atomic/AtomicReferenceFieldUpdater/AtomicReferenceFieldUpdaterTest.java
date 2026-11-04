/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicReferenceFieldUpdater API conformance.
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

    static class Holder {
        volatile String value;
    }

    static void testAtomicReferenceFieldUpdater() {
        AtomicReferenceFieldUpdater<Holder, String> updater =
            AtomicReferenceFieldUpdater.newUpdater(Holder.class, String.class, "value");
        Holder h = new Holder();
        updater.set(h, "a");
        check("a".equals(updater.get(h)), "get after set");
        check(updater.compareAndSet(h, "a", "b"), "compareAndSet succeeds");
        check("b".equals(updater.get(h)), "value updated");
        check(!updater.compareAndSet(h, "a", "c"), "compareAndSet fails with stale expected");
    }

    // ---- 契约深化：全操作族 / newUpdater 校验矩阵 / identity 语义 / 多对象隔离 ----
    static void testOperationFamily() {
        AtomicReferenceFieldUpdater<Holder, String> u =
            AtomicReferenceFieldUpdater.newUpdater(Holder.class, String.class, "value");
        Holder h = new Holder();
        check(u.get(h) == null, "initial field null");
        u.set(h, "s0");
        check("s0".equals(u.getAndSet(h, "s1")) && "s1".equals(u.get(h)), "getAndSet returns old");
        check(!u.weakCompareAndSet(h, "s0", "x") && "s1".equals(u.get(h)), "weakCAS failure returns false");
        int spins = 0;
        while (!u.weakCompareAndSet(h, "s1", "s2") && spins < 100_000) spins++;
        check("s2".equals(u.get(h)), "weakCAS success eventually applies uncontended");
        // update / accumulate 族
        java.util.List<String> seen = new java.util.ArrayList<>();
        check("s2".equals(u.getAndUpdate(h, s -> { seen.add(s); return s + "1"; })),
                "getAndUpdate returns old");
        check(seen.size() == 1 && "s2".equals(seen.get(0)), "getAndUpdate function receives old value");
        check("s211".equals(u.updateAndGet(h, s -> s + "1")), "updateAndGet returns new");
        check("s211".equals(u.getAndAccumulate(h, "x", (s, t) -> s + t)) && "s211x".equals(u.get(h)),
                "getAndAccumulate returns old, stores combined");
        check("s211xy".equals(u.accumulateAndGet(h, "y", (s, t) -> s + t)), "accumulateAndGet returns new");
        u.lazySet(h, "lz");
        check("lz".equals(u.get(h)), "lazySet then get");
    }

    static class BadFields {
        String nonVolatile;                        // 非 volatile
        volatile int intField;                     // 原始类型字段
        volatile Integer boxedMismatch;            // 引用类型不匹配
        public static volatile String staticField; // static 字段
        private volatile String privateField;      // 不可见（private）
    }

    static void testNewUpdaterValidation() {
        // JDK-8 实测：非 volatile → IAE("Must be volatile type")
        checkMessage(() -> AtomicReferenceFieldUpdater.newUpdater(BadFields.class, String.class, "nonVolatile"),
                IllegalArgumentException.class, "Must be volatile type", "non-volatile field");
        // 原始类型字段 → IAE
        assertThrows(IllegalArgumentException.class,
                () -> AtomicReferenceFieldUpdater.newUpdater(BadFields.class, int.class, "intField"),
                "primitive field rejected");
        // 弱类型（Class 参数）不匹配：字段类型不是 R → CCE（newUpdater 时抛）
        assertThrows(ClassCastException.class,
                () -> AtomicReferenceFieldUpdater.newUpdater(BadFields.class, String.class, "boxedMismatch"),
                "field type not R throws ClassCastException");
        // static 字段 → IAE（JDK-8 消息 null）
        assertThrows(IllegalArgumentException.class,
                () -> AtomicReferenceFieldUpdater.newUpdater(BadFields.class, String.class, "staticField"),
                "static field rejected");
        // private 字段 → RuntimeException（包装 IllegalAccessException）
        // JDK-8 无 nestmate：同文件嵌套类 private 也拒绝
        assertThrows(RuntimeException.class,
                () -> AtomicReferenceFieldUpdater.newUpdater(BadFields.class, String.class, "privateField"),
                "private field rejected");
        // 跨类 private（java.base）→ RuntimeException
        assertThrows(RuntimeException.class,
                () -> AtomicReferenceFieldUpdater.newUpdater(Thread.class, String.class, "name"),
                "foreign private field rejected");
        // 不存在的字段 → RuntimeException（包装 NoSuchFieldException）
        assertThrows(RuntimeException.class,
                () -> AtomicReferenceFieldUpdater.newUpdater(BadFields.class, String.class, "noSuchField"),
                "unknown field rejected");
        // null 字段名 / null 类 → RuntimeException（包装 NPE）
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

    public static void main(String[] args) {
        testAtomicReferenceFieldUpdater();
        testOperationFamily();
        testNewUpdaterValidation();
        testIdentitySemantics();
        testMultiObjectIndependence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
