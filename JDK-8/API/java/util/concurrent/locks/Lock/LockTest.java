/*
 * @test
 * @summary java.util.concurrent.locks.Lock API conformance: lock/unlock/tryLock and
 *          newCondition via a ReentrantLock.
 * @run main LockTest
 */

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    public static void main(String[] args) {
        testLock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
