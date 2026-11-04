/*
 * @test
 * @summary java.util.concurrent.locks.LockSupport conformance: white-box + black-box
 *          (unpark-before-park permit semantics and order independence, non-cumulative
 *          permits, parkNanos/parkUntil immediacy, blocker recording via getBlocker,
 *          park on an interrupted thread, unpark of a terminated thread).
 * @run main LockSupportTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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

    static void await(CountDownLatch l, String what) throws InterruptedException {
        if (!l.await(5, TimeUnit.SECONDS))
            throw new AssertionError("timeout waiting for " + what);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.LockSupport");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        // Note: LockSupport became final in JDK 14; on JDK 8 it is a plain public class.
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(hasMethod(c, "park") && hasMethod(c, "park", Object.class), "park/park(Object)");
        check(hasMethod(c, "parkNanos", long.class) && hasMethod(c, "parkNanos", Object.class, long.class),
              "parkNanos(long)/parkNanos(Object,long)");
        check(hasMethod(c, "parkUntil", long.class) && hasMethod(c, "parkUntil", Object.class, long.class),
              "parkUntil(long)/parkUntil(Object,long)");
        check(hasMethod(c, "unpark", Thread.class), "unpark(Thread)");
        check(hasMethod(c, "getBlocker", Thread.class), "getBlocker(Thread)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testLockSupport() throws Exception {
        check(LockSupport.getBlocker(Thread.currentThread()) == null, "getBlocker null for unparked");
        LockSupport.parkNanos(0L);
        check(true, "parkNanos(0) returns immediately");
    }

    static void testUnparkThenParkReturnsImmediately() {
        // Core deterministic contract: the permit is granted by unpark regardless of
        // ordering, so a park issued AFTER unpark returns immediately.
        LockSupport.unpark(Thread.currentThread());
        LockSupport.park();
        check(true, "park returns immediately after unpark (order independence)");
    }

    static void testUnparkThenParkObjectReturnsImmediately() {
        Object blocker = new Object();
        LockSupport.unpark(Thread.currentThread());
        LockSupport.park(blocker);
        check(true, "park(Object) returns immediately after unpark");
        check(LockSupport.getBlocker(Thread.currentThread()) == null,
              "getBlocker null after park returned");
    }

    static void testUnparkThenParkNanosReturnsImmediately() {
        LockSupport.unpark(Thread.currentThread());
        long start = System.nanoTime();
        LockSupport.parkNanos(2_000_000_000L); // 2s; permit must make it return at once
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        check(elapsedMs < 1000, "parkNanos returns immediately with a permit (took " + elapsedMs + "ms)");
    }

    static void testPermitNotCumulative() {
        // Two unparks do not stack: the single outstanding permit is consumed by the
        // first park, so a second (bounded) park must actually wait.
        LockSupport.unpark(Thread.currentThread());
        LockSupport.unpark(Thread.currentThread());
        LockSupport.park(); // consumes the single permit
        long start = System.nanoTime();
        LockSupport.parkNanos(200_000_000L); // 200ms bounded observation
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        check(elapsedMs >= 100, "second park blocks after permit consumed (took " + elapsedMs + "ms)");
    }

    static void testParkImmediateVariants() {
        LockSupport.parkNanos(1L);
        check(true, "parkNanos(1) returns immediately");
        LockSupport.parkNanos(new Object(), 0L);
        check(true, "parkNanos(blocker,0) returns immediately");
        LockSupport.parkUntil(0L);
        check(true, "parkUntil(0) returns immediately");
        LockSupport.parkUntil(new Object(), 0L);
        check(true, "parkUntil(blocker,0) returns immediately");
        LockSupport.parkNanos(new Object(), 1L);
        check(true, "parkNanos(blocker,1) returns immediately");
        check(LockSupport.getBlocker(Thread.currentThread()) == null,
              "getBlocker null after timed parks returned");
    }

    static void testParkNullBlocker() {
        LockSupport.unpark(Thread.currentThread());
        LockSupport.park(null); // null blocker is legal
        check(true, "park(null) returns immediately with a permit");
        check(LockSupport.getBlocker(Thread.currentThread()) == null,
              "getBlocker null for park(null)");
    }

    static void testGetBlockerOfParkedThread() throws Exception {
        final Object blocker = new Object();
        final CountDownLatch done = new CountDownLatch(1);
        final CountDownLatch parked = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            parked.countDown();
            LockSupport.park(blocker); // blocks until unpark
            done.countDown();
        });
        // Bounded observation: the blocker is visible while the thread is parked.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        Object observed = null;
        while (System.nanoTime() < deadline) {
            observed = LockSupport.getBlocker(t);
            if (observed != null) break;
            Thread.yield();
        }
        check(observed == blocker, "park(blocker) records the blocker object");
        LockSupport.unpark(t);
        await(done, "parked thread to be released");
        t.join(5000);
        check(!t.isAlive(), "unpark releases parked thread");
        check(LockSupport.getBlocker(t) == null, "blocker cleared after park returned");
    }

    static void testParkNanosBlockerParked() throws Exception {
        final Object blocker = new Object();
        final CountDownLatch done = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            LockSupport.parkNanos(blocker, 2_000_000_000L); // 2s bounded park
            done.countDown();
        });
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        Object observed = null;
        while (System.nanoTime() < deadline) {
            observed = LockSupport.getBlocker(t);
            if (observed != null) break;
            Thread.yield();
        }
        check(observed == blocker, "parkNanos(blocker,ns) records the blocker object");
        LockSupport.unpark(t); // releases early
        await(done, "timed parked thread to be released");
        t.join(5000);
        check(!t.isAlive(), "unpark releases timed parked thread");
    }

    static void testInterruptedParkReturnsImmediately() {
        // A thread with a pending interrupt does not park: park returns at once and
        // the interrupt status is preserved.
        Thread.currentThread().interrupt();
        long start = System.nanoTime();
        LockSupport.parkNanos(2_000_000_000L); // would be 2s; interrupt makes it immediate
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        check(elapsedMs < 1000, "parkNanos returns immediately on interrupted thread (took "
              + elapsedMs + "ms)");
        check(Thread.currentThread().isInterrupted(), "interrupt status preserved by park");
        check(Thread.interrupted(), "clearing the interrupt status returns true");
        // After clearing, a bounded park waits normally again (sanity).
        long start2 = System.nanoTime();
        LockSupport.parkNanos(50_000_000L);
        long elapsed2 = (System.nanoTime() - start2) / 1_000_000;
        check(elapsed2 >= 25, "parkNanos waits again after clearing the interrupt");
    }

    static void testUnparkTerminatedThreadIsHarmless() throws Exception {
        Thread t = startDaemon(() -> { });
        t.join(5000);
        check(!t.isAlive(), "helper thread terminated");
        LockSupport.unpark(t); // no exception, no effect
        LockSupport.unpark(t);
        check(true, "unpark on a terminated thread is harmless");
    }

    static void testGetBlockerNullArgument() {
        expectThrows(() -> LockSupport.getBlocker(null), NullPointerException.class,
                     "getBlocker(null) throws NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testLockSupport();
        testUnparkThenParkReturnsImmediately();
        testUnparkThenParkObjectReturnsImmediately();
        testUnparkThenParkNanosReturnsImmediately();
        testPermitNotCumulative();
        testParkImmediateVariants();
        testParkNullBlocker();
        testGetBlockerOfParkedThread();
        testParkNanosBlockerParked();
        testInterruptedParkReturnsImmediately();
        testUnparkTerminatedThreadIsHarmless();
        testGetBlockerNullArgument();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
