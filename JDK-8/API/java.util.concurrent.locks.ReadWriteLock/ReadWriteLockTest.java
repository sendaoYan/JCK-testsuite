/*
 * @test
 * @summary java.util.concurrent.locks.ReadWriteLock API conformance: read and write locks
 *          are distinct Lock objects.
 * @run main ReadWriteLockTest
 */

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReadWriteLock() {
        ReadWriteLock rwl = new ReentrantReadWriteLock();
        Lock rl = rwl.readLock();
        Lock wl = rwl.writeLock();
        check(rl instanceof Lock && wl instanceof Lock, "both are Locks");
        check(rl != wl, "read and write locks distinct");
    }

    public static void main(String[] args) {
        testReadWriteLock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
