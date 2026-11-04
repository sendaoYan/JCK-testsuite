/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicInteger conformance: white-box structure plus
 *          black-box behavior of get/set/lazySet/getAndSet/compareAndSet/weakCompareAndSet,
 *          increment/decrement/add, update/accumulate, Number views, overflow wrapping,
 *          serialization round-trip, and a concurrent incrementAndGet.
 * @run main AtomicIntegerTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerTest {
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
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicInteger");
        check(!c.isInterface(), "AtomicInteger is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicInteger is concrete");
        check(c.getSuperclass() == Number.class, "AtomicInteger extends java.lang.Number");
        check(Serializable.class.isAssignableFrom(c), "AtomicInteger implements Serializable");
        c.getMethod("get");
        c.getMethod("incrementAndGet");
        c.getMethod("getAndAdd", int.class);
        c.getMethod("updateAndGet", java.util.function.IntUnaryOperator.class);
        c.getMethod("intValue");
        c.getMethod("doubleValue");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testScalar();
        testUpdateAndNumber();
        testOverflow();
        testSerialization();
        testConcurrent();
    }

    static void testScalar() {
        AtomicInteger a = new AtomicInteger();
        check(a.get() == 0, "default constructor initial 0");
        check(new AtomicInteger(5).get() == 5, "constructor(5)");

        a.set(5);
        check(a.get() == 5, "set/get");
        check(a.getAndSet(7) == 5 && a.get() == 7, "getAndSet returns old, sets new");
        check(a.toString().equals("7"), "toString");

        a.lazySet(1);
        // lazySet only promises eventual visibility; force a full ordering with a
        // subsequent volatile read-modify-write before observing.
        a.getAndAdd(0);
        check(a.get() == 1, "lazySet then ordered read");

        // increment / decrement family
        a.set(5);
        check(a.incrementAndGet() == 6, "incrementAndGet");
        check(a.getAndIncrement() == 6 && a.get() == 7, "getAndIncrement returns old");
        check(a.decrementAndGet() == 6, "decrementAndGet");
        check(a.getAndDecrement() == 6 && a.get() == 5, "getAndDecrement returns old");

        // add family
        check(a.getAndAdd(10) == 5 && a.get() == 15, "getAndAdd returns old");
        check(a.addAndGet(-3) == 12, "addAndGet");

        // compareAndSet
        check(a.compareAndSet(12, 99) && a.get() == 99, "compareAndSet success");
        check(!a.compareAndSet(0, 1) && a.get() == 99, "compareAndSet failure returns false, unchanged");

        // weakCompareAndSet failure deterministic, success retried (bounded)
        check(!a.weakCompareAndSet(0, 1) && a.get() == 99, "weakCompareAndSet failure returns false");
        int spins = 0;
        while (!a.weakCompareAndSet(99, 100) && spins < 100_000) spins++;
        check(a.get() == 100, "weakCompareAndSet eventually succeeds uncontended");
    }

    static void testUpdateAndNumber() {
        AtomicInteger a = new AtomicInteger(10);
        check(a.getAndUpdate(x -> x + 1) == 10 && a.get() == 11, "getAndUpdate returns old");
        check(a.updateAndGet(x -> x + 1) == 12, "updateAndGet returns new");
        check(a.getAndAccumulate(5, (x, y) -> x + y) == 12 && a.get() == 17, "getAndAccumulate returns old");
        check(a.accumulateAndGet(3, (x, y) -> x * y) == 51, "accumulateAndGet returns new");

        // Number views
        a.set(123);
        check(a.intValue() == 123, "intValue");
        check(a.longValue() == 123L, "longValue");
        check(a.floatValue() == 123.0f, "floatValue");
        check(a.doubleValue() == 123.0, "doubleValue");
    }

    static void testOverflow() {
        AtomicInteger a = new AtomicInteger(Integer.MAX_VALUE);
        check(a.incrementAndGet() == Integer.MIN_VALUE, "incrementAndGet wraps MAX_VALUE to MIN_VALUE");
        check(a.getAndDecrement() == Integer.MIN_VALUE && a.get() == Integer.MAX_VALUE, "decrement wraps MIN_VALUE to MAX_VALUE");
        AtomicInteger b = new AtomicInteger(Integer.MIN_VALUE);
        check(b.decrementAndGet() == Integer.MAX_VALUE, "decrementAndGet wraps MIN_VALUE to MAX_VALUE");
    }

    @SuppressWarnings("unchecked")
    static void testSerialization() throws Exception {
        AtomicInteger a = new AtomicInteger(42);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(a);
        }
        AtomicInteger copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            copy = (AtomicInteger) ois.readObject();
        }
        check(copy.get() == 42, "serialization round-trip preserves value");
        copy.compareAndSet(42, 43);
        check(copy.get() == 43, "deserialized instance remains operational");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 100_000;
        final AtomicInteger a = new AtomicInteger(0);
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) a.incrementAndGet();
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        check(a.get() == N * M, "concurrent incrementAndGet loses no updates");
    }

    // ---- 契约深化：精确返回值序列 / 函数收到旧值 / J9+ 方法缺席 ----
    static void testReturnSequences() {
        AtomicInteger a = new AtomicInteger(0);
        // 0 减到负的边界：getAndDecrement 于 0 返回 0 且值变 -1
        check(a.getAndDecrement() == 0 && a.get() == -1, "getAndDecrement from 0 to negative");
        check(a.decrementAndGet() == -2, "decrementAndGet continues negative");
        // 返回值序列全等：getAndIncrement 0..4 返回 0,1,2,3,4
        a.set(0);
        boolean seq = true;
        for (int i = 0; i < 5; i++) seq &= (a.getAndIncrement() == i);
        check(seq, "getAndIncrement return sequence 0..4");
        check(a.get() == 5, "value after sequence");
        // getAndAdd/addAndGet 精确
        a.set(10);
        check(a.getAndAdd(-20) == 10 && a.get() == -10, "getAndAdd negative delta");
        check(a.addAndGet(Integer.MIN_VALUE) == -10 + Integer.MIN_VALUE, "addAndGet MIN_VALUE wraps");
        check(a.addAndGet(0) == -10 + Integer.MIN_VALUE, "addAndGet 0 no-op");
        // getAndSet 返回链
        a.set(1);
        check(a.getAndSet(2) == 1 && a.getAndSet(3) == 2 && a.get() == 3, "getAndSet chain returns old values");
        check(a.toString().equals("3"), "toString exact value");
    }

    static void testFunctionReceivesOldValue() {
        // updateAndGet 的函数收到旧值，返回新值
        AtomicInteger a = new AtomicInteger(7);
        java.util.List<Integer> seen = new java.util.ArrayList<>();
        int r = a.updateAndGet(x -> { seen.add(x); return x + 1; });
        check(seen.size() == 1 && seen.get(0) == 7, "updateAndGet function receives old value");
        check(r == 8, "updateAndGet returns new value");
        // getAndUpdate 返回旧值
        seen.clear();
        int old = a.getAndUpdate(x -> { seen.add(x); return x * 10; });
        check(seen.get(0) == 8, "getAndUpdate function receives old value");
        check(old == 8 && a.get() == 80, "getAndUpdate returns old, stores new");
        // accumulateAndGet 组合器收到 (旧值, x)
        java.util.List<Integer> accArgs = new java.util.ArrayList<>();
        int rr = a.accumulateAndGet(5, (x, y) -> { accArgs.add(x); accArgs.add(y); return x - y; });
        check(accArgs.get(0) == 80 && accArgs.get(1) == 5, "accumulateAndGet combiner receives (old, x)");
        check(rr == 75, "accumulateAndGet returns new value");
        // getAndAccumulate 返回旧值
        check(a.getAndAccumulate(100, (x, y) -> x + y) == 75, "getAndAccumulate returns old value");
    }

    static void testNumberViews() {
        AtomicInteger a = new AtomicInteger(-1);
        check(a.intValue() == -1, "intValue negative");
        check(a.longValue() == -1L, "longValue widens");
        a.set(1_000_000);
        check(a.floatValue() == 1_000_000.0f, "floatValue exact for small values");
        check(a.doubleValue() == 1_000_000.0, "doubleValue exact for small values");
        // Number 子类身份
        check(a instanceof Number, "AtomicInteger is a Number");
    }

    // JDK-9+ 访问模式族在 JDK-8 中缺席（反射断言记录差异）
    static void testAbsentJdk9Methods() throws Exception {
        Class<?> c = AtomicInteger.class;
        String[] absent = {
            "getPlain", "setPlain", "getOpaque", "setOpaque",
            "getAcquire", "setRelease",
            "compareAndExchange", "compareAndExchangeAcquire", "compareAndExchangeRelease",
            "weakCompareAndSetPlain", "weakCompareAndSetVolatile",
            "weakCompareAndSetAcquire", "weakCompareAndSetRelease",
        };
        for (String name : absent) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getMethods()) {
                if (m.getName().equals(name)) { found = true; break; }
            }
            check(!found, name + " must be absent in JDK-8");
        }
        // JDK-8 的 weakCompareAndSet 尚未标 @Deprecated（JDK-9 起废弃）
        java.lang.reflect.Method w = c.getMethod("weakCompareAndSet", int.class, int.class);
        check(!w.isAnnotationPresent(Deprecated.class), "weakCompareAndSet not yet deprecated in JDK-8");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testReturnSequences();
        testFunctionReceivesOldValue();
        testNumberViews();
        testAbsentJdk9Methods();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
