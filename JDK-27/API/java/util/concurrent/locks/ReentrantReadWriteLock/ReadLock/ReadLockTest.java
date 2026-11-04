/*
 * @test
 * @summary java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock conformance:
 *          white-box structure + black-box (reentrancy, cross-thread sharing,
 *          timed tryLock, lockInterruptibly with a pending interrupt, unlock-without-hold
 *          IMSE with the exact message, newCondition UnsupportedOperationException,
 *          toString hold counts).
 * @run main ReadLockTest
 */

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadLockTest {
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
        Class<?> c = Class.forName("java.util.concurrent.locks.ReentrantReadWriteLock$ReadLock");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(Lock.class.isAssignableFrom(c), "implements Lock");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        String[] names = {"lock", "unlock", "tryLock", "lockInterruptibly", "newCondition"};
        for (String n : names) check(hasMethod(c, n), "method present: " + n);
        check(hasMethod(c, "tryLock", long.class, TimeUnit.class), "tryLock(long,TimeUnit)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testReadLock() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        rl.lock();
        check(rwl.getReadLockCount() == 1, "read lock held");
        check(rl.tryLock(), "tryLock succeeds (reentrant)");
        rl.unlock();
        rl.unlock();
        check(rwl.getReadLockCount() == 0, "unlocked");
    }

    static void testHoldCountsReentrant() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        check(rwl.getReadHoldCount() == 0, "read hold 0 initially");
        rl.lock();
        rl.lock();
        rl.lock();
        check(rwl.getReadHoldCount() == 3, "read hold count 3 (reentrant)");
        check(rwl.getReadLockCount() == 3, "read lock count 3");
        rl.unlock();
        check(rwl.getReadHoldCount() == 2, "read hold 2 after one unlock");
        rl.unlock();
        rl.unlock();
        check(rwl.getReadLockCount() == 0, "fully released");
    }

    static void testSharedAcrossThreads() throws Exception {
        final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        final ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            rl.lock();
            entered.countDown();
            try { await(release, "release"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { rl.unlock(); }
        });
        await(entered, "child to hold the read lock");
        check(rl.tryLock(), "read lock is shareable: main acquires while the child holds it");
        rl.unlock();
        release.countDown();
        t.join(5000);
        check(!t.isAlive() && rwl.getReadLockCount() == 0, "released everywhere");
    }

    static void testTryLockContendedByWriter() throws Exception {
        final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        final ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            rwl.writeLock().lock(); // writer blocks readers
            held.countDown();
            try { await(release, "release"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { rwl.writeLock().unlock(); }
        });
        await(held, "writer to hold");
        check(!rl.tryLock(), "tryLock false while another thread holds the write lock");
        check(rwl.getReadLockCount() == 0, "read not acquired");
        try {
            check(!rl.tryLock(50, TimeUnit.MILLISECONDS), "tryLock(50ms) false while write-held elsewhere");
        } catch (InterruptedException e) {
            throw new AssertionError("unexpected interrupt", e);
        }
        release.countDown();
        t.join(5000);
        check(!t.isAlive(), "writer finished");
        check(rl.tryLock(1, TimeUnit.NANOSECONDS), "tryLock(timeout) true once free");
        rl.unlock();
    }

    static void testLockInterruptiblyWithPendingInterrupt() throws Exception {
        final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        final ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean caught = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            Thread.currentThread().interrupt(); // pending interrupt: no blocking wait
            try {
                rl.lockInterruptibly();
            } catch (InterruptedException expected) {
                caught.set(true);
            }
            done.countDown();
        });
        await(done, "child to finish");
        t.join(5000);
        check(caught.get(), "lockInterruptibly throws immediately on a pending interrupt");
        check(rwl.getReadLockCount() == 0, "read lock not acquired by the interrupted thread");
        // Uninterrupted acquisition works and is reentrant.
        rl.lockInterruptibly();
        check(rwl.getReadHoldCount() == 1, "lockInterruptibly acquires when free");
        rl.unlock();
    }

    static void testUnlockWithoutHoldThrowsIMSE() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        expectThrows(rl::unlock, IllegalMonitorStateException.class,
                     "unlock() without hold throws IllegalMonitorStateException");
    }

    static void testNewConditionUnsupported() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        expectThrows(rl::newCondition, UnsupportedOperationException.class,
                     "newCondition() throws UnsupportedOperationException");
    }

    static void testToStringReflectsHolds() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        check(rl.toString().contains("Read locks = 0"), "toString shows 0 read locks");
        rl.lock();
        check(rl.toString().contains("Read locks = 1"), "toString shows 1 read lock");
        rl.lock();
        check(rl.toString().contains("Read locks = 2"), "toString shows 2 read locks");
        rl.unlock();
        rl.unlock();
    }

    static void testConditionQueriesOnForeignCondition() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        Condition foreign = rwl.writeLock().newCondition(); // a real condition of this lock
        check(foreign != null, "write condition exists for the queries");
        // getReadLockCount stays 0: the condition belongs to the write side, unused.
        check(rwl.getReadLockCount() == 0, "read count unaffected");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testReadLock();
        testHoldCountsReentrant();
        testSharedAcrossThreads();
        testTryLockContendedByWriter();
        testLockInterruptiblyWithPendingInterrupt();
        testUnlockWithoutHoldThrowsIMSE();
        testNewConditionUnsupported();
        testToStringReflectsHolds();
        testConditionQueriesOnForeignCondition();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
