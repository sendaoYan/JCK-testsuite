/*
 * @test
 * @summary java.util.concurrent.locks.Lock conformance: white-box (interface) + black-box
 *          (lock/unlock/tryLock/tryLock(timeout)/lockInterruptibly/newCondition via
 *          ReentrantLock).
 * @run main LockTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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
        Class<?> c = Class.forName("java.util.concurrent.locks.Lock");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "lock"), "lock()");
        check(hasMethod(c, "lockInterruptibly"), "lockInterruptibly()");
        check(hasMethod(c, "tryLock"), "tryLock()");
        check(hasMethod(c, "tryLock", long.class, TimeUnit.class), "tryLock(long,TimeUnit)");
        check(hasMethod(c, "unlock"), "unlock()");
        check(hasMethod(c, "newCondition"), "newCondition()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testLockUnlockTryLock() {
        Lock lock = new ReentrantLock();
        check(lock.tryLock(), "tryLock acquires when free");
        lock.unlock();

        lock.lock();
        try {
            check(lock.tryLock(), "reentrant tryLock while held");
            lock.unlock();
        } finally {
            lock.unlock();
        }

        check(lock.newCondition() != null, "newCondition returns a Condition");
    }

    static void testTryLockTimeout() throws Exception {
        Lock lock = new ReentrantLock();
        check(lock.tryLock(1, TimeUnit.NANOSECONDS), "tryLock(timeout) when free");
        lock.unlock();
        lock.lock(); // held by main
        AtomicBoolean got = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            try { got.set(lock.tryLock(1, TimeUnit.NANOSECONDS)); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        t.join();
        check(!got.get(), "tryLock(timeout) false when contended");
        lock.unlock();
    }

    static void testLockInterruptibly() throws Exception {
        Lock lock = new ReentrantLock();
        lock.lock(); // held by main
        CountDownLatch started = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            started.countDown();
            try {
                lock.lockInterruptibly();
            } catch (InterruptedException expected) {
                interrupted.set(true);
            }
        });
        started.await();
        t.interrupt();
        t.join();
        check(interrupted.get(), "lockInterruptibly throws InterruptedException when interrupted");
        lock.unlock();

        Lock l2 = new ReentrantLock();
        l2.lockInterruptibly();
        check(true, "lockInterruptibly acquires when free");
        l2.unlock();
    }

    
    static void testBlackBox() throws Exception {
        testLockUnlockTryLock();
        testTryLockTimeout();
        testLockInterruptibly();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
