/*
 * @test
 * @summary java.util.concurrent.locks.StampedLock conformance: white-box + black-box
 *          (write/read/optimistic lock, validate, stamp misuse IMSE, non-reentrancy,
 *          cross-thread exclusion and sharing, optimistic reads never block writers,
 *          read/write/optimistic conversions, and Lock/ReadWriteLock views).
 * @run main StampedLockTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class StampedLockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static Thread startDaemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
        return t;
    }

    static void await(CountDownLatch l, String what) throws InterruptedException {
        if (!l.await(5, TimeUnit.SECONDS))
            throw new AssertionError("timeout waiting for " + what);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.StampedLock");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null, "StampedLock()");
        check(hasMethod(c, "writeLock") && hasMethod(c, "tryWriteLock"), "writeLock/tryWriteLock");
        check(hasMethod(c, "writeLockInterruptibly"), "writeLockInterruptibly");
        check(hasMethod(c, "tryWriteLock", long.class, TimeUnit.class), "tryWriteLock(long,TimeUnit)");
        check(hasMethod(c, "readLock") && hasMethod(c, "tryReadLock"), "readLock/tryReadLock");
        check(hasMethod(c, "readLockInterruptibly"), "readLockInterruptibly");
        check(hasMethod(c, "tryReadLock", long.class, TimeUnit.class), "tryReadLock(long,TimeUnit)");
        check(hasMethod(c, "tryOptimisticRead") && hasMethod(c, "validate", long.class),
              "tryOptimisticRead/validate");
        check(hasMethod(c, "unlockWrite", long.class) && hasMethod(c, "unlockRead", long.class)
              && hasMethod(c, "unlock", long.class), "unlockWrite/unlockRead/unlock");
        check(hasMethod(c, "tryConvertToWriteLock", long.class)
              && hasMethod(c, "tryConvertToReadLock", long.class)
              && hasMethod(c, "tryConvertToOptimisticRead", long.class), "tryConvertTo*");
        check(hasMethod(c, "tryUnlockWrite") && hasMethod(c, "tryUnlockRead"),
              "tryUnlockWrite/tryUnlockRead");
        check(hasMethod(c, "isWriteLocked") && hasMethod(c, "isReadLocked")
              && hasMethod(c, "getReadLockCount"), "isWriteLocked/isReadLocked/getReadLockCount");
        check(hasMethod(c, "asReadLock") && hasMethod(c, "asWriteLock")
              && hasMethod(c, "asReadWriteLock"), "asReadLock/asWriteLock/asReadWriteLock");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testWriteLock() {
        StampedLock sl = new StampedLock();
        check(!sl.isWriteLocked(), "not write locked initially");
        long w = sl.writeLock();
        check(w != 0, "writeLock returns non-zero stamp");
        check(sl.isWriteLocked(), "isWriteLocked");
        // non-reentrant: same thread cannot re-acquire write (self-deadlock avoidance)
        check(sl.tryWriteLock() == 0, "tryWriteLock returns 0 while write held (non-reentrant)");
        sl.unlockWrite(w);
        check(!sl.isWriteLocked(), "not write locked after unlockWrite");
    }

    static void testReadLock() {
        StampedLock sl = new StampedLock();
        check(!sl.isReadLocked(), "not read locked initially");
        long r = sl.readLock();
        check(r != 0, "readLock returns non-zero stamp");
        check(sl.isReadLocked(), "isReadLocked");
        check(sl.getReadLockCount() == 1, "getReadLockCount 1");
        sl.unlockRead(r);
        check(!sl.isReadLocked(), "not read locked after unlockRead");
    }

    static void testReadLockMultipleHolds() {
        StampedLock sl = new StampedLock();
        long r1 = sl.readLock();
        long r2 = sl.readLock(); // same thread may hold the read lock more than once
        check(r1 != 0 && r2 != 0 && r1 != r2, "two distinct non-zero read stamps");
        check(sl.getReadLockCount() == 2, "read lock count 2");
        check(sl.isReadLocked(), "read locked");
        sl.unlockRead(r1);
        check(sl.getReadLockCount() == 1, "read lock count 1 after first unlockRead");
        sl.unlockRead(r2);
        check(sl.getReadLockCount() == 0 && !sl.isReadLocked(), "fully released");
    }

    static void testReadSharedAcrossThreads() throws Exception {
        final StampedLock sl = new StampedLock();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicBoolean childHolds = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            long r = sl.readLock();
            if (r != 0) {
                childHolds.set(true);
                entered.countDown();
                try { await(release, "release"); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { sl.unlockRead(r); }
            } else {
                entered.countDown();
            }
        });
        await(entered, "child to hold the read lock");
        check(childHolds.get(), "child acquired the read lock");
        long r = sl.readLock(); // main shares the read lock with the child
        check(r != 0, "main acquires the read lock while the child holds it");
        check(sl.getReadLockCount() == 2, "two concurrent readers");
        sl.unlockRead(r);
        release.countDown();
        t.join(5000);
        check(!t.isAlive(), "child finished");
        check(sl.getReadLockCount() == 0, "read count back to 0");
    }

    static void testOptimisticRead() {
        StampedLock sl = new StampedLock();
        long opt = sl.tryOptimisticRead();
        check(opt != 0, "tryOptimisticRead non-zero");
        check(sl.validate(opt), "validate true with no writer");
        long w = sl.writeLock();
        check(!sl.validate(opt), "validate false after a writer");
        sl.unlockWrite(w);
        long opt2 = sl.tryOptimisticRead(); // a fresh stamp validates again
        check(opt2 != 0, "new optimistic stamp non-zero after the write");
        check(sl.validate(opt2), "new optimistic stamp validates");
    }

    static void testOptimisticDoesNotBlockWriter() throws Exception {
        final StampedLock sl = new StampedLock();
        long opt = sl.tryOptimisticRead();
        check(opt != 0, "optimistic stamp outstanding");
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicBoolean writerGot = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            long w = sl.writeLock(); // must acquire immediately: optimistic reads never block
            writerGot.set(w != 0);
            entered.countDown();
            try { await(release, "release"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { if (w != 0) sl.unlockWrite(w); }
        });
        await(entered, "writer to acquire");
        check(writerGot.get(), "writer acquired while an optimistic read was outstanding");
        check(!sl.validate(opt), "the optimistic stamp is invalidated by the writer");
        release.countDown();
        t.join(5000);
        check(!t.isAlive() && !sl.isWriteLocked(), "writer released and finished");
        long opt2 = sl.tryOptimisticRead();
        check(opt2 != 0 && sl.validate(opt2), "optimistic reads work again after the write");
    }

    static void testUnlockWrongStampThrowsIMSE() {
        StampedLock sl = new StampedLock();
        expectThrows(() -> sl.unlockWrite(0L), IllegalMonitorStateException.class,
                     "unlockWrite(0) throws IMSE");
        expectThrows(() -> sl.unlockRead(0L), IllegalMonitorStateException.class,
                     "unlockRead(0) throws IMSE");
        expectThrows(() -> sl.unlock(0L), IllegalMonitorStateException.class,
                     "unlock(0) throws IMSE");
        long w = sl.writeLock();
        expectThrows(() -> sl.unlockRead(w), IllegalMonitorStateException.class,
                     "unlockRead(writeStamp) throws IMSE");
        sl.unlockWrite(w);
        long r = sl.readLock();
        expectThrows(() -> sl.unlockWrite(r), IllegalMonitorStateException.class,
                     "unlockWrite(readStamp) throws IMSE");
        sl.unlockRead(r);
        check(!sl.isReadLocked() && !sl.isWriteLocked(), "lock usable after the IMSE probes");
    }

    static void testValidateZeroStampFalse() {
        StampedLock sl = new StampedLock();
        check(!sl.validate(0L), "validate(0) false");
    }

    static void testContendedTryLocksReturnZero() throws Exception {
        final StampedLock sl = new StampedLock();
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            long w = sl.writeLock();
            held.countDown();
            try { await(release, "release"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { sl.unlockWrite(w); }
        });
        await(held, "writer to hold");
        check(sl.tryWriteLock() == 0, "tryWriteLock 0 while another thread holds write");
        check(sl.tryReadLock() == 0, "tryReadLock 0 while another thread holds write");
        try {
            check(sl.tryWriteLock(50, TimeUnit.MILLISECONDS) == 0, "timed tryWriteLock 0 while contended");
            check(sl.tryReadLock(50, TimeUnit.MILLISECONDS) == 0, "timed tryReadLock 0 while contended");
        } catch (InterruptedException e) {
            throw new AssertionError("unexpected interrupt", e);
        }
        release.countDown();
        t.join(5000);
        check(!t.isAlive(), "writer finished");
        check(sl.tryWriteLock() != 0, "tryWriteLock non-zero once free");
        sl.tryUnlockWrite();
    }

    static void testConvertReadToWriteWithOtherReaderReturnsZero() throws Exception {
        final StampedLock sl = new StampedLock();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            long r = sl.readLock();
            entered.countDown();
            try { await(release, "release"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { sl.unlockRead(r); }
        });
        await(entered, "other reader to hold");
        long mine = sl.readLock();
        check(mine != 0, "main also holds the read lock");
        check(sl.tryConvertToWriteLock(mine) == 0,
              "read->write conversion fails while another reader holds");
        check(sl.isReadLocked() && sl.getReadLockCount() == 2, "both reads still held");
        sl.unlockRead(mine);
        release.countDown();
        t.join(5000);
        check(!t.isAlive(), "other reader finished");
        // Sole reader conversion succeeds (must run after the other reader released).
        long sole = sl.readLock();
        check(sl.tryConvertToWriteLock(sole) != 0, "read->write conversion succeeds for the sole reader");
        check(sl.isWriteLocked() && !sl.isReadLocked(), "write held after conversion");
        sl.tryUnlockWrite();
    }

    static void testConvertReadToOptimistic() {
        StampedLock sl = new StampedLock();
        long r = sl.readLock();
        long o = sl.tryConvertToOptimisticRead(r);
        check(o != 0, "read->optimistic conversion succeeds for the sole reader");
        check(!sl.isReadLocked(), "read released by the conversion");
        check(sl.validate(o), "converted optimistic stamp validates");
    }

    static void testStampPredicates() {
        StampedLock sl = new StampedLock();
        long w = sl.writeLock();
        sl.unlockWrite(w);
        long r = sl.readLock();
        sl.unlockRead(r);
        long o = sl.tryOptimisticRead();
        check(o != 0, "optimistic stamp non-zero");
        // On JDK 8 there are no static stamp-classification helpers (added in JDK 10).
        check(hasMethod(StampedLock.class, "tryOptimisticRead"), "instance API present");
    }

    static void testConvertWriteToRead() {
        StampedLock sl = new StampedLock();
        long w = sl.writeLock();
        check(sl.isWriteLocked(), "write held before convert");
        long r = sl.tryConvertToReadLock(w);
        check(r != 0, "write->read convert succeeds");
        check(!sl.isWriteLocked(), "write released after convert");
        check(sl.isReadLocked(), "read held after convert");
        sl.unlockRead(r);
        check(!sl.isReadLocked(), "read released");
    }

    static void testConvertReadToWrite() {
        StampedLock sl = new StampedLock();
        long r = sl.readLock();
        check(sl.isReadLocked(), "read held before convert");
        long w = sl.tryConvertToWriteLock(r);
        check(w != 0, "read->write convert succeeds when sole reader");
        check(!sl.isReadLocked(), "read released after convert");
        check(sl.isWriteLocked(), "write held after convert");
        sl.unlockWrite(w);
    }

    static void testConvertWriteToOptimistic() {
        StampedLock sl = new StampedLock();
        long w = sl.writeLock();
        long o = sl.tryConvertToOptimisticRead(w);
        check(o != 0, "write->optimistic convert succeeds");
        check(!sl.isWriteLocked(), "write released after convert");
        check(sl.validate(o), "converted optimistic stamp validates");
    }

    static void testUnlockGeneric() {
        StampedLock sl = new StampedLock();
        long w = sl.writeLock();
        sl.unlock(w);
        check(!sl.isWriteLocked(), "unlock(long) releases write");
        long r = sl.readLock();
        sl.unlock(r);
        check(!sl.isReadLocked(), "unlock(long) releases read");
    }

    static void testLockViews() {
        StampedLock sl = new StampedLock();
        Lock writeView = sl.asWriteLock();
        Lock readView = sl.asReadLock();
        ReadWriteLock rwView = sl.asReadWriteLock();

        writeView.lock();
        check(sl.isWriteLocked(), "asWriteLock().lock() writes");
        writeView.unlock();
        check(!sl.isWriteLocked(), "asWriteLock().unlock() releases");

        readView.lock();
        check(sl.isReadLocked(), "asReadLock().lock() reads");
        readView.unlock();
        check(!sl.isReadLocked(), "asReadLock().unlock() releases");

        check(rwView.readLock() != null && rwView.writeLock() != null, "asReadWriteLock views");
    }

    static void testTryLocksWhenFree() {
        StampedLock sl = new StampedLock();
        long w = sl.tryWriteLock();
        check(w != 0, "tryWriteLock non-zero when free");
        sl.unlockWrite(w);
        long r = sl.tryReadLock();
        check(r != 0, "tryReadLock non-zero when free");
        sl.unlockRead(r);
        long o = sl.tryOptimisticRead();
        check(o != 0, "tryOptimisticRead non-zero when free");
    }

    static void testToStringState() {
        StampedLock sl = new StampedLock();
        check(sl.toString().contains("Unlocked"), "toString unlocked");
        long w = sl.writeLock();
        check(sl.toString().contains("Write-locked"), "toString write-locked");
        sl.unlockWrite(w);
        long r = sl.readLock();
        check(sl.toString().contains("Read-locks"), "toString read-locked");
        sl.unlockRead(r);
        check(sl.toString().contains("Unlocked"), "toString unlocked again");
    }

    static void testInterruptiblyVariants() throws Exception {
        StampedLock sl = new StampedLock();
        long wi = sl.writeLockInterruptibly();
        check(wi != 0 && sl.isWriteLocked(), "writeLockInterruptibly acquires when free");
        sl.unlockWrite(wi);
        long ri = sl.readLockInterruptibly();
        check(ri != 0 && sl.isReadLocked(), "readLockInterruptibly acquires when free");
        sl.unlockRead(ri);

        // Pending interrupt: lockInterruptibly throws at once instead of blocking.
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean caught = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            Thread.currentThread().interrupt();
            try {
                sl.writeLockInterruptibly();
            } catch (InterruptedException expected) {
                caught.set(true);
            }
            done.countDown();
        });
        await(done, "child to finish");
        t.join(5000);
        check(caught.get(), "writeLockInterruptibly throws on a pending interrupt");
        check(!sl.isWriteLocked(), "write not acquired by the interrupted thread");
    }

    static void testBlackBox() throws Exception {
        StampedLock sl = new StampedLock();
        check(!sl.tryUnlockWrite(), "tryUnlockWrite false when not write locked");
        check(!sl.tryUnlockRead(), "tryUnlockRead false when not read locked");

        long w = sl.writeLock();
        check(sl.tryUnlockWrite(), "tryUnlockWrite true when write locked");
        check(!sl.isWriteLocked(), "write released via tryUnlockWrite");
        long r = sl.readLock();
        check(sl.tryUnlockRead(), "tryUnlockRead true when read locked");
        check(!sl.isReadLocked(), "read released via tryUnlockRead");

        long wi = sl.writeLockInterruptibly();
        check(wi != 0 && sl.isWriteLocked(), "writeLockInterruptibly acquires");
        sl.unlockWrite(wi);
        long ri = sl.readLockInterruptibly();
        check(ri != 0 && sl.isReadLocked(), "readLockInterruptibly acquires");
        sl.unlockRead(ri);

        check(sl.toString() != null && sl.toString().length() > 0, "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWriteLock();
        testReadLock();
        testReadLockMultipleHolds();
        testReadSharedAcrossThreads();
        testOptimisticRead();
        testOptimisticDoesNotBlockWriter();
        testUnlockWrongStampThrowsIMSE();
        testValidateZeroStampFalse();
        testContendedTryLocksReturnZero();
        testConvertReadToWriteWithOtherReaderReturnsZero();
        testConvertReadToOptimistic();
        testStampPredicates();
        testConvertWriteToRead();
        testConvertReadToWrite();
        testConvertWriteToOptimistic();
        testUnlockGeneric();
        testLockViews();
        testTryLocksWhenFree();
        testToStringState();
        testInterruptiblyVariants();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
