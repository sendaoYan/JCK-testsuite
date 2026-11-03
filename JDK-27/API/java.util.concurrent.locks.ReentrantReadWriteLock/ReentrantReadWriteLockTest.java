/*
 * @test
 * @summary java.util.concurrent.locks.ReentrantReadWriteLock conformance: white-box +
 *          black-box (write exclusivity, read sharing, reentrancy, downgrade, blocked
 *          upgrade, fairness, and ReadLock/WriteLock nested Lock behavior incl. their
 *          newCondition contract).
 * @run main ReentrantReadWriteLockTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.ReentrantReadWriteLock");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(ReadWriteLock.class.isAssignableFrom(c), "implements ReadWriteLock");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null, "ReentrantReadWriteLock()");
        check(c.getConstructor(boolean.class) != null, "ReentrantReadWriteLock(boolean)");
        check(hasMethod(c, "readLock") && hasMethod(c, "writeLock"), "readLock/writeLock");
        check(hasMethod(c, "isFair"), "isFair");
        check(hasMethod(c, "getReadLockCount") && hasMethod(c, "getReadHoldCount"),
              "getReadLockCount/getReadHoldCount");
        check(hasMethod(c, "isWriteLocked") && hasMethod(c, "isWriteLockedByCurrentThread"),
              "isWriteLocked/isWriteLockedByCurrentThread");
        check(hasMethod(c, "getWriteHoldCount"), "getWriteHoldCount");

        Class<?> read = Class.forName("java.util.concurrent.locks.ReentrantReadWriteLock$ReadLock");
        Class<?> write = Class.forName("java.util.concurrent.locks.ReentrantReadWriteLock$WriteLock");
        check(Lock.class.isAssignableFrom(read), "ReadLock implements Lock");
        check(Lock.class.isAssignableFrom(write), "WriteLock implements Lock");
        check(hasMethod(read, "lock") && hasMethod(read, "unlock") && hasMethod(read, "newCondition"),
              "ReadLock lock/unlock/newCondition");
        check(hasMethod(write, "isHeldByCurrentThread") && hasMethod(write, "getHoldCount"),
              "WriteLock isHeldByCurrentThread/getHoldCount");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testWriteExclusivityAndReentrancy() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        wl.lock();
        check(rwl.isWriteLocked(), "write locked");
        check(rwl.isWriteLockedByCurrentThread(), "write held by current thread");
        check(wl.isHeldByCurrentThread(), "WriteLock.isHeldByCurrentThread");
        check(rwl.getWriteHoldCount() == 1, "write hold count 1");
        wl.lock(); // reentrant write
        check(wl.getHoldCount() == 2, "WriteLock.getHoldCount 2 reentrant");
        check(rwl.getWriteHoldCount() == 2, "getWriteHoldCount 2");
        wl.unlock();
        wl.unlock();
        check(!rwl.isWriteLocked(), "write released");
    }

    static void testReadSharingAndReentrancy() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        rl.lock();
        check(rwl.getReadLockCount() == 1, "read lock count 1");
        check(rl.tryLock(), "read lock reentrant tryLock true");
        check(rwl.getReadHoldCount() == 2, "read hold count 2");
        check(rwl.getReadLockCount() == 2, "read lock count 2");
        rl.unlock();
        rl.unlock();
        check(rwl.getReadHoldCount() == 0, "read hold count 0");
        check(rwl.getReadLockCount() == 0, "read lock count 0");
    }

    static void testReadSharedAcrossThreads() throws Exception {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        rl.lock(); // main holds read
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean acquired = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            boolean ok = rl.tryLock(); // readers can share
            acquired.set(ok);
            if (ok) rl.unlock();
            done.countDown();
        });
        done.await();
        check(acquired.get(), "read lock shareable across threads");
        rl.unlock();
    }

    static void testDowngrade() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        wl.lock();
        check(rl.tryLock(), "write holder can acquire read lock (downgrade)");
        wl.unlock(); // release write, keep read
        check(!rwl.isWriteLocked(), "write released");
        check(rwl.getReadLockCount() == 1, "read still held after downgrade");
        rl.unlock();
    }

    static void testBlockedUpgrade() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        rl.lock();
        check(!wl.tryLock(), "cannot upgrade read -> write while holding read");
        check(!rwl.isWriteLocked(), "write not acquired during upgrade attempt");
        rl.unlock();
        check(wl.tryLock(), "write acquire succeeds after read released");
        wl.unlock();
    }

    static void testReadLockNewConditionThrows() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        expectThrows(rl::newCondition, UnsupportedOperationException.class,
                     "ReadLock.newCondition throws UnsupportedOperationException");
    }

    static void testWriteLockConditionAwaitSignal() throws Exception {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        Condition cond = wl.newCondition();
        check(cond != null, "WriteLock.newCondition non-null");
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean woken = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            wl.lock();
            try {
                ready.countDown();
                cond.await();
                woken.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                wl.unlock();
                done.countDown();
            }
        });
        ready.await();
        wl.lock(); // blocks until waiter released write lock inside await()
        try {
            cond.signal();
        } finally {
            wl.unlock();
        }
        done.await();
        check(woken.get(), "WriteLock condition signal wakes waiter");
    }

    static void testFairness() {
        check(!new ReentrantReadWriteLock().isFair(), "default non-fair");
        check(new ReentrantReadWriteLock(true).isFair(), "fair constructor");
        check(!new ReentrantReadWriteLock(false).isFair(), "non-fair constructor");
    }

    static void testReadLockTryLockTimeout() throws Exception {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        check(rl.tryLock(1, TimeUnit.NANOSECONDS), "read tryLock(timeout) when free");
        rl.unlock();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWriteExclusivityAndReentrancy();
        testReadSharingAndReentrancy();
        testReadSharedAcrossThreads();
        testDowngrade();
        testBlockedUpgrade();
        testReadLockNewConditionThrows();
        testWriteLockConditionAwaitSignal();
        testFairness();
        testReadLockTryLockTimeout();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
