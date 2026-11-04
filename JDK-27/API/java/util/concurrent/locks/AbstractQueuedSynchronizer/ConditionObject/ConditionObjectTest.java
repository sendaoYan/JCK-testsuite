/*
 * @test
 * @summary AbstractQueuedSynchronizer.ConditionObject conformance: implements
 *          Condition/Serializable and the await/signal cycle.
 * @run main ConditionObjectTest
 */

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;

public class ConditionObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class Lock extends AbstractQueuedSynchronizer {
        protected boolean tryAcquire(int arg) { return compareAndSetState(0, 1); }
        protected boolean tryRelease(int arg) { setState(0); return true; }
        protected boolean isHeldExclusively() { return getState() == 1; }
    }

    public static void main(String[] args) throws Exception {
        Lock lock = new Lock();
        AbstractQueuedSynchronizer.ConditionObject cond = lock.new ConditionObject();
        check(cond instanceof Condition, "implements Condition");
        check(cond instanceof Serializable, "implements Serializable");

        // timed await without a signal times out (returns <= 0)
        lock.acquire(1);
        long remaining = cond.awaitNanos(1);
        lock.release(1);
        check(remaining <= 0, "awaitNanos(1) times out");

        // await/signal cycle: main waits, a second thread signals it
        lock.acquire(1);
        Thread t = new Thread(() -> {
            lock.acquire(1);
            try { cond.signal(); } finally { lock.release(1); }
        });
        t.start();
        boolean signalled = cond.await(5, TimeUnit.SECONDS);
        lock.release(1);
        t.join();
        check(signalled, "await(timeout) returns true after signal()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
