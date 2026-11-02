/*
 * @test
 * @summary AbstractQueuedLongSynchronizer.ConditionObject conformance:
 *          implements Condition/Serializable and the await/signal cycle.
 * @run main ConditionObjectTest
 */

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;
import java.util.concurrent.locks.Condition;

public class ConditionObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class Lock extends AbstractQueuedLongSynchronizer {
        protected boolean tryAcquire(long arg) { return compareAndSetState(0L, 1L); }
        protected boolean tryRelease(long arg) { setState(0L); return true; }
        protected boolean isHeldExclusively() { return getState() == 1L; }
    }

    public static void main(String[] args) throws Exception {
        Lock lock = new Lock();
        AbstractQueuedLongSynchronizer.ConditionObject cond = lock.new ConditionObject();
        check(cond instanceof Condition, "implements Condition");
        check(cond instanceof Serializable, "implements Serializable");

        lock.acquire(1L);
        long remaining = cond.awaitNanos(1);
        lock.release(1L);
        check(remaining <= 0, "awaitNanos(1) times out");

        lock.acquire(1L);
        Thread t = new Thread(() -> {
            lock.acquire(1L);
            try { cond.signal(); } finally { lock.release(1L); }
        });
        t.start();
        boolean signalled = cond.await(5, TimeUnit.SECONDS);
        lock.release(1L);
        t.join();
        check(signalled, "await(timeout) returns true after signal()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
