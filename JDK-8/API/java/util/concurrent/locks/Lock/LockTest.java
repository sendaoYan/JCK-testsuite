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
    static void testLock() {
        Lock lock = new ReentrantLock();
        check(lock.tryLock(), "tryLock acquires");
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

    static void testTryLockTimeout() throws Exception {
        Lock lock = new ReentrantLock();
        check(lock.tryLock(1, TimeUnit.NANOSECONDS), "tryLock(timeout) acquires when free");
        lock.unlock();

        final ReentrantLock rl = new ReentrantLock();
        final Lock contended = rl;
        rl.lock(); // held by main
        AtomicBoolean got = new AtomicBoolean(true);
        Thread t = startDaemon(() -> {
            try { got.set(contended.tryLock(50, TimeUnit.MILLISECONDS)); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        t.join(5000);
        check(!t.isAlive() && !got.get(), "tryLock(timeout) times out false when contended");
        rl.unlock();
    }

    static void testLockInterruptibly() throws Exception {
        final ReentrantLock rl = new ReentrantLock();
        final Lock lock = rl;
        rl.lock(); // held by main
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
        await(started, "waiter to start");
        t.interrupt();
        await(done, "waiter to finish");
        check(interrupted.get(), "lockInterruptibly throws InterruptedException when interrupted");
        check(rl.isHeldByCurrentThread() && rl.getHoldCount() == 1,
              "lock not acquired by the interrupted thread");
        rl.unlock();
        check(!rl.isLocked(), "main released the lock afterwards");
        // lockInterruptibly succeeds when not contended and not interrupted
        ReentrantLock l2 = new ReentrantLock();
        l2.lockInterruptibly();
        check(l2.isHeldByCurrentThread(), "lockInterruptibly acquires when free");
        l2.unlock();
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testLock();
        testLockHoldingState();
        testTryLockFalseWhenHeldByOther();
        testTryLockTimeout();
        testLockInterruptibly();
        testUnlockWithoutHold();
        testNewConditionDistinct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
