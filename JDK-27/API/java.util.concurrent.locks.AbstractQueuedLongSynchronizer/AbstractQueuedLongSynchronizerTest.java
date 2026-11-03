/*
 * @test
 * @summary java.util.concurrent.locks.AbstractQueuedLongSynchronizer conformance: white-box
 *          structure + black-box long-state acquire/release/queue/condition behavior via
 *          minimal exclusive and shared subclasses.
 * @run main AbstractQueuedLongSynchronizerTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;

public class AbstractQueuedLongSynchronizerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Thread startDaemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
        return t;
    }

    // Exclusive boolean mutex (long state): state 1 = held.
    static class Mutex extends AbstractQueuedLongSynchronizer {
        protected boolean tryAcquire(long arg) { return compareAndSetState(0L, 1L); }
        protected boolean tryRelease(long arg) { setState(0L); return true; }
        protected boolean isHeldExclusively() { return getState() == 1L; }
        // expose protected state accessors for the test harness
        long state() { return getState(); }
        void state(long v) { setState(v); }
        boolean casState(long e, long u) { return compareAndSetState(e, u); }
        AbstractQueuedLongSynchronizer.ConditionObject newConditionObject() { return new ConditionObject(); }
    }

    // Shared boolean latch (long state): state 1 = open.
    static class BooleanLatch extends AbstractQueuedLongSynchronizer {
        protected long tryAcquireShared(long arg) { return getState() == 1L ? 1L : -1L; }
        protected boolean tryReleaseShared(long arg) { setState(1L); return true; }
        void close() { setState(0L); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.AbstractQueuedLongSynchronizer");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass() == Class.forName("java.util.concurrent.locks.AbstractOwnableSynchronizer"),
              "extends AbstractOwnableSynchronizer");
        String[] names = {"acquire", "acquireInterruptibly", "acquireShared", "acquireSharedInterruptibly",
                          "release", "releaseShared",
                          "hasQueuedThreads", "hasQueuedPredecessors", "getQueueLength", "getQueuedThreads",
                          "getFirstQueuedThread", "getExclusiveQueuedThreads", "getSharedQueuedThreads",
                          "isQueued", "owns", "hasWaiters", "getWaitQueueLength",
                          "getWaitingThreads", "hasContended", "toString"};
        for (String n : names) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(n)) { found = true; break; }
            check(found, "method present: " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // long state + toString
        Mutex m = new Mutex();
        check(m.state() == 0L, "initial state 0");
        m.state(3L);
        check(m.state() == 3L, "setState/getState");
        check(m.casState(3L, 4L), "compareAndSetState true");
        check(!m.casState(3L, 5L), "compareAndSetState false");
        check(m.state() == 4L, "compareAndSetState applied");
        m.state(0L);
        check(m.toString() != null && m.toString().length() > 0, "toString non-null");

        // exclusive tryAcquire/tryRelease/isHeldExclusively
        check(!m.isHeldExclusively(), "isHeldExclusively false initially");
        check(m.tryAcquire(1L), "tryAcquire succeeds when free");
        check(m.isHeldExclusively(), "isHeldExclusively true after tryAcquire");
        check(!m.tryAcquire(1L), "tryAcquire fails when already held (non-reentrant)");
        check(m.tryRelease(1L), "tryRelease succeeds");
        check(!m.isHeldExclusively(), "isHeldExclusively false after release");

        // acquire/release, acquireInterruptibly, tryAcquireNanos (uncontended)
        m.acquire(1L);
        check(m.isHeldExclusively(), "acquire acquired");
        m.release(1L);
        check(!m.isHeldExclusively(), "release released");
        m.acquireInterruptibly(1L);
        check(m.isHeldExclusively(), "acquireInterruptibly acquired");
        m.release(1L);
        check(m.tryAcquireNanos(1L, 1L), "tryAcquireNanos succeeds when free");
        m.release(1L);

        // queue introspection with no contention
        check(!m.hasQueuedThreads(), "hasQueuedThreads false");
        check(!m.hasContended(), "hasContended false when uncontended");
        check(!m.hasQueuedPredecessors(), "hasQueuedPredecessors false");
        check(m.getQueueLength() == 0, "getQueueLength 0");
        check(m.getFirstQueuedThread() == null, "getFirstQueuedThread null");
        check(!m.isQueued(Thread.currentThread()), "isQueued false for current thread");
        check(m.getExclusiveQueuedThreads().isEmpty(), "getExclusiveQueuedThreads empty");
        check(m.getSharedQueuedThreads().isEmpty(), "getSharedQueuedThreads empty");
        check(m.getQueuedThreads().isEmpty(), "getQueuedThreads empty");

        // exclusive blocking acquire: another thread blocks until release
        Mutex m2 = new Mutex();
        m2.acquire(1L); // held by main
        CountDownLatch blocked = new CountDownLatch(1);
        Thread t2 = startDaemon(() -> { m2.acquire(1L); blocked.countDown(); });
        m2.release(1L);
        blocked.await();
        t2.join();
        check(!t2.isAlive(), "blocked acquire completed after release");

        // shared latch: tryAcquireShared/tryReleaseShared/acquireShared/tryAcquireSharedNanos
        BooleanLatch latch = new BooleanLatch();
        check(latch.tryAcquireShared(1L) == -1L, "tryAcquireShared -1 when closed");
        check(latch.tryReleaseShared(1L), "tryReleaseShared opens latch");
        check(latch.tryAcquireShared(1L) == 1L, "tryAcquireShared 1 when open");
        check(latch.tryAcquireSharedNanos(1L, 1L), "tryAcquireSharedNanos true when open");
        latch.close(); // close again
        AtomicBoolean sharedAcquired = new AtomicBoolean(false);
        CountDownLatch sharedDone = new CountDownLatch(1);
        Thread ts = startDaemon(() -> {
            latch.acquireShared(1L); // blocks until opened
            sharedAcquired.set(true);
            sharedDone.countDown();
        });
        latch.releaseShared(1L);
        sharedDone.await();
        ts.join();
        check(sharedAcquired.get(), "acquireShared unblocked by releaseShared");

        // acquireSharedInterruptibly (uncontended, open latch)
        latch.acquireSharedInterruptibly(1L);
        check(true, "acquireSharedInterruptibly on open latch returns immediately");

        // ConditionObject: await/signal + hasWaiters/getWaitQueueLength/getWaitingThreads/owns
        Mutex mc = new Mutex();
        AbstractQueuedLongSynchronizer.ConditionObject cond = mc.newConditionObject();
        check(mc.owns(cond), "owns(ConditionObject) true for own condition");
        mc.acquire(1L);
        try {
            check(!mc.hasWaiters(cond), "hasWaiters false initially");
            check(mc.getWaitQueueLength(cond) == 0, "getWaitQueueLength 0");
            check(mc.getWaitingThreads(cond).isEmpty(), "getWaitingThreads empty");
        } finally {
            mc.release(1L);
        }

        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean woken = new AtomicBoolean(false);
        Thread waiter = startDaemon(() -> {
            mc.acquire(1L);
            try {
                ready.countDown();
                cond.await();
                woken.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                mc.release(1L);
                done.countDown();
            }
        });
        ready.await();
        mc.acquire(1L); // blocks until the waiter has released the lock inside await()
        try {
            check(mc.hasWaiters(cond), "hasWaiters true while awaiting");
            check(mc.getWaitQueueLength(cond) == 1, "getWaitQueueLength 1");
            check(mc.getWaitingThreads(cond).size() == 1, "getWaitingThreads size 1");
            cond.signal();
        } finally {
            mc.release(1L);
        }
        done.await();
        waiter.join();
        check(woken.get(), "ConditionObject signal wakes waiter");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
