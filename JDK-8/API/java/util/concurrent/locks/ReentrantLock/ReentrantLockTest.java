/*
 * @test
 * @summary java.util.concurrent.locks.ReentrantLock conformance: white-box (reflection
 *          structure) + black-box (lock/unlock/tryLock/lockInterruptibly/reentrancy/
 *          fairness/newCondition await/signal combination behavior).
 * @run main ReentrantLockTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
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

    // Bounded spin until a condition holds; hard timeout converts a hang into an error.
    static void spinUntil(java.util.function.BooleanSupplier cond, String what) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!cond.getAsBoolean()) {
            if (System.nanoTime() > deadline)
                throw new AssertionError("timeout waiting for " + what);
            Thread.yield();
        }
    }

    // Exposes the protected introspection methods for the black-box harness.
    static class ExposedReentrantLock extends ReentrantLock {
        Thread owner() { return getOwner(); }
        java.util.Collection<Thread> queuedThreads() { return getQueuedThreads(); }
        java.util.Collection<Thread> waitingThreads(Condition c) { return getWaitingThreads(c); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.ReentrantLock");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(Lock.class.isAssignableFrom(c), "implements Lock");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        // constructors
        check(c.getConstructor() != null, "ReentrantLock()");
        check(c.getConstructor(boolean.class) != null, "ReentrantLock(boolean fair)");
        // key members
        check(hasMethod(c, "lock") && hasMethod(c, "unlock"), "lock/unlock");
        check(hasMethod(c, "tryLock"), "tryLock()");
        check(hasMethod(c, "tryLock", long.class, TimeUnit.class), "tryLock(long,TimeUnit)");
        check(hasMethod(c, "lockInterruptibly"), "lockInterruptibly");
        check(hasMethod(c, "newCondition"), "newCondition");
        check(hasMethod(c, "getHoldCount"), "getHoldCount");
        check(hasMethod(c, "isHeldByCurrentThread"), "isHeldByCurrentThread");
        check(hasMethod(c, "isLocked") && hasMethod(c, "isFair"), "isLocked/isFair");
        check(hasMethod(c, "hasQueuedThreads") && hasMethod(c, "getQueueLength"),
              "hasQueuedThreads/getQueueLength");
        check(hasMethod(c, "hasQueuedThread", Thread.class), "hasQueuedThread(Thread)");
        check(hasMethod(c, "hasWaiters", Condition.class)
              && hasMethod(c, "getWaitQueueLength", Condition.class),
              "hasWaiters/getWaitQueueLength");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testLockUnlockReentrancy() {
        ReentrantLock lock = new ReentrantLock();
        check(!lock.isLocked(), "not locked initially");
        check(!lock.isHeldByCurrentThread(), "not held initially");
        lock.lock();
        check(lock.isLocked(), "isLocked after lock");
        check(lock.isHeldByCurrentThread(), "isHeldByCurrentThread after lock");
        check(lock.getHoldCount() == 1, "holdCount 1");
        lock.lock(); // reentrant
        check(lock.getHoldCount() == 2, "holdCount 2 reentrant");
        lock.unlock();
        check(lock.getHoldCount() == 1, "holdCount 1 after one unlock");
        check(lock.isLocked(), "still locked after one unlock");
        lock.unlock();
        check(lock.getHoldCount() == 0, "holdCount 0 after full unlock");
        check(!lock.isLocked(), "unlocked");
        check(!lock.isHeldByCurrentThread(), "not held after unlock");
    }

    static void testTryLock() {
        ReentrantLock lock = new ReentrantLock();
        check(lock.tryLock(), "tryLock acquires when free");
        check(lock.tryLock(), "tryLock reentrant acquire when held by self");
        check(lock.getHoldCount() == 2, "holdCount 2 via tryLock reentrancy");
        lock.unlock();
        lock.unlock();
        check(lock.getHoldCount() == 0, "unlocked after two tryLock holds");
    }

    static void testTryLockContended() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        final AtomicBoolean acquired = new AtomicBoolean(false);
        Thread t = startDaemon(() -> acquired.set(lock.tryLock()));
        t.join();
        check(!acquired.get(), "tryLock returns false when held by another thread");
        lock.unlock();

        // tryLock(long, TimeUnit) immediate timeout when contended
        lock.lock();
        final AtomicBoolean timed = new AtomicBoolean(false);
        Thread t2 = startDaemon(() -> {
            try {
                timed.set(lock.tryLock(1, TimeUnit.NANOSECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t2.join();
        check(!timed.get(), "tryLock(1ns) returns false when contended");
        lock.unlock();

        // tryLock(long, TimeUnit) reentrant succeeds when held by self
        lock.lock();
        try {
            check(lock.tryLock(1, TimeUnit.NANOSECONDS), "tryLock(timeout) reentrant true");
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
        lock.unlock();
        lock.unlock();
    }

    static void testFairness() {
        check(!new ReentrantLock().isFair(), "default non-fair");
        check(new ReentrantLock(true).isFair(), "fair constructor");
        check(!new ReentrantLock(false).isFair(), "non-fair constructor");
    }

    static void testLockInterruptibly() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        lock.lock(); // held by main
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            started.countDown();
            try {
                lock.lockInterruptibly();
            } catch (InterruptedException expected) {
                interrupted.set(true);
            } finally {
                done.countDown();
            }
        });
        started.await();
        t.interrupt();
        done.await();
        check(interrupted.get(), "lockInterruptibly throws InterruptedException");
        check(lock.isHeldByCurrentThread(), "lock still held by main");
        check(lock.getHoldCount() == 1, "holdCount still 1");
        lock.unlock();

        // lockInterruptibly succeeds when not contended and not interrupted
        ReentrantLock l2 = new ReentrantLock();
        l2.lockInterruptibly();
        check(l2.isHeldByCurrentThread(), "lockInterruptibly acquires when free");
        l2.unlock();
    }

    static void testQueueIntrospection() {
        ReentrantLock lock = new ReentrantLock();
        check(!lock.hasQueuedThreads(), "no queued threads initially");
        check(lock.getQueueLength() == 0, "queue length 0 initially");
        Thread neverQueued = new Thread(() -> {});
        check(!lock.hasQueuedThread(neverQueued), "hasQueuedThread false for non-queued thread");
    }

    static void testConditionAwaitSignal() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        check(cond != null, "newCondition non-null");
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean woken = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            lock.lock();
            try {
                ready.countDown();
                cond.await();
                woken.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
                done.countDown();
            }
        });
        ready.await();
        lock.lock(); // blocks until waiter has entered await() and released the lock
        try {
            check(lock.hasWaiters(cond), "hasWaiters while awaiting");
            check(lock.getWaitQueueLength(cond) == 1, "getWaitQueueLength 1");
            cond.signal();
        } finally {
            lock.unlock();
        }
        done.await();
        check(woken.get(), "signal wakes awaiting thread");
    }

    static void testConditionTimeout() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        lock.lock();
        try {
            check(cond.awaitNanos(1L) <= 0, "awaitNanos(1) times out immediately");
            check(!cond.await(1L, TimeUnit.NANOSECONDS), "await(1,ns) returns false on timeout");
            // no waiters after timeout (must be queried while holding the lock)
            check(lock.getWaitQueueLength(cond) == 0, "no waiters after timeout");
        } finally {
            lock.unlock();
        }
    }

    static void testConditionSignalAll() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch done = new CountDownLatch(2);
        AtomicInteger woken = new AtomicInteger();
        for (int i = 0; i < 2; i++) {
            startDaemon(() -> {
                lock.lock();
                try {
                    ready.countDown();
                    cond.await();
                    woken.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                    done.countDown();
                }
            });
        }
        ready.await();
        lock.lock(); // blocks until both waiters released the lock inside await()
        try {
            cond.signalAll();
        } finally {
            lock.unlock();
        }
        done.await();
        check(woken.get() == 2, "signalAll wakes both waiters");
    }

    static void testUnlockWithoutHold() {
        ReentrantLock lock = new ReentrantLock();
        expectThrows(lock::unlock, IllegalMonitorStateException.class,
                     "unlock without hold throws IllegalMonitorStateException");
    }

    // ---- Part 3: deepened contract tests ----
    static void testOverReleaseThrowsIMSE() {
        ReentrantLock lock = new ReentrantLock();
        check(lock.getHoldCount() == 0, "getHoldCount 0 on a fresh unlocked lock");
        lock.lock();
        lock.lock();
        lock.unlock();
        lock.unlock();
        check(lock.getHoldCount() == 0, "hold count back to 0 after two unlocks");
        expectThrows(lock::unlock, IllegalMonitorStateException.class,
                     "unlock beyond the held count throws IllegalMonitorStateException");
    }

    static void testHoldCountThreeLevels() {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        lock.lock();
        lock.lock();
        check(lock.getHoldCount() == 3, "hold count 3 after three locks");
        check(lock.isHeldByCurrentThread() && lock.isLocked(), "still held at depth 3");
        lock.unlock();
        check(lock.getHoldCount() == 2, "hold count 2");
        check(lock.isLocked(), "still locked at depth 2");
        lock.unlock();
        check(lock.getHoldCount() == 1, "hold count 1");
        lock.unlock();
        check(lock.getHoldCount() == 0 && !lock.isLocked(), "fully released");
    }

    static void testWaiterQueryEdgeCases() {
        ReentrantLock lock = new ReentrantLock();
        Condition own = lock.newCondition();
        // The wait counts may only be queried while holding the lock (documented IMSE).
        expectThrows(() -> lock.hasWaiters(own), IllegalMonitorStateException.class,
                     "hasWaiters without holding the lock throws IMSE");
        expectThrows(() -> lock.getWaitQueueLength(own), IllegalMonitorStateException.class,
                     "getWaitQueueLength without holding the lock throws IMSE");
        lock.lock();
        try {
            check(!lock.hasWaiters(own), "hasWaiters false while holding with no waiters");
            check(lock.getWaitQueueLength(own) == 0, "getWaitQueueLength 0 while holding");
        } finally {
            lock.unlock();
        }
        // Null condition is rejected.
        expectThrows(() -> lock.hasWaiters(null), NullPointerException.class,
                     "hasWaiters(null) throws NPE");
        expectThrows(() -> lock.getWaitQueueLength(null), NullPointerException.class,
                     "getWaitQueueLength(null) throws NPE");
        // A condition created by another lock is rejected.
        Condition foreign = new ReentrantLock().newCondition();
        expectThrows(() -> lock.hasWaiters(foreign), IllegalArgumentException.class,
                     "hasWaiters(foreign condition) throws IllegalArgumentException(\"Not owner\")");
        expectThrows(() -> lock.getWaitQueueLength(foreign), IllegalArgumentException.class,
                     "getWaitQueueLength(foreign condition) throws IAE");
    }

    static void testTryLockZeroTimeoutContended() throws Exception {
        final ReentrantLock lock = new ReentrantLock();
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            lock.lock();
            held.countDown();
            try { await(release, "release"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { lock.unlock(); }
        });
        await(held, "child to hold");
        check(!lock.tryLock(0, TimeUnit.NANOSECONDS), "tryLock(0,unit) false when contended");
        release.countDown();
        t.join(5000);
        check(!t.isAlive(), "child finished");
    }

    static void testQueuedThreadIntrospection() throws Exception {
        final ReentrantLock lock = new ReentrantLock();
        lock.lock(); // held by main
        final CountDownLatch done = new CountDownLatch(1);
        final Thread child = startDaemon(() -> {
            lock.lock(); // enqueues behind main
            lock.unlock();
            done.countDown();
        });
        spinUntil(() -> lock.getQueueLength() == 1, "child to enqueue");
        check(lock.hasQueuedThreads(), "hasQueuedThreads true with a queued thread");
        check(lock.getQueueLength() == 1, "getQueueLength 1");
        check(lock.hasQueuedThread(child), "hasQueuedThread true for the blocked child");
        check(lock.isLocked() && lock.isHeldByCurrentThread(), "main still holds the lock");
        lock.unlock();
        await(done, "child to acquire after release");
        child.join(5000);
        check(!child.isAlive(), "child finished");
        check(!lock.hasQueuedThreads(), "queue empty after the child acquired");
    }

    static void testConditionNegativeNanos() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        lock.lock();
        try {
            check(cond.awaitNanos(-1L) <= 0, "awaitNanos(-1) times out immediately");
            check(!cond.await(-1L, TimeUnit.NANOSECONDS), "await(-1,unit) false immediately");
            check(lock.getWaitQueueLength(cond) == 0, "no waiters after negative timeouts");
        } finally {
            lock.unlock();
        }
    }

    static void testLockInterruptiblyRetrySucceeds() throws Exception {
        final ReentrantLock lock = new ReentrantLock();
        lock.lock(); // held by main
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);
        final CountDownLatch reacquired = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            started.countDown();
            try {
                lock.lockInterruptibly();
            } catch (InterruptedException expected) {
                interrupted.countDown();
                try {
                    lock.lockInterruptibly(); // retry: succeeds once main releases
                    lock.unlock();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                reacquired.countDown();
            }
        });
        await(started, "child to start");
        t.interrupt();
        await(interrupted, "child to abort with InterruptedException");
        check(lock.isHeldByCurrentThread() && lock.getHoldCount() == 1,
              "lock still held by main after the child was interrupted");
        lock.unlock();
        await(reacquired, "child to re-acquire after main releases");
        t.join(5000);
    }

    static void testBlackBox() {
        ExposedReentrantLock lock = new ExposedReentrantLock();
        check(lock.owner() == null, "getOwner null when unlocked");
        lock.lock();
        check(lock.owner() == Thread.currentThread(), "getOwner current thread when locked");
        check(lock.queuedThreads().isEmpty(), "getQueuedThreads empty");
        Condition cond = lock.newCondition();
        check(lock.waitingThreads(cond).isEmpty(), "getWaitingThreads empty");
        check(lock.toString() != null && lock.toString().contains("[Locked by thread"),
              "toString reports locked");
        lock.unlock();
        check(lock.owner() == null, "getOwner null after unlock");
        check(lock.toString().contains("[Unlocked]"), "toString reports unlocked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testLockUnlockReentrancy();
        testTryLock();
        testTryLockContended();
        testFairness();
        testLockInterruptibly();
        testQueueIntrospection();
        testConditionAwaitSignal();
        testConditionTimeout();
        testConditionSignalAll();
        testUnlockWithoutHold();
        testBlackBox();
        testOverReleaseThrowsIMSE();
        testHoldCountThreeLevels();
        testWaiterQueryEdgeCases();
        testTryLockZeroTimeoutContended();
        testQueuedThreadIntrospection();
        testConditionNegativeNanos();
        testLockInterruptiblyRetrySucceeds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
