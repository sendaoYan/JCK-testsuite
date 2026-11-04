/*
 * @test
 * @summary java.util.concurrent.locks.StampedLock conformance: white-box + black-box
 *          (write/read/optimistic lock, validate, stamp predicates, non-reentrancy,
 *          read/write/optimistic conversions, and Lock/ReadWriteLock views).
 * @run main StampedLockTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class StampedLockTest {
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
        Class<?> c = Class.forName("java.util.concurrent.locks.StampedLock");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null, "StampedLock()");
        check(hasMethod(c, "writeLock") && hasMethod(c, "tryWriteLock"), "writeLock/tryWriteLock");
        check(hasMethod(c, "readLock") && hasMethod(c, "tryReadLock"), "readLock/tryReadLock");
        check(hasMethod(c, "tryOptimisticRead") && hasMethod(c, "validate", long.class),
              "tryOptimisticRead/validate");
        check(hasMethod(c, "unlockWrite", long.class) && hasMethod(c, "unlockRead", long.class)
              && hasMethod(c, "unlock", long.class), "unlockWrite/unlockRead/unlock");
        check(hasMethod(c, "tryConvertToWriteLock", long.class)
              && hasMethod(c, "tryConvertToReadLock", long.class)
              && hasMethod(c, "tryConvertToOptimisticRead", long.class), "tryConvertTo*");
        check(hasMethod(c, "isWriteLocked") && hasMethod(c, "isReadLocked")
              && hasMethod(c, "getReadLockCount"), "isWriteLocked/isReadLocked/getReadLockCount");
        check(hasMethod(c, "asReadLock") && hasMethod(c, "asWriteLock")
              && hasMethod(c, "asReadWriteLock"), "asReadLock/asWriteLock/asReadWriteLock");
        check(hasMethod(c, "isWriteLockStamp", long.class) && hasMethod(c, "isReadLockStamp", long.class)
              && hasMethod(c, "isLockStamp", long.class) && hasMethod(c, "isOptimisticReadStamp", long.class),
              "static stamp predicates");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testWriteLock() {
        StampedLock sl = new StampedLock();
        check(!sl.isWriteLocked(), "not write locked initially");
        long w = sl.writeLock();
        check(w != 0, "writeLock returns non-zero stamp");
        check(StampedLock.isWriteLockStamp(w), "isWriteLockStamp");
        check(StampedLock.isLockStamp(w), "isLockStamp");
        check(sl.isWriteLocked(), "isWriteLocked");
        // non-reentrant: same thread cannot re-acquire write
        check(sl.tryWriteLock() == 0, "tryWriteLock returns 0 while write held (non-reentrant)");
        sl.unlockWrite(w);
        check(!sl.isWriteLocked(), "not write locked after unlockWrite");
    }

    static void testReadLock() {
        StampedLock sl = new StampedLock();
        check(!sl.isReadLocked(), "not read locked initially");
        long r = sl.readLock();
        check(r != 0, "readLock returns non-zero stamp");
        check(StampedLock.isReadLockStamp(r), "isReadLockStamp");
        check(StampedLock.isLockStamp(r), "isLockStamp");
        check(sl.isReadLocked(), "isReadLocked");
        check(sl.getReadLockCount() == 1, "getReadLockCount 1");
        sl.unlockRead(r);
        check(!sl.isReadLocked(), "not read locked after unlockRead");
    }

    static void testOptimisticRead() {
        StampedLock sl = new StampedLock();
        long opt = sl.tryOptimisticRead();
        check(opt != 0, "tryOptimisticRead non-zero");
        check(StampedLock.isOptimisticReadStamp(opt), "isOptimisticReadStamp");
        check(!StampedLock.isLockStamp(opt), "optimistic stamp is not lock stamp");
        check(sl.validate(opt), "validate true with no writer");
        long w = sl.writeLock();
        check(!sl.validate(opt), "validate false after a writer");
        sl.unlockWrite(w);
    }

    static void testStampPredicates() {
        StampedLock sl = new StampedLock();
        long w = sl.writeLock();
        check(StampedLock.isWriteLockStamp(w) && !StampedLock.isReadLockStamp(w)
              && !StampedLock.isOptimisticReadStamp(w), "write stamp classification");
        sl.unlockWrite(w);
        long r = sl.readLock();
        check(!StampedLock.isWriteLockStamp(r) && StampedLock.isReadLockStamp(r)
              && !StampedLock.isOptimisticReadStamp(r), "read stamp classification");
        sl.unlockRead(r);
        long o = sl.tryOptimisticRead();
        check(StampedLock.isOptimisticReadStamp(o) && !StampedLock.isWriteLockStamp(o)
              && !StampedLock.isReadLockStamp(o), "optimistic stamp classification");
    }

    static void testConvertWriteToRead() {
        StampedLock sl = new StampedLock();
        long w = sl.writeLock();
        check(sl.isWriteLocked(), "write held before convert");
        long r = sl.tryConvertToReadLock(w);
        check(r != 0, "write->read convert succeeds");
        check(!sl.isWriteLocked(), "write released after convert");
        check(sl.isReadLocked(), "read held after convert");
        check(StampedLock.isReadLockStamp(r), "converted stamp is read stamp");
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
        check(StampedLock.isWriteLockStamp(w), "converted stamp is write stamp");
        sl.unlockWrite(w);
    }

    static void testConvertWriteToOptimistic() {
        StampedLock sl = new StampedLock();
        long w = sl.writeLock();
        long o = sl.tryConvertToOptimisticRead(w);
        check(o != 0, "write->optimistic convert succeeds");
        check(!sl.isWriteLocked(), "write released after convert");
        check(StampedLock.isOptimisticReadStamp(o), "converted stamp is optimistic");
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
        testOptimisticRead();
        testStampPredicates();
        testConvertWriteToRead();
        testConvertReadToWrite();
        testConvertWriteToOptimistic();
        testUnlockGeneric();
        testLockViews();
        testTryLocksWhenFree();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
