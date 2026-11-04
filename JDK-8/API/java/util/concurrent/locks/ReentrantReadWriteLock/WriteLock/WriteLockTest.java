/*
 * @test
 * @summary java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock conformance:
 *          white-box structure + black-box (reentrancy/hold counts, exclusivity against
 *          readers and writers, timed tryLock, lockInterruptibly with a pending interrupt,
 *          unlock-without-hold IMSE, newCondition support + no-waiters signal no-op,
 *          toString state).
 * @run main WriteLockTest
 */

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WriteLockTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.ReentrantReadWriteLock$WriteLock");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(Lock.class.isAssignableFrom(c), "implements Lock");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        String[] names = {"lock", "unlock", "tryLock", "lockInterruptibly", "newCondition",
                          "isHeldByCurrentThread", "getHoldCount"};
        for (String n : names) check(hasMethod(c, n), "method present: " + n);
        check(hasMethod(c, "tryLock", long.class, TimeUnit.class), "tryLock(long,TimeUnit)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testWriteLock() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        wl.lock();
        check(rwl.isWriteLocked(), "write lock held");
        check(rwl.isWriteLockedByCurrentThread(), "held by current thread");
        check(wl.tryLock(), "tryLock succeeds (reentrant)");
        wl.unlock();
        wl.unlock();
        check(!rwl.isWriteLocked(), "released");
    }

    static void testHoldCountsReentrant() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        check(wl.getHoldCount() == 0, "hold count 0 initially");
        check(!wl.isHeldByCurrentThread(), "not held initially");
        wl.lock();
        wl.lock();
        wl.lock();
        check(wl.getHoldCount() == 3, "hold count 3 (reentrant)");
        check(rwl.getWriteHoldCount() == 3, "parent write hold count 3");
        wl.unlock();
        check(wl.getHoldCount() == 2, "hold count 2 after one unlock");
        check(rwl.isWriteLocked(), "still locked");
        wl.unlock();
        wl.unlock();
        check(!rwl.isWriteLocked() && wl.getHoldCount() == 0, "fully released");
    }

    static void testExcludesReadersAndWriters() throws Exception {
        final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        final ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            wl.lock(); // this writer excludes everyone else
            held.countDown();
            try { await(release, "release"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { wl.unlock(); }
        });
        await(held, "writer to hold");
        check(!rwl.readLock().tryLock(), "reader excluded while another thread holds write");
        check(!wl.tryLock(), "second writer excluded (cross-thread)");
        try {
            check(!wl.tryLock(50, TimeUnit.MILLISECONDS), "timed tryLock false while write-held elsewhere");
        } catch (InterruptedException e) {
            throw new AssertionError("unexpected interrupt", e);
        }
        release.countDown();
        t.join(5000);
        check(!t.isAlive(), "writer finished");
        check(wl.tryLock(1, TimeUnit.NANOSECONDS), "timed tryLock true once free");
        wl.unlock();
    }

    static void testLockInterruptiblyWithPendingInterrupt() throws Exception {
        final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        final ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean caught = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            Thread.currentThread().interrupt(); // pending interrupt: no blocking wait
            try {
                wl.lockInterruptibly();
            } catch (InterruptedException expected) {
                caught.set(true);
            }
            done.countDown();
        });
        await(done, "child to finish");
        t.join(5000);
        check(caught.get(), "lockInterruptibly throws immediately on a pending interrupt");
        check(!rwl.isWriteLocked(), "write lock not acquired by the interrupted thread");
        wl.lockInterruptibly();
        check(wl.isHeldByCurrentThread(), "lockInterruptibly acquires when free");
        wl.unlock();
    }

    static void testUnlockWithoutHoldThrowsIMSE() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        expectThrows(wl::unlock, IllegalMonitorStateException.class,
                     "unlock() without hold throws IllegalMonitorStateException");
        // Over-release: hold 1, unlock twice -> IMSE.
        wl.lock();
        wl.unlock();
        expectThrows(wl::unlock, IllegalMonitorStateException.class,
                     "unlock beyond hold count throws IMSE");
    }

    static void testNewConditionSupportsNoWaitersSignal() throws Exception {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        Condition cond = wl.newCondition();
        check(cond != null, "newCondition returns a Condition");
        wl.lock();
        try {
            cond.signal();     // held, no waiters: must not throw
            cond.signalAll();
            check(rwl.getWaitQueueLength(cond) == 0, "still no waiters after signal");
            passed++;
        } finally {
            wl.unlock();
        }
        // Condition methods without holding throw IllegalMonitorStateException.
        expectThrows(cond::signal, IllegalMonitorStateException.class,
                     "signal() without holding throws IMSE");
    }

    static void testToStringState() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        check(wl.toString().contains("Unlocked"), "toString unlocked");
        wl.lock();
        check(wl.toString().contains("Locked by thread"), "toString locked");
        wl.unlock();
        check(wl.toString().contains("Unlocked"), "toString unlocked again");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWriteLock();
        testHoldCountsReentrant();
        testExcludesReadersAndWriters();
        testLockInterruptiblyWithPendingInterrupt();
        testUnlockWithoutHoldThrowsIMSE();
        testNewConditionSupportsNoWaitersSignal();
        testToStringState();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
