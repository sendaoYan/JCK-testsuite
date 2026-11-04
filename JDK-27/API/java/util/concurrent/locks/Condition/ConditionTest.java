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
            check(cond.awaitNanos(-1L) <= 0, "awaitNanos(-1) times out immediately");
            check(!cond.await(1L, TimeUnit.NANOSECONDS), "await(1,ns) false on timeout");
            check(!cond.awaitUntil(new Date(0L)), "awaitUntil(past) false");
            // no waiters after timing out (queried while holding the lock)
            check(lock.getWaitQueueLength(cond) == 0, "no waiters after timed-out awaits");
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
        AtomicBoolean interruptPreserved = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            lock.lock();
            try {
                ready.countDown();
                cond.awaitUninterruptibly(); // must ignore the interrupt and keep waiting
                returned.set(true);
                interruptPreserved.set(Thread.currentThread().isInterrupted());
            } finally {
                lock.unlock();
                done.countDown();
            }
        });
        ready.await();
        lock.lock(); // returns only once the waiter is inside awaitUninterruptibly
        try {
            t.interrupt();
            check(lock.hasWaiters(cond), "waiter still waiting after interrupt (uninterruptibly)");
            cond.signal();
        } finally {
            lock.unlock();
        }
        done.await();
        t.join();
        check(returned.get(), "awaitUninterruptibly returns after signal");
        check(interruptPreserved.get(), "interrupt status preserved across awaitUninterruptibly");
    }

    static void testAwaitReleasesLock() throws Exception {
        final ReentrantLock lock = new ReentrantLock();
        final Condition cond = lock.newCondition();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean returned = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            lock.lock();
            try {
                entered.countDown();
                cond.await(); // must release the lock while waiting
                returned.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
                done.countDown();
            }
        });
        await(entered, "waiter to enter await");
        // Deterministic proof that await() released the lock: main can acquire it,
        // and while held the waiter is registered on the condition queue.
        lock.lock();
        try {
            check(lock.getHoldCount() == 1, "main acquired the lock while waiter is in await");
            check(lock.hasWaiters(cond), "waiter registered while awaiting");
            check(lock.getWaitQueueLength(cond) == 1, "exactly one waiter");
            cond.signal();
        } finally {
            lock.unlock();
        }
        await(done, "waiter to wake and return");
        t.join(5000);
        check(returned.get(), "await returned after signal; waiter re-acquired the lock");
    }

    static void testAwaitInterrupted() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean caught = new AtomicBoolean(false);
        AtomicBoolean statusCleared = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            lock.lock();
            try {
                ready.countDown();
                cond.await();
            } catch (InterruptedException expected) {
                caught.set(true);
                statusCleared.set(!Thread.currentThread().isInterrupted());
            } finally {
                lock.unlock();
                done.countDown();
            }
        });
        await(ready, "waiter to start");
        lock.lock(); // returns only once the waiter released the lock inside await()
        try {
            t.interrupt();
        } finally {
            lock.unlock();
        }
        await(done, "waiter to finish");
        t.join(5000);
        check(caught.get(), "await throws InterruptedException when interrupted");
        check(statusCleared.get(), "interrupt status cleared when await throws InterruptedException");
    }

    static void testSignalAllWakesAllThree() throws Exception {
        final ReentrantLock lock = new ReentrantLock();
        final Condition cond = lock.newCondition();
        final int N = 3;
        CountDownLatch ready = new CountDownLatch(N);
        CountDownLatch done = new CountDownLatch(N);
        final AtomicInteger woken = new AtomicInteger();
        for (int i = 0; i < N; i++) {
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
        await(ready, "waiters to queue");
        lock.lock();
        try {
            check(lock.getWaitQueueLength(cond) == N, "N waiters queued before signalAll");
            cond.signalAll();
        } finally {
            lock.unlock();
        }
        await(done, "all waiters to wake");
        check(woken.get() == N, "signalAll wakes all " + N + " waiters");
    }

    static void testConditionIsolation() throws Exception {
        final ReentrantLock lock = new ReentrantLock();
        final Condition condA = lock.newCondition();
        final Condition condB = lock.newCondition();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch doneA = new CountDownLatch(1);
        final AtomicBoolean wokenA = new AtomicBoolean(false);
        Thread ta = startDaemon(() -> {
            lock.lock();
            try {
                ready.countDown();
                condA.await();
                wokenA.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
                doneA.countDown();
            }
        });
        Thread tb = startDaemon(() -> {
            lock.lock();
            try {
                ready.countDown();
                condB.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        });
        await(ready, "both waiters to queue");
        lock.lock();
        try {
            check(lock.getWaitQueueLength(condA) == 1 && lock.getWaitQueueLength(condB) == 1,
                  "one waiter on each condition");
            condA.signal(); // must wake only the waiter on condA
            check(lock.getWaitQueueLength(condB) == 1, "waiter on condB unaffected by condA signal");
        } finally {
            lock.unlock();
        }
        await(doneA, "condA waiter to wake");
        check(wokenA.get(), "signal on condA wakes its own waiter only");
        lock.lock();
        try {
            condB.signalAll();
        } finally {
            lock.unlock();
        }
        tb.join(5000);
        check(!tb.isAlive(), "condB waiter woken by its own signalAll");
    }

    static void testAwaitWithoutLockThrowsIMSE() {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        expectThrows(() -> { try { cond.await(); } catch (InterruptedException e) { throw new RuntimeException(e); } },
                     IllegalMonitorStateException.class, "await() without lock");
        expectThrows(() -> cond.awaitUninterruptibly(),
                     IllegalMonitorStateException.class, "awaitUninterruptibly() without lock");
        expectThrows(() -> { try { cond.awaitNanos(1L); }
                             catch (InterruptedException e) { throw new RuntimeException(e); } },
                     IllegalMonitorStateException.class, "awaitNanos() without lock");
        expectThrows(() -> { try { cond.await(1, TimeUnit.SECONDS); } catch (InterruptedException e) { throw new RuntimeException(e); } },
                     IllegalMonitorStateException.class, "await(timeout) without lock");
        expectThrows(() -> { try { cond.awaitUntil(new Date()); }
                             catch (InterruptedException e) { throw new RuntimeException(e); } },
                     IllegalMonitorStateException.class, "awaitUntil() without lock");
    }

    static void testSignalWithoutLockThrowsIMSE() {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        expectThrows(cond::signal, IllegalMonitorStateException.class, "signal() without lock");
        expectThrows(cond::signalAll, IllegalMonitorStateException.class, "signalAll() without lock");
    }

    static void testSignalWithNoWaitersIsNoOp() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        lock.lock();
        try {
            cond.signal();    // held but no waiters: must not throw
            cond.signalAll();
            check(lock.getWaitQueueLength(cond) == 0, "still no waiters after signal with empty queue");
            passed++;
        } finally {
            lock.unlock();
        }
    }

    static void testWaiterCountQueries() throws Exception {
        final ReentrantLock lock = new ReentrantLock();
        final Condition cond = lock.newCondition();
        // The wait counts may only be queried while holding the lock.
        expectThrows(() -> lock.hasWaiters(cond), IllegalMonitorStateException.class,
                     "hasWaiters without holding the lock throws IMSE");
        expectThrows(() -> lock.getWaitQueueLength(cond), IllegalMonitorStateException.class,
                     "getWaitQueueLength without holding the lock throws IMSE");
        lock.lock();
        try {
            check(!lock.hasWaiters(cond), "hasWaiters false while holding with no waiters");
            check(lock.getWaitQueueLength(cond) == 0, "getWaitQueueLength 0 while holding");
        } finally {
            lock.unlock();
        }
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            lock.lock();
            try {
                entered.countDown();
                cond.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
                done.countDown();
            }
        });
        await(entered, "waiter to enter await");
        lock.lock();
        try {
            check(lock.hasWaiters(cond), "hasWaiters true with one waiter while holding lock");
            check(lock.getWaitQueueLength(cond) == 1, "getWaitQueueLength 1 while holding lock");
            cond.signal();
        } finally {
            lock.unlock();
        }
        await(done, "waiter to finish");
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
        testAwaitReleasesLock();
        testAwaitInterrupted();
        testSignalAllWakesAllThree();
        testConditionIsolation();
        testAwaitWithoutLockThrowsIMSE();
        testSignalWithoutLockThrowsIMSE();
        testSignalWithNoWaitersIsNoOp();
        testWaiterCountQueries();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
