/*
 * @test
 * @summary java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock conformance.
 * @run main WriteLockTest
 */

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WriteLockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWriteLock() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        wl.lock();
        check(rwl.isWriteLocked(), "write lock held");
        check(rwl.isWriteLockedByCurrentThread(), "held by current thread");
        check(wl.tryLock(), "tryLock succeeds (reentrant)");
        wl.unlock();
        wl.unlock();
        check(!rwl.isWriteLocked(), "released");
    }

    public static void main(String[] args) {
        testWriteLock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
