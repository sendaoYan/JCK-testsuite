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

    static void testLockPairStableIdentity() {
        ReadWriteLock rwl = new ReentrantReadWriteLock();
        check(rwl.readLock() == rwl.readLock(), "readLock() returns the same instance");
        check(rwl.writeLock() == rwl.writeLock(), "writeLock() returns the same instance");
        check(rwl.readLock() != rwl.writeLock(), "read and write pairs are distinct objects");
    }

    static void testWriteExcludesReaders() throws Exception {
        final ReadWriteLock rwl = new ReentrantReadWriteLock();
        rwl.writeLock().lock(); // main holds write
        final java.util.concurrent.atomic.AtomicBoolean gotRead =
            new java.util.concurrent.atomic.AtomicBoolean(true);
        Thread t = new Thread(() -> gotRead.set(rwl.readLock().tryLock()));
        t.setDaemon(true);
        t.start();
        t.join(5000);
        check(!t.isAlive() && !gotRead.get(), "reader tryLock false while another thread holds write");
        rwl.writeLock().unlock();
    }

    static void testWriteExcludesWriters() throws Exception {
        final ReadWriteLock rwl = new ReentrantReadWriteLock();
        rwl.writeLock().lock(); // main holds write
        final java.util.concurrent.atomic.AtomicBoolean gotWrite =
            new java.util.concurrent.atomic.AtomicBoolean(true);
        Thread t = new Thread(() -> gotWrite.set(rwl.writeLock().tryLock()));
        t.setDaemon(true);
        t.start();
        t.join(5000);
        check(!t.isAlive() && !gotWrite.get(), "writer tryLock false while another thread holds write");
        rwl.writeLock().unlock();
    }

    public static void main(String[] args) throws Exception {
        testReadWriteLock();
        testLockPairStableIdentity();
        testWriteExcludesReaders();
        testWriteExcludesWriters();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
