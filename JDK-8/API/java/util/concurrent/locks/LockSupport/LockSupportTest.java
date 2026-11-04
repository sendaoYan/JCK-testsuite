/*
 * @test
 * @summary java.util.concurrent.locks.LockSupport conformance: park/unpark/getBlocker.
 * @run main LockSupportTest
 */

import java.util.concurrent.locks.LockSupport;

public class LockSupportTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLockSupport() throws Exception {
        check(LockSupport.getBlocker(Thread.currentThread()) == null, "getBlocker null for unparked");
        LockSupport.parkNanos(0L);
        check(true, "parkNanos(0) returns immediately");

        final Object blocker = new Object();
        Thread t = new Thread(() -> LockSupport.park(blocker));
        t.start();
        long deadline = System.currentTimeMillis() + 3000;
        while (LockSupport.getBlocker(t) == null && System.currentTimeMillis() < deadline) {
            Thread.yield();
        }
        check(LockSupport.getBlocker(t) == blocker, "park(blocker) records blocker");
        LockSupport.unpark(t);
        t.join(3000);
        check(!t.isAlive(), "unpark releases parked thread");
    }

    public static void main(String[] args) throws Exception {
        testLockSupport();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
