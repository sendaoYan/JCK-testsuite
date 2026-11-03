/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicBoolean conformance: white-box structure
 *          plus black-box behavior of get/set/lazySet/getAndSet/compareAndSet/weakCompareAndSet,
 *          the plain/opaque/acquire/release accessors, compareAndExchange, and a concurrent CAS.
 * @run main AtomicBooleanTest
 */

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicBooleanTest {
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
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicBoolean");
        check(!c.isInterface(), "AtomicBoolean is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicBoolean is concrete");
        check(c.getSuperclass() == Object.class, "AtomicBoolean extends Object");
        check(Serializable.class.isAssignableFrom(c), "AtomicBoolean implements Serializable");
        // core member presence
        c.getMethod("get");
        c.getMethod("set", boolean.class);
        c.getMethod("compareAndSet", boolean.class, boolean.class);
        c.getMethod("getAndSet", boolean.class);
        c.getMethod("compareAndExchange", boolean.class, boolean.class);
        c.getMethod("getPlain");
        c.getMethod("setRelease", boolean.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testScalar();
        testAccessors();
        testConcurrent();
    }

    static void testScalar() {
        AtomicBoolean b = new AtomicBoolean();
        check(!b.get(), "default constructor initial value false");
        check(new AtomicBoolean(true).get(), "constructor(true)");

        b.set(true);
        check(b.get(), "set/get");
        check(b.getAndSet(false) && !b.get(), "getAndSet returns old and sets new");
        check(b.toString().equals("false"), "toString reflects current value");

        b.lazySet(true);
        check(b.get(), "lazySet then get");

        // compareAndSet success / failure, failure leaves value unchanged
        check(b.compareAndSet(true, false) && !b.get(), "compareAndSet success");
        check(!b.compareAndSet(true, false) && !b.get(), "compareAndSet failure returns false, unchanged");

        // weakCompareAndSet: failure is deterministic
        check(!b.weakCompareAndSet(true, false) && !b.get(), "weakCompareAndSet failure returns false, unchanged");
        // weakCompareAndSet success: may fail spuriously, retry deterministically
        int spins = 0;
        while (!b.weakCompareAndSet(false, true) && spins < 100_000) spins++;
        check(b.get(), "weakCompareAndSet eventually succeeds uncontended");
        check(b.weakCompareAndSetPlain(true, false) && !b.get(), "weakCompareAndSetPlain success");
        b.set(true);
        check(b.weakCompareAndSetVolatile(true, false) && !b.get(), "weakCompareAndSetVolatile success");
        b.set(true);
        check(b.weakCompareAndSetAcquire(true, false) && !b.get(), "weakCompareAndSetAcquire success");
        b.set(true);
        check(b.weakCompareAndSetRelease(true, false) && !b.get(), "weakCompareAndSetRelease success");
    }

    static void testAccessors() {
        AtomicBoolean b = new AtomicBoolean(false);

        // compareAndExchange returns the witness (current) value, success or not
        b.set(true);
        check(b.compareAndExchange(true, false) && b.get() == false, "compareAndExchange success returns old value true");
        check(!b.compareAndExchange(true, false), "compareAndExchange failure returns actual value false");
        b.set(true);
        check(b.compareAndExchangeAcquire(true, false) && !b.get(), "compareAndExchangeAcquire");
        b.set(true);
        check(b.compareAndExchangeRelease(true, false) && !b.get(), "compareAndExchangeRelease");

        // plain accessors
        b.set(false);
        b.setPlain(true);
        check(b.getPlain(), "setPlain/getPlain");

        // opaque accessors
        b.setOpaque(false);
        check(!b.getOpaque(), "setOpaque/getOpaque");

        // acquire/release accessors
        b.setRelease(true);
        check(b.getAcquire(), "setRelease/getAcquire");
    }

    static void testConcurrent() throws Exception {
        final int N = 8;
        final AtomicBoolean flag = new AtomicBoolean(false);
        final AtomicInteger winners = new AtomicInteger(0);
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                if (flag.compareAndSet(false, true)) winners.incrementAndGet();
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        check(flag.get(), "concurrent compareAndSet sets flag true");
        check(winners.get() == 1, "exactly one thread wins the CAS");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
