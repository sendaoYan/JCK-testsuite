/*
 * @test
 * @summary java.util.concurrent.locks.ReadWriteLock conformance: white-box (interface) +
 *          black-box (readLock/writeLock distinct Lock objects via ReentrantReadWriteLock,
 *          incl. write-holder read downgrade).
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.ReadWriteLock");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "readLock"), "readLock()");
        check(hasMethod(c, "writeLock"), "writeLock()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ReadWriteLock rwl = new ReentrantReadWriteLock();
        Lock rl = rwl.readLock();
        Lock wl = rwl.writeLock();
        check(rl instanceof Lock && wl instanceof Lock, "both are Locks");
        check(rl != wl, "read and write locks distinct");

        wl.lock();
        check(wl.tryLock(), "write lock reentrant in same thread");
        check(rl.tryLock(), "write holder can acquire read lock (downgrade)");
        rl.unlock();
        wl.unlock();
        wl.unlock();

        check(rl.tryLock(), "read lock acquired when free");
        rl.unlock();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
