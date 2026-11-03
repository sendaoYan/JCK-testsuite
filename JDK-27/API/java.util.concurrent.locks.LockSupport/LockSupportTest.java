/*
 * @test
 * @summary java.util.concurrent.locks.LockSupport conformance: white-box + black-box
 *          (park/unpark permit semantics, parkNanos, park(Object)/getBlocker, and
 *          setCurrentBlocker; deterministic, no wall-clock).
 * @run main LockSupportTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

public class LockSupportTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static Thread startDaemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
        return t;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.LockSupport");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(hasMethod(c, "park") && hasMethod(c, "park", Object.class), "park/park(Object)");
        check(hasMethod(c, "parkNanos", long.class) && hasMethod(c, "parkNanos", Object.class, long.class),
              "parkNanos(long)/parkNanos(Object,long)");
        check(hasMethod(c, "parkUntil", long.class) && hasMethod(c, "parkUntil", Object.class, long.class),
              "parkUntil(long)/parkUntil(Object,long)");
        check(hasMethod(c, "unpark", Thread.class), "unpark(Thread)");
        check(hasMethod(c, "getBlocker", Thread.class), "getBlocker(Thread)");
        check(hasMethod(c, "setCurrentBlocker", Object.class), "setCurrentBlocker(Object)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testPermitSemantics() {
        // unpark before park grants a permit so park returns immediately.
        LockSupport.unpark(Thread.currentThread());
        LockSupport.park();
        check(true, "unpark-then-park returns immediately (permit)");
        // second park blocks: use a short parkNanos instead to stay deterministic.
    }

    static void testParkNanosImmediate() {
        LockSupport.parkNanos(1L);
        check(true, "parkNanos(1) returns immediately");
        LockSupport.parkNanos(new Object(), 0L);
        check(true, "parkNanos(blocker,0) returns immediately");
    }

    static void testGetBlockerNull() {
        check(LockSupport.getBlocker(Thread.currentThread()) == null,
              "getBlocker null for non-parked current thread");
    }

    static void testGetBlockerNullArgument() {
        expectThrows(() -> LockSupport.getBlocker(null), NullPointerException.class,
                     "getBlocker(null) throws NPE");
    }

    static void testSetCurrentBlockerAndCrossThread() throws Exception {
        Object blocker = new Object();
        // Deterministic blocker observation: setCurrentBlocker then a latch handshake.
        LockSupport.setCurrentBlocker(blocker);
        try {
            check(LockSupport.getBlocker(Thread.currentThread()) == blocker,
                  "getBlocker returns setCurrentBlocker value");
        } finally {
            LockSupport.setCurrentBlocker(null);
        }
        check(LockSupport.getBlocker(Thread.currentThread()) == null, "getBlocker cleared");

        // Cross-thread: park(blocker) records blocker, unpark releases.
        final Object blocker2 = new Object();
        CountDownLatch set = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            LockSupport.setCurrentBlocker(blocker2);
            set.countDown();
            LockSupport.park(); // blocks until unpark
        });
        set.await();
        check(LockSupport.getBlocker(t) == blocker2, "getBlocker(t) returns thread blocker");
        LockSupport.unpark(t);
        t.join();
        check(!t.isAlive(), "unpark releases parked thread");
    }

    static void testParkObjectPermit() {
        // unpark before park(Object) also returns immediately.
        Object blocker = new Object();
        LockSupport.unpark(Thread.currentThread());
        LockSupport.park(blocker);
        check(true, "park(Object) with prior permit returns immediately");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPermitSemantics();
        testParkNanosImmediate();
        testGetBlockerNull();
        testGetBlockerNullArgument();
        testSetCurrentBlockerAndCrossThread();
        testParkObjectPermit();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
