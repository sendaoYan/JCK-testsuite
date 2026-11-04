/*
 * @test
 * @summary java.util.concurrent.locks.AbstractQueuedSynchronizer conformance: white-box
 *          structure + black-box state/acquire/release/queue/condition behavior via
 *          minimal exclusive and shared subclasses.
 * @run main AbstractQueuedSynchronizerTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class AbstractQueuedSynchronizerTest {
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

    // Exclusive boolean mutex: state 1 = held.
    static class Mutex extends AbstractQueuedSynchronizer {
        protected boolean tryAcquire(int arg) { return compareAndSetState(0, 1); }
        protected boolean tryRelease(int arg) { setState(0); return true; }
        protected boolean isHeldExclusively() { return getState() == 1; }
        // expose protected state accessors for the test harness
        int state() { return getState(); }
        void state(int v) { setState(v); }
        boolean casState(int e, int u) { return compareAndSetState(e, u); }
        AbstractQueuedSynchronizer.ConditionObject newConditionObject() { return new ConditionObject(); }
    }

    // Shared boolean latch: state 1 = open.
    static class BooleanLatch extends AbstractQueuedSynchronizer {
        protected int tryAcquireShared(int arg) { return getState() == 1 ? 1 : -1; }
        protected boolean tryReleaseShared(int arg) { setState(1); return true; }
        void close() { setState(0); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.AbstractQueuedSynchronizer");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass() == Class.forName("java.util.concurrent.locks.AbstractOwnableSynchronizer"),
              "extends AbstractOwnableSynchronizer");
        String[] names = {"acquire", "acquireInterruptibly", "acquireShared", "acquireSharedInterruptibly",
                          "release", "releaseShared",
                          "hasQueuedThreads", "hasQueuedPredecessors", "getQueueLength", "getQueuedThreads",
                          "getFirstQueuedThread", "getExclusiveQueuedThreads", "getSharedQueuedThreads",
                          "isQueued", "owns", "hasWaiters", "getWaitQueueLength",
                          "getWaitingThreads", "toString"};
        for (String n : names) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(n)) { found = true; break; }
            check(found, "method present: " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // state + toString
        Mutex m = new Mutex();
        check(m.state() == 0, "initial state 0");
        m.state(3);
        check(m.state() == 3, "setState/getState");
        check(m.casState(3, 4), "compareAndSetState true");
        check(!m.casState(3, 5), "compareAndSetState false");
        check(m.state() == 4, "compareAndSetState applied");
        m.state(0);
        check(m.toString() != null && m.toString().length() > 0, "toString non-null");

        // exclusive tryAcquire/tryRelease/isHeldExclusively
        check(!m.isHeldExclusively(), "isHeldExclusively false initially");
        check(m.tryAcquire(1), "tryAcquire succeeds when free");
        check(m.isHeldExclusively(), "isHeldExclusively true after tryAcquire");
        check(!m.tryAcquire(1), "tryAcquire fails when already held (non-reentrant)");
        check(m.tryRelease(1), "tryRelease succeeds");
        check(!m.isHeldExclusively(), "isHeldExclusively false after release");

        // acquire/release, acquireInterruptibly, tryAcquireNanos (uncontended)
        m.acquire(1);
        check(m.isHeldExclusively(), "acquire acquired");
        m.release(1);
        check(!m.isHeldExclusively(), "release released");
        m.acquireInterruptibly(1);
        check(m.isHeldExclusively(), "acquireInterruptibly acquired");
        m.release(1);
        check(m.tryAcquireNanos(1, 1L), "tryAcquireNanos succeeds when free");
        m.release(1);

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
        m2.acquire(1); // held by main
        CountDownLatch blocked = new CountDownLatch(1);
        Thread t2 = startDaemon(() -> { m2.acquire(1); blocked.countDown(); });
        m2.release(1);
        blocked.await();
        t2.join();
        check(!t2.isAlive(), "blocked acquire completed after release");

        // shared latch: tryAcquireShared/tryReleaseShared/acquireShared/tryAcquireSharedNanos
        BooleanLatch latch = new BooleanLatch();
        check(latch.tryAcquireShared(1) == -1, "tryAcquireShared -1 when closed");
        check(latch.tryReleaseShared(1), "tryReleaseShared opens latch");
        check(latch.tryAcquireShared(1) == 1, "tryAcquireShared 1 when open");
        check(latch.tryAcquireSharedNanos(1, 1L), "tryAcquireSharedNanos true when open");
        latch.close(); // close again
        AtomicBoolean sharedAcquired = new AtomicBoolean(false);
        CountDownLatch sharedDone = new CountDownLatch(1);
        Thread ts = startDaemon(() -> {
            latch.acquireShared(1); // blocks until opened
            sharedAcquired.set(true);
            sharedDone.countDown();
        });
        latch.releaseShared(1);
        sharedDone.await();
        ts.join();
        check(sharedAcquired.get(), "acquireShared unblocked by releaseShared");

        // acquireSharedInterruptibly (uncontended, open latch)
        latch.acquireSharedInterruptibly(1);
        check(true, "acquireSharedInterruptibly on open latch returns immediately");

        // ConditionObject: await/signal + hasWaiters/getWaitQueueLength/getWaitingThreads/owns
        Mutex mc = new Mutex();
        AbstractQueuedSynchronizer.ConditionObject cond = mc.newConditionObject();
        check(mc.owns(cond), "owns(ConditionObject) true for own condition");
        mc.acquire(1);
        try {
            check(!mc.hasWaiters(cond), "hasWaiters false initially");
            check(mc.getWaitQueueLength(cond) == 0, "getWaitQueueLength 0");
            check(mc.getWaitingThreads(cond).isEmpty(), "getWaitingThreads empty");
        } finally {
            mc.release(1);
        }

        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean woken = new AtomicBoolean(false);
        Thread waiter = startDaemon(() -> {
            mc.acquire(1);
            try {
                ready.countDown();
                cond.await();
                woken.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                mc.release(1);
                done.countDown();
            }
        });
        ready.await();
        mc.acquire(1); // blocks until the waiter has released the lock inside await()
        try {
            check(mc.hasWaiters(cond), "hasWaiters true while awaiting");
            check(mc.getWaitQueueLength(cond) == 1, "getWaitQueueLength 1");
            check(mc.getWaitingThreads(cond).size() == 1, "getWaitingThreads size 1");
            cond.signal();
        } finally {
            mc.release(1);
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
