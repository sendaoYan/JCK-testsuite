/*
 * @test
 * @summary java.util.concurrent.CyclicBarrier conformance: white-box structure + black-box
 *          behavior (await arrival indices, barrierAction ordering, reset/reuse, isBroken,
 *          timed await timeout, generation break propagation, negative parties validation).
 * @run main CyclicBarrierTest
 */

import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
        a.setDaemon(true);
        b.setDaemon(true);
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

    // ---- Part 3: contract deepening (appended) ----

    static void testConstructorValidation() {
        try {
            new CyclicBarrier(-1);
            check(false, "negative parties should throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() == null, "negative-parties IAE carries no detail message");
        }
        try {
            new CyclicBarrier(0, () -> { });
            check(false, "zero parties should throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() == null, "zero-parties IAE carries no detail message");
        }
    }

    // barrierAction runs exactly once per trip, while all parties are still counted
    // as waiting on the current generation (i.e. before they are released); the same
    // barrier trips twice in a row without reset (it is cyclic)
    static void testBarrierActionOrdering() throws Exception {
        final AtomicInteger actionCount = new AtomicInteger();
        final AtomicInteger waitingInsideAction = new AtomicInteger(-1);
        final CyclicBarrier[] self = new CyclicBarrier[1];
        CyclicBarrier barrier = new CyclicBarrier(2, () -> {
            actionCount.incrementAndGet();
            waitingInsideAction.set(self[0].getNumberWaiting());
        });
        self[0] = barrier;
        tripBarrier(barrier);
        check(actionCount.get() == 1, "barrierAction ran exactly once on the first trip");
        check(waitingInsideAction.get() == 2, "inside the action both parties still count as waiting");
        tripBarrier(barrier); // second trip, no reset in between: the barrier is cyclic
        check(actionCount.get() == 2, "barrierAction runs exactly once per trip (cyclic reuse)");
        check(waitingInsideAction.get() == 2, "action still runs before the parties are released");
        check(!barrier.isBroken(), "reused barrier is not broken");
    }

    // getNumberWaiting observed with one party parked; reset() breaks that waiter
    // with BrokenBarrierException and leaves the barrier reusable
    static void testGetNumberWaitingAndReset() throws Exception {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean broken = new AtomicBoolean();
        Thread waiter = new Thread(() -> {
            entered.countDown();
            try {
                barrier.await();
            } catch (BrokenBarrierException e) {
                broken.set(true);
            } catch (InterruptedException e) {
                // not expected in this scenario
            } finally {
                done.countDown();
            }
        });
        waiter.setDaemon(true);
        waiter.start();
        check(entered.await(5, TimeUnit.SECONDS), "waiter started");
        check(barrier.getParties() == 2, "getParties is constant");
        check(barrier.getNumberWaiting() == 1, "one party currently waiting");
        barrier.reset();
        if (!done.await(2, TimeUnit.SECONDS)) {
            // the waiter had not parked on the broken generation yet; break the new one
            barrier.reset();
            check(done.await(5, TimeUnit.SECONDS), "waiter released by reset (bounded)");
        }
        waiter.join(5000);
        check(!waiter.isAlive(), "waiter finished");
        check(broken.get(), "reset() breaks a waiting party with BrokenBarrierException");
        check(!barrier.isBroken(), "reset leaves the barrier unbroken");
        check(barrier.getNumberWaiting() == 0, "no parties waiting after reset");
        int[] idx = tripBarrier(barrier);
        check(idx[0] == 0 && idx[1] == 1, "barrier reusable after reset");
    }

    // a timeout breaks the barrier for the whole generation: the same-generation
    // untimed waiter observes BrokenBarrierException, not TimeoutException
    static void testTimeoutBreaksBarrierForOthers() throws Exception {
        final CyclicBarrier barrier = new CyclicBarrier(3); // third party never arrives
        final CountDownLatch entered = new CountDownLatch(2);
        final CountDownLatch done = new CountDownLatch(2);
        final AtomicBoolean patientBroken = new AtomicBoolean();
        final AtomicBoolean impatientTimedOut = new AtomicBoolean();
        final AtomicReference<String> unexpected = new AtomicReference<>();
        Thread patient = new Thread(() -> {
            entered.countDown();
            try {
                barrier.await(); // untimed: only a broken barrier can release it
            } catch (BrokenBarrierException e) {
                patientBroken.set(true);
            } catch (InterruptedException e) {
                unexpected.set("patient interrupted");
            } finally {
                done.countDown();
            }
        });
        Thread impatient = new Thread(() -> {
            entered.countDown();
            try {
                barrier.await(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                impatientTimedOut.set(true);
            } catch (BrokenBarrierException e) {
                unexpected.set("impatient broken");
            } catch (InterruptedException e) {
                unexpected.set("impatient interrupted");
            } finally {
                done.countDown();
            }
        });
        patient.setDaemon(true);
        impatient.setDaemon(true);
        patient.start();
        impatient.start();
        check(entered.await(5, TimeUnit.SECONDS), "both waiters started");
        check(done.await(5, TimeUnit.SECONDS), "both waiters finished (bounded)");
        patient.join(5000);
        impatient.join(5000);
        check(!patient.isAlive() && !impatient.isAlive(), "both waiter threads ended");
        check(impatientTimedOut.get(), "short-timeout await throws TimeoutException");
        check(patientBroken.get(), "same-generation waiter gets BrokenBarrierException after the timeout");
        check(unexpected.get() == null, "no other outcome observed");
        check(barrier.isBroken(), "barrier is broken after the timeout");
        check(barrier.getNumberWaiting() == 0, "no parties waiting on a broken barrier");
        barrier.reset();
        check(!barrier.isBroken(), "reset clears the broken state");
        check(barrier.getParties() == 3, "parties unchanged by reset");
    }

    // interrupting one waiter breaks the barrier; the other waiter sees BrokenBarrierException
    static void testInterruptBreaksBarrier() throws Exception {
        final CyclicBarrier barrier = new CyclicBarrier(3);
        final CountDownLatch entered = new CountDownLatch(2);
        final CountDownLatch done = new CountDownLatch(2);
        final AtomicBoolean victimInterrupted = new AtomicBoolean();
        final AtomicBoolean bystanderBroken = new AtomicBoolean();
        final AtomicReference<String> unexpected = new AtomicReference<>();
        Thread victim = new Thread(() -> {
            entered.countDown();
            try {
                barrier.await();
            } catch (InterruptedException e) {
                victimInterrupted.set(true);
            } catch (BrokenBarrierException e) {
                unexpected.set("victim broken");
            } finally {
                done.countDown();
            }
        });
        Thread bystander = new Thread(() -> {
            entered.countDown();
            try {
                barrier.await();
            } catch (BrokenBarrierException e) {
                bystanderBroken.set(true);
            } catch (InterruptedException e) {
                unexpected.set("bystander interrupted");
            } finally {
                done.countDown();
            }
        });
        victim.setDaemon(true);
        bystander.setDaemon(true);
        victim.start();
        bystander.start();
        check(entered.await(5, TimeUnit.SECONDS), "both waiters started");
        victim.interrupt();
        check(done.await(5, TimeUnit.SECONDS), "both waiters finished (bounded)");
        victim.join(5000);
        bystander.join(5000);
        check(!victim.isAlive() && !bystander.isAlive(), "both waiter threads ended");
        check(victimInterrupted.get(), "interrupted waiter gets InterruptedException");
        check(bystanderBroken.get(), "other waiter gets BrokenBarrierException");
        check(unexpected.get() == null, "no other outcome observed");
        check(barrier.isBroken(), "interrupt breaks the barrier");
        barrier.reset();
        check(!barrier.isBroken(), "reset repairs the broken barrier");
    }

    // timeout <= 0: unmet parties time out immediately (and break the barrier);
    // a lone party still trips instantly
    static void testZeroAndNegativeTimeout() throws Exception {
        CyclicBarrier two = new CyclicBarrier(2);
        boolean timedOut = false;
        try {
            two.await(0, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            timedOut = true;
        }
        check(timedOut, "await(0, unit) times out immediately when parties are unmet");
        check(two.isBroken(), "the zero-timeout await breaks the barrier");
        CyclicBarrier negative = new CyclicBarrier(2);
        timedOut = false;
        try {
            negative.await(-1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            timedOut = true;
        }
        check(timedOut, "await(negative, unit) is treated as an expired timeout");
        check(negative.isBroken(), "negative-timeout await breaks the barrier");
        CyclicBarrier single = new CyclicBarrier(1);
        check(single.await(0, TimeUnit.MILLISECONDS) == 0, "a lone party trips instantly even with zero timeout");
        check(!single.isBroken(), "tripping does not break the barrier");
    }

    static void testNullUnit() {
        try {
            new CyclicBarrier(1).await(1, null);
            check(false, "await(timeout, null unit) should throw");
        } catch (NullPointerException expected) {
            check(true, "await(timeout, null unit) throws NullPointerException");
        } catch (Exception e) {
            check(false, "unexpected exception " + e);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testValidationAndTimeout();
        testConstructorValidation();
        testBarrierActionOrdering();
        testGetNumberWaitingAndReset();
        testTimeoutBreaksBarrierForOthers();
        testInterruptBreaksBarrier();
        testZeroAndNegativeTimeout();
        testNullUnit();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
