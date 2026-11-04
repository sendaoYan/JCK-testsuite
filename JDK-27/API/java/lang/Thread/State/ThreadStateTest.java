/*
 * @test
 * @summary java.lang.Thread.State conformance: white-box structure + black-box behavior
 *          (all six states observed deterministically with monitors and join).
 * @run main ThreadStateTest
 */

import java.util.concurrent.CountDownLatch;

public class ThreadStateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Bounded busy wait for a state transition (no Thread.sleep).
    static void awaitState(Thread t, Thread.State expected) {
        for (int i = 0; i < 100_000_000 && t.getState() != expected; i++) {
            Thread.onSpinWait();
        }
        check(t.getState() == expected, "thread reaches " + expected + " (was " + t.getState() + ")");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Thread.State.class;
        check(c.isEnum(), "Thread.State is an enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        check(Thread.State.values().length == 6, "exactly 6 states");
        for (String n : new String[]{"NEW", "RUNNABLE", "BLOCKED", "WAITING",
                                     "TIMED_WAITING", "TERMINATED"}) {
            boolean found = false;
            for (Thread.State s : Thread.State.values()) {
                if (s.name().equals(n)) found = true;
            }
            check(found, "constant " + n + " declared");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testEnumMechanics() {
        check(Thread.State.valueOf("RUNNABLE") == Thread.State.RUNNABLE, "valueOf round-trip");
        try {
            Thread.State.valueOf("RUNNING");
            throw new AssertionError("valueOf(invalid): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        check(Thread.State.NEW.compareTo(Thread.State.RUNNABLE) < 0,
              "compareTo follows declaration order");
    }

    static void testNewRunnableTerminated() throws Exception {
        Thread t = new Thread(() -> { });
        check(t.getState() == Thread.State.NEW, "fresh thread is NEW");
        check(Thread.currentThread().getState() == Thread.State.RUNNABLE,
              "current thread is RUNNABLE");
        t.start();
        t.join();
        check(t.getState() == Thread.State.TERMINATED, "joined thread is TERMINATED");
    }

    static void testWaitingTimedWaitingBlocked() throws Exception {
        final Object lock = new Object();
        final CountDownLatch waiting = new CountDownLatch(1);
        final CountDownLatch timedWaiting = new CountDownLatch(1);
        final CountDownLatch blocked = new CountDownLatch(1);

        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                waiting.countDown();
                try {
                    lock.wait();            // WAITING
                } catch (InterruptedException expected) { }
            }
        }, "waiter");
        waiter.setDaemon(true);

        Thread timedWaiter = new Thread(() -> {
            synchronized (lock) {
                timedWaiting.countDown();
                try {
                    lock.wait(60_000);      // TIMED_WAITING
                } catch (InterruptedException expected) { }
            }
        }, "timed-waiter");
        timedWaiter.setDaemon(true);

        waiter.start();
        timedWaiter.start();
        waiting.await();
        timedWaiting.await();

        // Both waiters hold nothing now; the blocker contends on the main-held lock.
        Thread blocker = new Thread(() -> {
            blocked.countDown();
            synchronized (lock) { /* BLOCKED until the main thread releases */ }
        }, "blocker");
        blocker.setDaemon(true);

        synchronized (lock) {               // main owns the monitor
            blocker.start();
            blocked.await();
            awaitState(waiter, Thread.State.WAITING);
            awaitState(timedWaiter, Thread.State.TIMED_WAITING);
            awaitState(blocker, Thread.State.BLOCKED);
            lock.notifyAll();               // release waiters
        }                                   // release the blocker
        waiter.join();
        timedWaiter.join();
        blocker.join();
        check(waiter.getState() == Thread.State.TERMINATED, "waiter terminated");
        check(timedWaiter.getState() == Thread.State.TERMINATED, "timed waiter terminated");
        check(blocker.getState() == Thread.State.TERMINATED, "blocker terminated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testEnumMechanics();
        testNewRunnableTerminated();
        testWaitingTimedWaitingBlocked();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
