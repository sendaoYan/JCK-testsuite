/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicReferenceArray conformance: white-box structure plus
 *          black-box behavior of indexed get/set/lazySet/getAndSet/compareAndSet/weakCompareAndSet,
 *          update/accumulate, toString, ordering accessors, compareAndExchange, copy-on-construct,
 *          index bounds, and concurrent per-index updates.
 * @run main AtomicReferenceArrayTest
 */

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicReferenceArrayTest {
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
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicReferenceArray");
        check(!c.isInterface(), "AtomicReferenceArray is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicReferenceArray is concrete");
        check(c.getSuperclass() == Object.class, "AtomicReferenceArray extends Object");
        check(Serializable.class.isAssignableFrom(c), "AtomicReferenceArray implements Serializable");
        c.getConstructor(int.class);
        c.getConstructor(Object[].class);
        c.getMethod("length");
        c.getMethod("get", int.class);
        c.getMethod("compareAndSet", int.class, Object.class, Object.class);
        c.getMethod("updateAndGet", int.class, java.util.function.UnaryOperator.class);
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
        AtomicReferenceArray<String> a = new AtomicReferenceArray<>(3);
        check(a.length() == 3, "length");
        check(a.get(0) == null, "initial null");
        check(a.get(1) == null, "initial null (index 1)");

        a.set(0, "a");
        check("a".equals(a.get(0)), "set/get");

        check("a".equals(a.getAndSet(0, "b")) && "b".equals(a.get(0)), "getAndSet returns old");

        a.lazySet(0, "c");
        check("c".equals(a.get(0)), "lazySet then get");

        check(a.compareAndSet(0, "c", "d") && "d".equals(a.get(0)), "compareAndSet(index) success");
        check(!a.compareAndSet(0, "c", "e") && "d".equals(a.get(0)), "compareAndSet(index) failure returns false");

        check(!a.weakCompareAndSet(0, "c", "e") && "d".equals(a.get(0)), "weakCompareAndSet(index) failure");
        int spins = 0;
        while (!a.weakCompareAndSet(0, "d", "e") && spins < 100_000) spins++;
        check("e".equals(a.get(0)), "weakCompareAndSet(index) eventually succeeds uncontended");
        check(a.weakCompareAndSetPlain(0, "e", "f") && "f".equals(a.get(0)), "weakCompareAndSetPlain(index) success");
        a.set(0, "x");
        check(a.weakCompareAndSetVolatile(0, "x", "y") && "y".equals(a.get(0)), "weakCompareAndSetVolatile(index) success");
        a.set(0, "x");
        check(a.weakCompareAndSetAcquire(0, "x", "z") && "z".equals(a.get(0)), "weakCompareAndSetAcquire(index) success");
        a.set(0, "x");
        check(a.weakCompareAndSetRelease(0, "x", "w") && "w".equals(a.get(0)), "weakCompareAndSetRelease(index) success");
    }

    static void testUpdateAccessors() {
        AtomicReferenceArray<String> a = new AtomicReferenceArray<>(2);
        a.set(0, "a");
        check("a".equals(a.getAndUpdate(0, s -> s + "1")) && "a1".equals(a.get(0)), "getAndUpdate(index) returns old");
        check("a11".equals(a.updateAndGet(0, s -> s + "1")), "updateAndGet(index) returns new");
        check("a11".equals(a.getAndAccumulate(0, "x", (s, t) -> s + t)) && "a11x".equals(a.get(0)), "getAndAccumulate(index)");
        check("a11xy".equals(a.accumulateAndGet(0, "y", (s, t) -> s + t)), "accumulateAndGet(index)");

        a.setPlain(1, "p");
        check("p".equals(a.getPlain(1)), "setPlain/getPlain(index)");
        a.setOpaque(1, "o");
        check("o".equals(a.getOpaque(1)), "setOpaque/getOpaque(index)");
        a.setRelease(1, "r");
        check("r".equals(a.getAcquire(1)), "setRelease/getAcquire(index)");

        a.set(0, "v");
        check("v".equals(a.compareAndExchange(0, "v", "w")) && "w".equals(a.get(0)), "compareAndExchange(index) success returns old");
        check("w".equals(a.compareAndExchange(0, "v", "q")) && "w".equals(a.get(0)), "compareAndExchange(index) failure returns actual");
    }

    static void testConstructionAndBounds() {
        String[] seed = {"a", "b", "c"};
        AtomicReferenceArray<String> a = new AtomicReferenceArray<>(seed);
        check(a.length() == 3, "length from E[] constructor");
        check("a".equals(a.get(0)) && "b".equals(a.get(1)) && "c".equals(a.get(2)), "E[] constructor copies values");
        seed[0] = "changed";
        check("a".equals(a.get(0)), "E[] constructor does not alias source array");

        check(new AtomicReferenceArray<>(new String[]{"a", "b"}).toString().equals("[a, b]"), "toString");
        check(new AtomicReferenceArray<>(0).toString().equals("[]"), "toString of empty array");

        assertThrows(IndexOutOfBoundsException.class, () -> a.get(-1), "get negative index throws IOOBE");
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(3), "get index == length throws IOOBE");
        assertThrows(IndexOutOfBoundsException.class, () -> a.set(3, "x"), "set index == length throws IOOBE");
        assertThrows(NegativeArraySizeException.class, () -> new AtomicReferenceArray<>(-1), "negative length throws NegativeArraySizeException");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 50_000;
        final AtomicReferenceArray<Integer> a = new AtomicReferenceArray<>(4);
        for (int i = 0; i < 4; i++) a.set(i, 0);
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int t = 0; t < N; t++) {
            ts[t] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) {
                    int idx = j & 3;
                    a.updateAndGet(idx, x -> x + 1);
                }
            });
            ts[t].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        for (int i = 0; i < 4; i++) {
            check(a.get(i) == N * M / 4, "concurrent per-index updateAndGet loses no updates");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
