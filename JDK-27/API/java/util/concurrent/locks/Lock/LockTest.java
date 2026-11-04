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

    static void await(CountDownLatch l, String what) throws InterruptedException {
        if (!l.await(5, TimeUnit.SECONDS))
            throw new AssertionError("timeout waiting for " + what);
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

    static void testLockHoldingState() {
        ReentrantLock rl = new ReentrantLock();
        Lock lock = rl; // drive through the interface, assert on the concrete lock
        check(!rl.isHeldByCurrentThread(), "not held before lock");
        lock.lock();
        try {
            check(rl.isHeldByCurrentThread(), "lock() acquires for current thread");
            check(rl.getHoldCount() == 1, "lock() hold count 1");
        } finally {
            lock.unlock();
        }
        check(!rl.isHeldByCurrentThread(), "unlock() releases");
    }

    static void testTryLockFalseWhenHeldByOther() throws Exception {
        final ReentrantLock rl = new ReentrantLock();
        final Lock lock = rl;
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            lock.lock();
            held.countDown();
            try { await(release, "release"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { lock.unlock(); }
        });
        await(held, "helper to hold lock");
        check(!lock.tryLock(), "tryLock false while another thread holds the lock");
        release.countDown();
        t.join(5000);
        check(!t.isAlive(), "helper terminated");
    }

    static void testUnlockWithoutHold() {
        Lock lock = new ReentrantLock();
        expectThrows(lock::unlock, IllegalMonitorStateException.class,
                     "unlock() without holding throws IllegalMonitorStateException");
    }

    static void testNewConditionDistinct() {
        Lock lock = new ReentrantLock();
        check(lock.newCondition() != lock.newCondition(),
              "newCondition returns a fresh Condition per call");
    }

    static void testBlackBox() throws Exception {
        testLockUnlockTryLock();
        testTryLockTimeout();
        testLockInterruptibly();
        testLockHoldingState();
        testTryLockFalseWhenHeldByOther();
        testUnlockWithoutHold();
        testNewConditionDistinct();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
