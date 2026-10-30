/*
 * @test
 * @summary java.util.concurrent.locks.StampedLock API conformance: write/read/optimistic
 *          lock state.
 * @run main StampedLockTest
 */

import java.util.concurrent.locks.StampedLock;

public class StampedLockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStampedLock() {
        StampedLock sl = new StampedLock();
        long w = sl.writeLock();
        check(sl.isWriteLocked(), "write locked");
        sl.unlockWrite(w);
        check(!sl.isWriteLocked(), "write unlocked");

        long r = sl.readLock();
        check(sl.getReadLockCount() == 1, "read lock count 1");
        sl.unlockRead(r);

        long opt = sl.tryOptimisticRead();
        check(sl.validate(opt), "optimistic read validates");
    }

    public static void main(String[] args) {
        testStampedLock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
