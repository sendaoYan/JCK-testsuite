/*
 * @test
 * @summary java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock conformance.
 * @run main ReadLockTest
 */

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadLockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReadLock() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        rl.lock();
        check(rwl.getReadLockCount() == 1, "read lock held");
        check(rl.tryLock(), "tryLock succeeds (reentrant)");
        rl.unlock();
        rl.unlock();
        check(rwl.getReadLockCount() == 0, "unlocked");
    }

    public static void main(String[] args) {
        testReadLock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
