/*
 * @test
 * @summary java.util.concurrent.CountDownLatch conformance: white-box structure + black-box
 *          behavior (getCount/countDown/await blocking/timed await/negative count,
 *          interrupt, zero/negative timeout semantics, null unit, multi-waiter release).
 * @run main CountDownLatchTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CountDownLatchTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.CountDownLatch");
        check(!c.isInterface(), "CountDownLatch is a class");
        hasMethod(c, "await");
        hasMethod(c, "await", long.class, TimeUnit.class);
        hasMethod(c, "countDown");
        hasMethod(c, "getCount");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCountDown() {
        CountDownLatch latch = new CountDownLatch(2);
        check(latch.getCount() == 2, "initial count");
        latch.countDown();
        check(latch.getCount() == 1, "countDown decrements");
        latch.countDown();
        check(latch.getCount() == 0, "countDown to zero");
        latch.countDown(); // stays at zero, does not go negative
        check(latch.getCount() == 0, "count stays at zero");
    }

    static void testAwaitBlocking() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean awaited = new AtomicBoolean();

        Thread t = new Thread(() -> {
            try {
                entered.countDown();
                latch.await();
                awaited.set(true);
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                done.countDown();
            }
        });
        t.start();
        check(entered.await(10, TimeUnit.SECONDS), "worker entered await()");
        check(!awaited.get(), "await blocks while count > 0");
        latch.countDown();
        check(done.await(10, TimeUnit.SECONDS), "worker released");
        t.join(5000);
        check(!t.isAlive(), "worker thread finished");
        check(awaited.get(), "await returns after countDown");
    }

    static void testAwait() throws Exception {
        CountDownLatch zero = new CountDownLatch(0);
        check(zero.await(0, TimeUnit.MILLISECONDS), "await on zero latch returns true immediately");
        zero.await();
        passed++; // no exception, reached zero latch

        CountDownLatch never = new CountDownLatch(1);
        check(!never.await(50, TimeUnit.MILLISECONDS), "timed await returns false on timeout");

        CountDownLatch released = new CountDownLatch(1);
        released.countDown();
        check(released.await(1, TimeUnit.SECONDS), "timed await returns true once released");
    }

    static void testNegativeCount() {
        try {
            new CountDownLatch(-1);
            check(false, "negative count should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "negative count throws IllegalArgumentException");
        }
    }

    static void testBlackBox() {
        CountDownLatch latch = new CountDownLatch(3);
        String s = latch.toString();
        check(s != null && s.contains("Count = 3"), "toString contains count 3");
        latch.countDown();
        check(latch.toString().contains("Count = 2"), "toString reflects decrement");
        latch.countDown();
        latch.countDown();
        check(latch.toString().contains("Count = 0"), "toString at zero");
    }

    // ---- Part 3: contract deepening (appended) ----

    // interrupting a waiter: await throws InterruptedException promptly and the count
    // is unaffected (interrupt delivered before or after parking yields IE either way)
    static void testAwaitInterrupt() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean();
        Thread t = new Thread(() -> {
            entered.countDown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                interrupted.set(true);
            } finally {
                done.countDown();
            }
        });
        t.setDaemon(true);
        t.start();
        check(entered.await(5, TimeUnit.SECONDS), "worker entered await");
        t.interrupt();
        check(done.await(5, TimeUnit.SECONDS), "interrupted await returns promptly (bounded)");
        t.join(5000);
        check(!t.isAlive(), "interrupted worker finished");
        check(interrupted.get(), "await throws InterruptedException when interrupted");
        check(latch.getCount() == 1, "interrupt does not change the count");
    }

    static void testNegativeCountMessage() {
        try {
            new CountDownLatch(-1);
            check(false, "negative count should throw");
        } catch (IllegalArgumentException e) {
            check("count < 0".equals(e.getMessage()), "IAE message is exactly 'count < 0'");
        }
    }

    // timeout <= 0: a zero-count latch passes, a pending latch fails immediately
    static void testZeroAndNegativeTimeouts() throws Exception {
        CountDownLatch zero = new CountDownLatch(0);
        check(zero.await(0, TimeUnit.MILLISECONDS), "await(0, unit) returns true on a zero-count latch");
        check(zero.await(-1, TimeUnit.MILLISECONDS), "await(negative, unit) returns true on a zero-count latch");
        CountDownLatch pending = new CountDownLatch(1);
        check(!pending.await(0, TimeUnit.MILLISECONDS), "await(0, unit) returns false immediately when count > 0");
        check(!pending.await(-5, TimeUnit.MILLISECONDS), "await(negative, unit) returns false immediately when count > 0");
    }

    // once the count has reached zero, untimed await returns immediately
    static void testReleasedAwaitImmediate() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        latch.countDown();
        latch.await(); // count already zero: must not block
        passed++;
        check(latch.await(0, TimeUnit.NANOSECONDS), "timed await(0) is true once the count reached zero");
    }

    static void testNullUnit() throws Exception {
        CountDownLatch latch = new CountDownLatch(0);
        try {
            latch.await(1, null);
            check(false, "await(timeout, null unit) should throw");
        } catch (NullPointerException expected) {
            check(true, "await(timeout, null unit) throws NullPointerException");
        }
    }

    // one countDown releases every waiter at once
    static void testReleaseAllWaiters() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch entered = new CountDownLatch(3);
        final CountDownLatch done = new CountDownLatch(3);
        final AtomicInteger released = new AtomicInteger();
        Runnable waiter = () -> {
            try {
                entered.countDown();
                latch.await();
                released.incrementAndGet();
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                done.countDown();
            }
        };
        Thread[] waiters = new Thread[3];
        for (int i = 0; i < 3; i++) {
            waiters[i] = new Thread(waiter);
            waiters[i].setDaemon(true);
            waiters[i].start();
        }
        check(entered.await(5, TimeUnit.SECONDS), "all three waiters entered await");
        check(latch.getCount() == 1, "count still positive while waiters are blocked");
        latch.countDown();
        check(done.await(5, TimeUnit.SECONDS), "all three waiters released by one countDown");
        for (Thread t : waiters) {
            t.join(5000);
            check(!t.isAlive(), "waiter thread finished");
        }
        check(released.get() == 3, "exactly three waiters passed await");
        check(latch.getCount() == 0, "count is zero after the release");
    }

    // the count reaching zero during a timed await makes it return true
    static void testTimedAwaitReleasedDuringWait() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean();
        final CountDownLatch done = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                result.set(latch.await(10, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                done.countDown();
            }
        });
        t.setDaemon(true);
        t.start();
        latch.countDown(); // before or during the wait: true either way
        check(done.await(5, TimeUnit.SECONDS), "timed await finished (bounded)");
        t.join(5000);
        check(result.get(), "timed await returns true when the count reaches zero before the timeout");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCountDown();
        testAwaitBlocking();
        testAwait();
        testNegativeCount();
        testBlackBox();
        testAwaitInterrupt();
        testNegativeCountMessage();
        testZeroAndNegativeTimeouts();
        testReleasedAwaitImmediate();
        testNullUnit();
        testReleaseAllWaiters();
        testTimedAwaitReleasedDuringWait();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
