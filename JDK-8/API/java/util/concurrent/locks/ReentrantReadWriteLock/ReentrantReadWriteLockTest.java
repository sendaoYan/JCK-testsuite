/*
 * @test
 * @summary java.util.concurrent.locks.ReentrantReadWriteLock API conformance: write and
 *          read lock state.
 * @run main ReentrantReadWriteLockTest
 */

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReentrantReadWriteLock() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();

        wl.lock();
        try {
            check(rwl.isWriteLocked(), "write locked");
        } finally {
            wl.unlock();
        }
        check(!rwl.isWriteLocked(), "write unlocked");

        rl.lock();
        try {
            check(rwl.getReadLockCount() == 1, "read lock count 1");
        } finally {
            rl.unlock();
        }
        check(rwl.getReadLockCount() == 0, "read lock count 0");
    }

    public static void main(String[] args) {
        testReentrantReadWriteLock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
