/*
 * @test
 * @summary java.util.concurrent.CyclicBarrier conformance: white-box structure + black-box
 *          behavior (await arrival indices, barrierAction, reset/reuse, isBroken,
 *          timed await timeout, negative parties validation).
 * @run main CyclicBarrierTest
 */

import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class CyclicBarrierTest {
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
        Class<?> c = cls("java.util.concurrent.CyclicBarrier");
        check(!c.isInterface(), "CyclicBarrier is a class");
        hasMethod(c, "await");
        hasMethod(c, "await", long.class, TimeUnit.class);
        hasMethod(c, "getParties");
        hasMethod(c, "getNumberWaiting");
        hasMethod(c, "isBroken");
        hasMethod(c, "reset");
    }

    // runs two parties through the barrier; returns their sorted arrival indices
    static int[] tripBarrier(CyclicBarrier barrier) throws Exception {
        final int[] arrival = new int[2];
        CountDownLatch ready = new CountDownLatch(2);
        final java.util.concurrent.atomic.AtomicInteger next = new java.util.concurrent.atomic.AtomicInteger();
        Runnable party = () -> {
            int me = next.getAndIncrement();
            try {
                ready.countDown();
                arrival[me] = barrier.await();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        };
        Thread a = new Thread(party);
        Thread b = new Thread(party);
        a.start();
        b.start();
        check(ready.await(10, TimeUnit.SECONDS), "both parties started");
        a.join(10000);
        b.join(10000);
        check(!a.isAlive() && !b.isAlive(), "both parties finished");
        int[] sorted = arrival.clone();
        Arrays.sort(sorted);
        return sorted;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // single-party barrier
        CyclicBarrier single = new CyclicBarrier(1);
        check(single.getParties() == 1, "getParties");
        check(single.getNumberWaiting() == 0, "getNumberWaiting");
        check(single.await() == 0, "single-party await returns 0");
        check(!single.isBroken(), "not broken");

        // two-party barrier: distinct arrival indices 0 and 1
        CyclicBarrier two = new CyclicBarrier(2);
        int[] idx = tripBarrier(two);
        check(idx[0] == 0 && idx[1] == 1, "two parties get distinct arrival indices 0 and 1");
        check(two.getNumberWaiting() == 0, "no waiting parties after trip");

        // barrierAction runs exactly once when tripped
        AtomicBoolean actionRan = new AtomicBoolean();
        CyclicBarrier withAction = new CyclicBarrier(2, () -> actionRan.set(true));
        tripBarrier(withAction);
        check(actionRan.get(), "barrierAction runs when tripped");

        // cyclic reuse: trip, reset, trip again
        CyclicBarrier reusable = new CyclicBarrier(2);
        int[] first = tripBarrier(reusable);
        check(first[0] == 0 && first[1] == 1, "first trip");
        reusable.reset();
        check(!reusable.isBroken(), "not broken after reset");
        int[] second = tripBarrier(reusable);
        check(second[0] == 0 && second[1] == 1, "second trip after reset (cyclic reuse)");
    }

    static void testValidationAndTimeout() throws Exception {
        // negative / zero parties validation
        try {
            new CyclicBarrier(-1);
            check(false, "negative parties should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "negative parties throws IllegalArgumentException");
        }
        try {
            new CyclicBarrier(0);
            check(false, "zero parties should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "zero parties throws IllegalArgumentException");
        }

        // timed await times out and breaks the barrier
        CyclicBarrier barrier = new CyclicBarrier(2);
        boolean timedOut = false;
        try {
            barrier.await(50, TimeUnit.MILLISECONDS);
        } catch (TimeoutException expected) {
            timedOut = true;
        }
        check(timedOut, "timed await throws TimeoutException when a party never arrives");
        check(barrier.isBroken(), "barrier is broken after timeout");

        // a broken barrier makes the next await throw BrokenBarrierException
        boolean broken = false;
        try {
            barrier.await();
        } catch (BrokenBarrierException expected) {
            broken = true;
        }
        check(broken, "await on a broken barrier throws BrokenBarrierException");

        // reset clears the broken state
        barrier.reset();
        check(!barrier.isBroken(), "reset clears broken state");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testValidationAndTimeout();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
