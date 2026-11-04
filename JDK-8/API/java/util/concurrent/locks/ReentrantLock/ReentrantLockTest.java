/*
 * @test
 * @summary java.util.concurrent.locks.ReentrantLock API conformance: tryLock/isLocked/
 *          isHeldByCurrentThread, unlock, and getHoldCount.
 * @run main ReentrantLockTest
 */

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReentrantLock() {
        ReentrantLock lock = new ReentrantLock();
        check(lock.tryLock(), "tryLock succeeds");
        check(lock.isLocked(), "isLocked");
        check(lock.isHeldByCurrentThread(), "isHeldByCurrentThread");
        lock.unlock();
        check(!lock.isLocked(), "unlock releases");
        check(lock.getHoldCount() == 0, "getHoldCount after unlock");
    }

    public static void main(String[] args) {
        testReentrantLock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
