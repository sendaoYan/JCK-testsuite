/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicReference conformance: white-box structure plus
 *          black-box behavior of get/set/lazySet/getAndSet/compareAndSet/weakCompareAndSet,
 *          update/accumulate, toString, ordering accessors, compareAndExchange, and a
 *          concurrent updateAndGet.
 * @run main AtomicReferenceTest
 */

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceTest {
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
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicReference");
        check(!c.isInterface(), "AtomicReference is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicReference is concrete");
        check(c.getSuperclass() == Object.class, "AtomicReference extends Object");
        check(Serializable.class.isAssignableFrom(c), "AtomicReference implements Serializable");
        c.getConstructor();
        c.getConstructor(Object.class);
        c.getMethod("get");
        c.getMethod("compareAndSet", Object.class, Object.class);
        c.getMethod("updateAndGet", java.util.function.UnaryOperator.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testScalar();
        testUpdateAccessors();
        testConcurrent();
    }

    static void testScalar() {
        AtomicReference<String> r = new AtomicReference<>();
        check(r.get() == null, "default constructor initial null");
        check("x".equals(new AtomicReference<>("x").get()), "constructor(value)");

        r.set("a");
        check("a".equals(r.get()), "set/get");
        check("a".equals(r.getAndSet("b")) && "b".equals(r.get()), "getAndSet returns old, sets new");
        check("b".equals(r.toString()), "toString reflects value (String.valueOf)");
        check("null".equals(new AtomicReference<>().toString()), "toString of null value is \"null\"");

        r.lazySet("c");
        check("c".equals(r.get()), "lazySet then get");

        check(r.compareAndSet("c", "d") && "d".equals(r.get()), "compareAndSet success");
        check(!r.compareAndSet("c", "e") && "d".equals(r.get()), "compareAndSet failure returns false, unchanged");

        check(!r.weakCompareAndSet("c", "e") && "d".equals(r.get()), "weakCompareAndSet failure returns false");
        int spins = 0;
        while (!r.weakCompareAndSet("d", "e") && spins < 100_000) spins++;
        check("e".equals(r.get()), "weakCompareAndSet eventually succeeds uncontended");
        check(r.weakCompareAndSetPlain("e", "f") && "f".equals(r.get()), "weakCompareAndSetPlain success");
        r.set("x");
        check(r.weakCompareAndSetVolatile("x", "y") && "y".equals(r.get()), "weakCompareAndSetVolatile success");
        r.set("x");
        check(r.weakCompareAndSetAcquire("x", "z") && "z".equals(r.get()), "weakCompareAndSetAcquire success");
        r.set("x");
        check(r.weakCompareAndSetRelease("x", "w") && "w".equals(r.get()), "weakCompareAndSetRelease success");
    }

    static void testUpdateAccessors() {
        AtomicReference<String> r = new AtomicReference<>("a");
        check("a".equals(r.getAndUpdate(s -> s + "1")) && "a1".equals(r.get()), "getAndUpdate returns old");
        check("a11".equals(r.updateAndGet(s -> s + "1")), "updateAndGet returns new");
        check("a11".equals(r.getAndAccumulate("x", (s, t) -> s + t)) && "a11x".equals(r.get()), "getAndAccumulate returns old");
        check("a11xy".equals(r.accumulateAndGet("y", (s, t) -> s + t)), "accumulateAndGet returns new");

        r.setPlain("p");
        check("p".equals(r.getPlain()), "setPlain/getPlain");
        r.setOpaque("o");
        check("o".equals(r.getOpaque()), "setOpaque/getOpaque");
        r.setRelease("r");
        check("r".equals(r.getAcquire()), "setRelease/getAcquire");

        r.set("v");
        check("v".equals(r.compareAndExchange("v", "w")) && "w".equals(r.get()), "compareAndExchange success returns old");
        check("w".equals(r.compareAndExchange("v", "q")) && "w".equals(r.get()), "compareAndExchange failure returns actual");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 100_000;
        final AtomicReference<Integer> r = new AtomicReference<>(0);
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) r.updateAndGet(x -> x + 1);
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        check(r.get() == N * M, "concurrent updateAndGet loses no updates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
