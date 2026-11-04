/*
 * @test
 * @summary AbstractQueuedSynchronizer.ConditionObject conformance: implements
 *          Condition/Serializable; full await/signal semantics through a minimal
 *          exclusive synchronizer (lock release while awaiting, IllegalMonitorState
 *          when the lock is not held, interrupt semantics, signalAll, condition
 *          isolation), plus the base-class UnsupportedOperationException probe.
 * @run main ConditionObjectTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;

public class ConditionObjectTest {
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

    static final class Mutex extends AbstractQueuedSynchronizer {
        protected boolean tryAcquire(int arg) { return compareAndSetState(0, 1); }
        protected boolean tryRelease(int arg) { setState(0); return true; }
        protected boolean isHeldExclusively() { return getState() == 1; }
    }

    // Strict variant: tryRelease refuses when the lock is not held, mirroring the JDK's
    // own synchronizers so that Condition.await() without the lock fails fast with
    // IllegalMonitorStateException (thrown from fullyRelease) instead of parking.
    static final class StrictMutex extends AbstractQueuedSynchronizer {
        protected boolean tryAcquire(int arg) { return compareAndSetState(0, 1); }
        protected boolean tryRelease(int arg) {
            if (getState() != 1) return false;
            setState(0);
            return true;
        }
        protected boolean isHeldExclusively() { return getState() == 1; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject");
        check(Condition.class.isAssignableFrom(c), "implements Condition");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isStatic(c.getModifiers()), "non-static inner class (bound to the synchronizer)");
        check(!Modifier.isFinal(c.getModifiers()), "open for extension (not final)");
        // Inner class: its constructor takes the enclosing synchronizer (no no-arg ctor).
        check(c.getDeclaredConstructors().length >= 1
              && c.getDeclaredConstructors()[0].getParameterTypes().length == 1,
              "constructor takes the enclosing synchronizer instance");
    }

    static void testBaseClassUOE() {
        // A bare synchronizer does not override isHeldExclusively(); the AQS base
        // implementation throws UnsupportedOperationException, which propagates out
        // of the condition methods that check ownership.
        AbstractQueuedSynchronizer bare = new AbstractQueuedSynchronizer() {};
        AbstractQueuedSynchronizer.ConditionObject cond = bare.new ConditionObject();
        expectThrows(cond::signal, UnsupportedOperationException.class,
                     "signal() on a bare synchronizer surfaces base isHeldExclusively() UOE");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testIsConditionAndSerializable() {
        Mutex lock = new Mutex();
        AbstractQueuedSynchronizer.ConditionObject cond = lock.new ConditionObject();
        check(cond instanceof Condition, "implements Condition");
        check(cond instanceof Serializable, "implements Serializable");
    }

    static void testAwaitNanosTimesOut() throws Exception {
        Mutex lock = new Mutex();
        AbstractQueuedSynchronizer.ConditionObject cond = lock.new ConditionObject();
        lock.acquire(1);
        try {
            long remaining = cond.awaitNanos(1);
            check(remaining <= 0, "awaitNanos(1) times out");
            check(lock.getWaitQueueLength(cond) == 0, "no waiters left after timeout");
        } finally {
            lock.release(1);
        }
    }

    static void testMainAwaitsHelperSignals() throws Exception {
        // Main waits with a bounded timeout while a helper thread signals it.
        Mutex lock = new Mutex();
        AbstractQueuedSynchronizer.ConditionObject cond = lock.new ConditionObject();
        lock.acquire(1);
        Thread t = startDaemon(() -> {
            lock.acquire(1);
            try { cond.signal(); } finally { lock.release(1); }
        });
        boolean signalled = cond.await(5, TimeUnit.SECONDS);
        lock.release(1);
        t.join(5000);
        check(signalled, "await(timeout) returns true after signal()");
    }

    static void testAwaitReleasesLockAndSignalWakes() throws Exception {
        final Mutex lock = new Mutex();
        final AbstractQueuedSynchronizer.ConditionObject cond = lock.new ConditionObject();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean returned = new AtomicBoolean(false);
        Thread waiter = startDaemon(() -> {
            lock.acquire(1);
            try {
                entered.countDown();
                cond.await(); // releases the synchronizer while waiting
                returned.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.release(1);
                done.countDown();
            }
        });
        await(entered, "waiter to enter await");
        // Deterministic proof that await released the lock: main acquires it.
        lock.acquire(1);
        try {
            check(lock.isHeldExclusively(), "main holds the synchronizer while the waiter awaits");
            check(lock.hasWaiters(cond), "hasWaiters true while awaiting");
            check(lock.getWaitQueueLength(cond) == 1, "getWaitQueueLength 1");
            check(lock.getWaitingThreads(cond).size() == 1
                  && lock.getWaitingThreads(cond).contains(waiter),
                  "getWaitingThreads contains the waiter");
            cond.signal();
        } finally {
            lock.release(1);
        }
        await(done, "waiter to wake");
        waiter.join(5000);
        check(returned.get(), "signal wakes the awaiting thread and it re-acquires");
    }

    static void testWithoutLockThrowsIMSE() {
        StrictMutex lock = new StrictMutex();
        AbstractQueuedSynchronizer.ConditionObject cond = lock.new ConditionObject();
        expectThrows(() -> cond.signal(), IllegalMonitorStateException.class,
                     "signal() without holding throws IllegalMonitorStateException");
        expectThrows(() -> cond.signalAll(), IllegalMonitorStateException.class,
                     "signalAll() without holding throws IllegalMonitorStateException");
        expectThrows(() -> cond.awaitUninterruptibly(), IllegalMonitorStateException.class,
                     "awaitUninterruptibly() without holding throws IllegalMonitorStateException");
        expectThrows(() -> { try { cond.awaitNanos(1L); }
                             catch (InterruptedException e) { throw new RuntimeException(e); } },
                     IllegalMonitorStateException.class,
                     "awaitNanos() without holding throws IllegalMonitorStateException");
        expectThrows(() -> { try { cond.await(1, TimeUnit.SECONDS); }
                             catch (InterruptedException e) { throw new RuntimeException(e); } },
                     IllegalMonitorStateException.class, "await(timeout) without holding throws IMSE");
    }

    static void testSignalWithNoWaitersIsNoOp() throws Exception {
        Mutex lock = new Mutex();
        AbstractQueuedSynchronizer.ConditionObject cond = lock.new ConditionObject();
        lock.acquire(1);
        try {
            cond.signal();     // held but nobody waiting: must not throw
            cond.signalAll();
            check(lock.getWaitQueueLength(cond) == 0, "still no waiters after signal");
            passed++;
        } finally {
            lock.release(1);
        }
    }

    static void testAwaitInterrupted() throws Exception {
        final Mutex lock = new Mutex();
        final AbstractQueuedSynchronizer.ConditionObject cond = lock.new ConditionObject();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean caught = new AtomicBoolean(false);
        final AtomicBoolean statusCleared = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            lock.acquire(1);
            try {
                entered.countDown();
                cond.await();
            } catch (InterruptedException expected) {
                caught.set(true);
                statusCleared.set(!Thread.currentThread().isInterrupted());
            } finally {
                lock.release(1);
                done.countDown();
            }
        });
        await(entered, "waiter to enter await");
        lock.acquire(1);
        try {
            t.interrupt();
        } finally {
            lock.release(1);
        }
        await(done, "waiter to finish");
        t.join(5000);
        check(caught.get(), "await throws InterruptedException when interrupted");
        check(statusCleared.get(), "interrupt status cleared when await throws");
    }

    static void testAwaitUninterruptibly() throws Exception {
        final Mutex lock = new Mutex();
        final AbstractQueuedSynchronizer.ConditionObject cond = lock.new ConditionObject();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean returned = new AtomicBoolean(false);
        final AtomicBoolean interruptPreserved = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            lock.acquire(1);
            try {
                entered.countDown();
                cond.awaitUninterruptibly(); // keeps waiting across the interrupt
                returned.set(true);
                interruptPreserved.set(Thread.currentThread().isInterrupted());
            } finally {
                lock.release(1);
                done.countDown();
            }
        });
        await(entered, "waiter to enter awaitUninterruptibly");
        lock.acquire(1);
        try {
            t.interrupt();
            check(lock.hasWaiters(cond), "waiter still waiting after interrupt (uninterruptibly)");
            cond.signal();
        } finally {
            lock.release(1);
        }
        await(done, "waiter to finish");
        t.join(5000);
        check(returned.get(), "awaitUninterruptibly returns after signal");
        check(interruptPreserved.get(), "interrupt status preserved across awaitUninterruptibly");
    }

    static void testSignalAllWakesAll() throws Exception {
        final Mutex lock = new Mutex();
        final AbstractQueuedSynchronizer.ConditionObject cond = lock.new ConditionObject();
        final int N = 3;
        CountDownLatch ready = new CountDownLatch(N);
        CountDownLatch done = new CountDownLatch(N);
        final AtomicInteger woken = new AtomicInteger();
        for (int i = 0; i < N; i++) {
            startDaemon(() -> {
                lock.acquire(1);
                try {
                    ready.countDown();
                    cond.await();
                    woken.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.release(1);
                    done.countDown();
                }
            });
        }
        await(ready, "waiters to queue");
        lock.acquire(1);
        try {
            check(lock.getWaitQueueLength(cond) == N, "N waiters queued");
            cond.signalAll();
        } finally {
            lock.release(1);
        }
        await(done, "all waiters to wake");
        check(woken.get() == N, "signalAll wakes all " + N + " waiters");
    }

    static void testConditionIsolation() throws Exception {
        final Mutex lock = new Mutex();
        final AbstractQueuedSynchronizer.ConditionObject condA = lock.new ConditionObject();
        final AbstractQueuedSynchronizer.ConditionObject condB = lock.new ConditionObject();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch doneA = new CountDownLatch(1);
        final AtomicBoolean wokenA = new AtomicBoolean(false);
        Thread ta = startDaemon(() -> {
            lock.acquire(1);
            try {
                ready.countDown();
                condA.await();
                wokenA.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.release(1);
                doneA.countDown();
            }
        });
        Thread tb = startDaemon(() -> {
            lock.acquire(1);
            try {
                ready.countDown();
                condB.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.release(1);
            }
        });
        await(ready, "both waiters to queue");
        lock.acquire(1);
        try {
            check(lock.getWaitQueueLength(condA) == 1 && lock.getWaitQueueLength(condB) == 1,
                  "one waiter on each condition");
            condA.signal();
            check(lock.getWaitQueueLength(condB) == 1, "condB waiter unaffected by condA signal");
        } finally {
            lock.release(1);
        }
        await(doneA, "condA waiter to wake");
        check(wokenA.get(), "condA signal wakes its own waiter only");
        lock.acquire(1);
        try {
            condB.signalAll();
        } finally {
            lock.release(1);
        }
        tb.join(5000);
        check(!tb.isAlive(), "condB waiter woken by its own signalAll");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBaseClassUOE();
        testIsConditionAndSerializable();
        testAwaitNanosTimesOut();
        testMainAwaitsHelperSignals();
        testAwaitReleasesLockAndSignalWakes();
        testWithoutLockThrowsIMSE();
        testSignalWithNoWaitersIsNoOp();
        testAwaitInterrupted();
        testAwaitUninterruptibly();
        testSignalAllWakesAll();
        testConditionIsolation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
