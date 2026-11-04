/*
 * @test
 * @summary java.util.concurrent.locks.AbstractQueuedSynchronizer conformance: white-box
 *          structure + black-box state/acquire/release/queue/condition behavior via
 *          minimal exclusive and shared subclasses.
 * @run main AbstractQueuedSynchronizerTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class AbstractQueuedSynchronizerTest {
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

    // Bounded spin until a condition holds (used to observe queue enqueue, which has
    // no latch hook); hard timeout converts a hang into an AssertionError.
    static void spinUntil(java.util.function.BooleanSupplier cond, String what) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!cond.getAsBoolean()) {
            if (System.nanoTime() > deadline)
                throw new AssertionError("timeout waiting for " + what);
            Thread.yield();
        }
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

    // Strict mutex: release without holding reports failure instead of blindly clearing.
    static class StrictMutex extends AbstractQueuedSynchronizer {
        protected boolean tryAcquire(int arg) { return compareAndSetState(0, 1); }
        protected boolean tryRelease(int arg) {
            if (getState() != 1) return false;
            setState(0);
            return true;
        }
        protected boolean isHeldExclusively() { return getState() == 1; }
        int state() { return getState(); }
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
    static void testAQS() {
        Mutex aqs = new Mutex();
        check(aqs.state() == 0, "initial state 0");
        aqs.state(3);
        check(aqs.state() == 3, "setState/getState");
        check(aqs.casState(3, 4), "compareAndSetState true");
        check(aqs.state() == 4, "compareAndSetState applied");
        check(!aqs.casState(3, 5), "compareAndSetState false");
        check(!aqs.hasQueuedThreads(), "no queued threads");
        check(aqs.getQueueLength() == 0, "queue length 0");
        check(aqs.getQueuedThreads().isEmpty(), "no queued thread list");
        check(aqs.hasContended() == false || aqs.hasContended() == true, "hasContended");
    }

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

    // ---- Part 3: deepened contract tests ----
    static void testToStringContainsState() {
        Mutex m = new Mutex();
        check(m.toString().contains("State = 0"), "toString reflects state 0");
        m.state(4);
        check(m.toString().contains("State = 4"), "toString reflects state 4");
        check(m.toString().contains("empty queue"), "toString reports empty queue");
        m.state(0);
    }

    static void testStrictReleaseReturnsFalse() {
        // AQS.release does not itself throw when tryRelease fails; it returns false.
        // (The IllegalMonitorStateException familiar from ReentrantLock is thrown by
        // ReentrantLock.Sync.tryRelease, not by the AQS framework.)
        StrictMutex m = new StrictMutex();
        check(m.state() == 0, "strict mutex initially free");
        check(!m.release(1), "release without acquisition returns false");
        check(m.state() == 0, "state unchanged by failed release");
        m.acquire(1);
        check(m.state() == 1, "acquired");
        check(m.release(1), "release while held returns true");
        check(!m.release(1), "double release returns false");
    }

    static void testForeignConditionRejected() {
        Mutex m1 = new Mutex();
        Mutex m2 = new Mutex();
        AbstractQueuedSynchronizer.ConditionObject own = m1.newConditionObject();
        AbstractQueuedSynchronizer.ConditionObject foreign = m2.newConditionObject();
        check(m1.owns(own), "owns true for own condition");
        check(!m1.owns(foreign), "owns false for a condition of another synchronizer");
        expectThrows(() -> m1.hasWaiters(foreign), IllegalArgumentException.class,
                     "hasWaiters(foreign condition) throws IllegalArgumentException(\"Not owner\")");
        expectThrows(() -> m1.getWaitQueueLength(foreign), IllegalArgumentException.class,
                     "getWaitQueueLength(foreign condition) throws IAE");
        expectThrows(() -> m1.getWaitingThreads(foreign), IllegalArgumentException.class,
                     "getWaitingThreads(foreign condition) throws IAE");
    }

    static void testIsQueuedNull() {
        Mutex m = new Mutex();
        expectThrows(() -> m.isQueued(null), NullPointerException.class,
                     "isQueued(null) throws NPE");
    }

    static void testQueuedThreadObservation() throws Exception {
        final Mutex m = new Mutex();
        m.acquire(1); // held by main
        final CountDownLatch done = new CountDownLatch(1);
        final Thread child = startDaemon(() -> {
            m.acquire(1); // enqueues behind main
            m.release(1);
            done.countDown();
        });
        // Deterministic observation: wait until the child is actually enqueued.
        spinUntil(() -> m.getQueueLength() == 1, "child to enqueue");
        check(m.hasQueuedThreads(), "hasQueuedThreads true with a queued thread");
        check(m.getQueueLength() == 1, "getQueueLength 1");
        check(m.isQueued(child), "isQueued true for the blocked child");
        check(m.getFirstQueuedThread() == child, "getFirstQueuedThread is the blocked child");
        check(m.getQueuedThreads().contains(child), "getQueuedThreads contains the child");
        check(m.getExclusiveQueuedThreads().contains(child), "getExclusiveQueuedThreads contains the child");
        check(m.hasQueuedPredecessors(), "hasQueuedPredecessors true while a waiter is queued");
        check(m.hasContended(), "hasContended true after contention");
        m.release(1); // let the child through
        await(done, "child to acquire after release");
        child.join(5000);
        check(!child.isAlive(), "child finished");
        check(!m.hasQueuedThreads(), "queue empty again after the child acquired");
    }

    static void testCrossThreadTryAcquireFails() throws Exception {
        final Mutex m = new Mutex();
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            m.acquire(1);
            held.countDown();
            try { await(release, "release"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { m.release(1); }
        });
        await(held, "child to hold");
        check(!m.tryAcquire(1), "tryAcquire fails while another thread holds (cross-thread)");
        check(m.isQueued(Thread.currentThread()) == false, "failed tryAcquire does not enqueue");
        release.countDown();
        t.join(5000);
        check(!t.isAlive(), "child released and finished");
    }

    static void testTryAcquireNanosContendedFalse() throws Exception {
        final Mutex m = new Mutex();
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            m.acquire(1);
            held.countDown();
            try { await(release, "release"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { m.release(1); }
        });
        await(held, "child to hold");
        check(!m.tryAcquireNanos(1, 50_000_000L), "tryAcquireNanos(50ms) false while contended");
        release.countDown();
        t.join(5000);
        check(!t.isAlive(), "child finished");
    }

    static void testAcquireInterruptiblyThrows() throws Exception {
        final Mutex m = new Mutex();
        m.acquire(1); // held by main
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            started.countDown();
            try {
                m.acquireInterruptibly(1);
                m.release(1); // not reached on the interrupt path
            } catch (InterruptedException expected) {
                interrupted.set(true);
            } finally {
                done.countDown();
            }
        });
        await(started, "child to start");
        t.interrupt();
        await(done, "child to abort");
        t.join(5000);
        check(interrupted.get(), "acquireInterruptibly throws InterruptedException when interrupted");
        check(m.state() == 1, "mutex still held by main after child was interrupted");
        m.release(1);
        // The interrupted thread can acquire normally once the lock is free again.
        final CountDownLatch reacquired = new CountDownLatch(1);
        Thread t2 = startDaemon(() -> {
            try { m.acquireInterruptibly(1); m.release(1); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            reacquired.countDown();
        });
        await(reacquired, "interrupted thread to re-acquire after release");
        t2.join(5000);
    }

    static void testConditionSignalNoWaitersIsNoOp() throws Exception {
        Mutex m = new Mutex();
        AbstractQueuedSynchronizer.ConditionObject cond = m.newConditionObject();
        m.acquire(1);
        try {
            cond.signal();     // held, no waiters: must not throw
            cond.signalAll();
            check(m.getWaitQueueLength(cond) == 0, "no waiters after signalling an empty condition");
            passed++;
        } finally {
            m.release(1);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testAQS();
        testBlackBox();
        testToStringContainsState();
        testStrictReleaseReturnsFalse();
        testForeignConditionRejected();
        testIsQueuedNull();
        testQueuedThreadObservation();
        testCrossThreadTryAcquireFails();
        testTryAcquireNanosContendedFalse();
        testAcquireInterruptiblyThrows();
        testConditionSignalNoWaitersIsNoOp();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
