/*
 * @test
 * @summary java.util.concurrent.CountDownLatch conformance: white-box structure + black-box
 *          behavior (getCount/countDown/await blocking/timed await/negative count).
 * @run main CountDownLatchTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCountDown();
        testAwaitBlocking();
        testAwait();
        testNegativeCount();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
