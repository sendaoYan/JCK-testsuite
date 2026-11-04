/*
 * @test
 * @summary java.util.concurrent.locks.Condition conformance: white-box (interface) +
 *          black-box (await/signal/signalAll/timed awaits/awaitUntil/awaitUninterruptibly
 *          via ReentrantLock.newCondition).
 * @run main ConditionTest
 */

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionTest {
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.Condition");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "await"), "await()");
        check(hasMethod(c, "awaitUninterruptibly"), "awaitUninterruptibly()");
        check(hasMethod(c, "awaitNanos", long.class), "awaitNanos(long)");
        check(hasMethod(c, "await", long.class, TimeUnit.class), "await(long,TimeUnit)");
        check(hasMethod(c, "awaitUntil", Date.class), "awaitUntil(Date)");
        check(hasMethod(c, "signal") && hasMethod(c, "signalAll"), "signal/signalAll");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testAwaitSignal() throws Exception {
        final ReentrantLock lock = new ReentrantLock();
        final Condition cond = lock.newCondition();
        final CountDownLatch started = new CountDownLatch(1);
        final AtomicBoolean signalled = new AtomicBoolean(false);

        Thread t = startDaemon(() -> {
            lock.lock();
            try {
                started.countDown();
                cond.await();
                signalled.set(true);
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                lock.unlock();
            }
        });
        started.await();
        lock.lock();
        try {
            cond.signal();
        } finally {
            lock.unlock();
        }
        t.join();
        check(signalled.get(), "signal wakes awaiting thread");
    }

    static void testTimedAwaits() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        lock.lock();
        try {
            check(cond.awaitNanos(1L) <= 0, "awaitNanos(1) times out");
            check(!cond.await(1L, TimeUnit.NANOSECONDS), "await(1,ns) false on timeout");
            check(!cond.awaitUntil(new Date(0L)), "awaitUntil(past) false");
        } finally {
            lock.unlock();
        }
    }

    static void testAwaitUninterruptibly() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean returned = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            lock.lock();
            try {
                ready.countDown();
                cond.awaitUninterruptibly();
                returned.set(true);
            } finally {
                lock.unlock();
                done.countDown();
            }
        });
        ready.await();
        lock.lock();
        try {
            cond.signal();
        } finally {
            lock.unlock();
        }
        done.await();
        t.join();
        check(returned.get(), "awaitUninterruptibly returns after signal");
    }

    static void testSignalAll() throws Exception {
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
        lock.lock();
        try {
            cond.signalAll();
        } finally {
            lock.unlock();
        }
        done.await();
        check(woken.get() == 2, "signalAll wakes both waiters");
    }

    
    static void testBlackBox() throws Exception {
        testAwaitSignal();
        testTimedAwaits();
        testAwaitUninterruptibly();
        testSignalAll();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
