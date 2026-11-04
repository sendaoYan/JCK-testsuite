/*
 * @test
 * @summary java.util.concurrent.locks.Condition API conformance: await/signal between two
 *          threads.
 * @run main ConditionTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCondition() throws Exception {
        final ReentrantLock lock = new ReentrantLock();
        final Condition cond = lock.newCondition();
        final CountDownLatch started = new CountDownLatch(1);
        final AtomicBoolean signalled = new AtomicBoolean(false);

        Thread t = new Thread(() -> {
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
        t.start();

        started.await();
        lock.lock(); // blocks until the waiter has entered cond.await()
        try {
            cond.signal();
        } finally {
            lock.unlock();
        }
        t.join(5000);
        check(signalled.get(), "signal wakes awaiting thread");
    }

    public static void main(String[] args) throws Exception {
        testCondition();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
