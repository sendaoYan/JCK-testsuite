/*
 * @test
 * @summary java.lang.management.LockInfo conformance: class name and identity hash.
 * @run main LockInfoTest
 */

import java.lang.management.LockInfo;

public class LockInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLockInfo() {
        LockInfo li = new LockInfo("java.util.concurrent.locks.ReentrantLock", 12345);
        check("java.util.concurrent.locks.ReentrantLock".equals(li.getClassName()), "getClassName");
        check(li.getIdentityHashCode() == 12345, "getIdentityHashCode");
        check(li.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testLockInfo();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
