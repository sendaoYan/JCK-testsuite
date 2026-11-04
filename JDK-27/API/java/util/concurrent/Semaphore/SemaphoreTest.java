/*
 * @test
 * @summary java.util.concurrent.Semaphore conformance: white-box structure + black-box
 *          behavior (acquire/release/tryAcquire/permits/drainPermits/fairness).
 * @run main SemaphoreTest
 */

import java.io.Serializable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreTest {
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
        Class<?> c = cls("java.util.concurrent.Semaphore");
        check(!c.isInterface(), "Semaphore is a class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        hasMethod(c, "acquire");
        hasMethod(c, "acquire", int.class);
        hasMethod(c, "release");
        hasMethod(c, "release", int.class);
        hasMethod(c, "tryAcquire");
        hasMethod(c, "tryAcquire", long.class, TimeUnit.class);
        hasMethod(c, "availablePermits");
        hasMethod(c, "drainPermits");
        hasMethod(c, "isFair");
        hasMethod(c, "hasQueuedThreads");
        hasMethod(c, "getQueueLength");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Semaphore sem = new Semaphore(2);
        check(sem.availablePermits() == 2, "initial permits");
        sem.acquire();
        check(sem.availablePermits() == 1, "acquire decrements");
        check(sem.tryAcquire(), "tryAcquire succeeds when permits available");
        check(sem.availablePermits() == 0, "permits exhausted");
        check(!sem.tryAcquire(), "tryAcquire fails when empty");
        sem.release();
        check(sem.availablePermits() == 1, "release increments");

        // drainPermits
        Semaphore d = new Semaphore(3);
        check(d.drainPermits() == 3, "drainPermits returns all available permits");
        check(d.availablePermits() == 0, "drainPermits leaves zero permits");

        // bulk acquire/release
        Semaphore b = new Semaphore(5);
        b.acquire(2);
        check(b.availablePermits() == 3, "acquire(2) decrements by two");
        b.release(2);
        check(b.availablePermits() == 5, "release(2) increments by two");

        // fairness flag
        Semaphore fair = new Semaphore(1, true);
        check(fair.isFair(), "fair semaphore isFair");
        Semaphore nonFair = new Semaphore(1, false);
        check(!nonFair.isFair(), "non-fair semaphore not isFair");

        // timed tryAcquire on an empty semaphore returns false
        Semaphore z = new Semaphore(0);
        check(!z.tryAcquire(50, TimeUnit.MILLISECONDS), "timed tryAcquire returns false on empty semaphore");
        check(!z.hasQueuedThreads(), "no queued threads");
        check(z.getQueueLength() == 0, "queue length zero");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
