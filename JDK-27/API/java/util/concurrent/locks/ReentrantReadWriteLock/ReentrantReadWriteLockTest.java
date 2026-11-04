/*
 * @test
 * @summary java.util.concurrent.locks.ReentrantReadWriteLock conformance: white-box +
 *          black-box (write exclusivity, read sharing, reentrancy, downgrade, blocked
 *          upgrade, fairness, and ReadLock/WriteLock nested Lock behavior incl. their
 *          newCondition contract).
 * @run main ReentrantReadWriteLockTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockTest {
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

    // Exposes the protected introspection methods for the black-box harness.
    static class ExposedRRWL extends ReentrantReadWriteLock {
        Thread owner() { return getOwner(); }
        java.util.Collection<Thread> queuedThreads() { return getQueuedThreads(); }
        java.util.Collection<Thread> queuedReaderThreads() { return getQueuedReaderThreads(); }
        java.util.Collection<Thread> queuedWriterThreads() { return getQueuedWriterThreads(); }
        java.util.Collection<Thread> waitingThreads(Condition c) { return getWaitingThreads(c); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.ReentrantReadWriteLock");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(ReadWriteLock.class.isAssignableFrom(c), "implements ReadWriteLock");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null, "ReentrantReadWriteLock()");
        check(c.getConstructor(boolean.class) != null, "ReentrantReadWriteLock(boolean)");
        check(hasMethod(c, "readLock") && hasMethod(c, "writeLock"), "readLock/writeLock");
        check(hasMethod(c, "isFair"), "isFair");
        check(hasMethod(c, "getReadLockCount") && hasMethod(c, "getReadHoldCount"),
              "getReadLockCount/getReadHoldCount");
        check(hasMethod(c, "isWriteLocked") && hasMethod(c, "isWriteLockedByCurrentThread"),
              "isWriteLocked/isWriteLockedByCurrentThread");
        check(hasMethod(c, "getWriteHoldCount"), "getWriteHoldCount");

        Class<?> read = Class.forName("java.util.concurrent.locks.ReentrantReadWriteLock$ReadLock");
        Class<?> write = Class.forName("java.util.concurrent.locks.ReentrantReadWriteLock$WriteLock");
        check(Lock.class.isAssignableFrom(read), "ReadLock implements Lock");
        check(Lock.class.isAssignableFrom(write), "WriteLock implements Lock");
        check(hasMethod(read, "lock") && hasMethod(read, "unlock") && hasMethod(read, "newCondition"),
              "ReadLock lock/unlock/newCondition");
        check(hasMethod(write, "isHeldByCurrentThread") && hasMethod(write, "getHoldCount"),
              "WriteLock isHeldByCurrentThread/getHoldCount");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testWriteExclusivityAndReentrancy() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        wl.lock();
        check(rwl.isWriteLocked(), "write locked");
        check(rwl.isWriteLockedByCurrentThread(), "write held by current thread");
        check(wl.isHeldByCurrentThread(), "WriteLock.isHeldByCurrentThread");
        check(rwl.getWriteHoldCount() == 1, "write hold count 1");
        wl.lock(); // reentrant write
        check(wl.getHoldCount() == 2, "WriteLock.getHoldCount 2 reentrant");
        check(rwl.getWriteHoldCount() == 2, "getWriteHoldCount 2");
        wl.unlock();
        wl.unlock();
        check(!rwl.isWriteLocked(), "write released");
    }

    static void testReadSharingAndReentrancy() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        rl.lock();
        check(rwl.getReadLockCount() == 1, "read lock count 1");
        check(rl.tryLock(), "read lock reentrant tryLock true");
        check(rwl.getReadHoldCount() == 2, "read hold count 2");
        check(rwl.getReadLockCount() == 2, "read lock count 2");
        rl.unlock();
        rl.unlock();
        check(rwl.getReadHoldCount() == 0, "read hold count 0");
        check(rwl.getReadLockCount() == 0, "read lock count 0");
    }

    static void testReadSharedAcrossThreads() throws Exception {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        rl.lock(); // main holds read
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean acquired = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            boolean ok = rl.tryLock(); // readers can share
            acquired.set(ok);
            if (ok) rl.unlock();
            done.countDown();
        });
        done.await();
        check(acquired.get(), "read lock shareable across threads");
        rl.unlock();
    }

    static void testDowngrade() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        wl.lock();
        check(rl.tryLock(), "write holder can acquire read lock (downgrade)");
        wl.unlock(); // release write, keep read
        check(!rwl.isWriteLocked(), "write released");
        check(rwl.getReadLockCount() == 1, "read still held after downgrade");
        rl.unlock();
    }

    static void testBlockedUpgrade() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        rl.lock();
        check(!wl.tryLock(), "cannot upgrade read -> write while holding read");
        check(!rwl.isWriteLocked(), "write not acquired during upgrade attempt");
        rl.unlock();
        check(wl.tryLock(), "write acquire succeeds after read released");
        wl.unlock();
    }

    static void testReadLockNewConditionThrows() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        expectThrows(rl::newCondition, UnsupportedOperationException.class,
                     "ReadLock.newCondition throws UnsupportedOperationException");
    }

    static void testWriteLockConditionAwaitSignal() throws Exception {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        Condition cond = wl.newCondition();
        check(cond != null, "WriteLock.newCondition non-null");
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean woken = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            wl.lock();
            try {
                ready.countDown();
                cond.await();
                woken.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                wl.unlock();
                done.countDown();
            }
        });
        ready.await();
        wl.lock(); // blocks until waiter released write lock inside await()
        try {
            cond.signal();
        } finally {
            wl.unlock();
        }
        done.await();
        check(woken.get(), "WriteLock condition signal wakes waiter");
    }

    static void testFairness() {
        check(!new ReentrantReadWriteLock().isFair(), "default non-fair");
        check(new ReentrantReadWriteLock(true).isFair(), "fair constructor");
        check(!new ReentrantReadWriteLock(false).isFair(), "non-fair constructor");
    }

    static void testReadLockTryLockTimeout() throws Exception {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        check(rl.tryLock(1, TimeUnit.NANOSECONDS), "read tryLock(timeout) when free");
        rl.unlock();
        check(rwl.writeLock().tryLock(1, TimeUnit.NANOSECONDS), "write tryLock(timeout) when free");
        rwl.writeLock().unlock();
    }

    // ---- Part 3: deepened contract tests ----
    static void testReadSharedBothThreadsHoldSimultaneously() throws Exception {
        final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        final ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            rl.lock(); // child holds the read lock
            entered.countDown();
            try { await(release, "release"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { rl.unlock(); }
        });
        await(entered, "child to hold the read lock");
        // Bidirectional deterministic proof of read sharing: while the child holds the
        // read lock, main acquires it too.
        check(rl.tryLock(), "main acquires the read lock while the child holds it");
        check(rwl.getReadLockCount() == 2, "two read holders simultaneously");
        rl.unlock();
        release.countDown();
        t.join(5000);
        check(!t.isAlive(), "child finished");
        check(rwl.getReadLockCount() == 0, "read count back to 0");
    }

    static void testReadExcludesWriters() throws Exception {
        final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        rwl.readLock().lock(); // main holds read
        final AtomicBoolean gotWrite = new AtomicBoolean(true);
        Thread t = startDaemon(() -> gotWrite.set(rwl.writeLock().tryLock()));
        t.join(5000);
        check(!t.isAlive() && !gotWrite.get(), "writer tryLock false while another thread holds read");
        rwl.readLock().unlock();
    }

    static void testWriteExcludesReaders() throws Exception {
        final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        rwl.writeLock().lock(); // main holds write
        final AtomicBoolean gotRead = new AtomicBoolean(true);
        Thread t = startDaemon(() -> gotRead.set(rwl.readLock().tryLock()));
        t.join(5000);
        check(!t.isAlive() && !gotRead.get(), "reader tryLock false while another thread holds write");
        rwl.writeLock().unlock();
    }

    static void testWriteExcludesWriters() throws Exception {
        final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        rwl.writeLock().lock(); // main holds write
        final AtomicBoolean gotWrite = new AtomicBoolean(true);
        Thread t = startDaemon(() -> gotWrite.set(rwl.writeLock().tryLock()));
        t.join(5000);
        check(!t.isAlive() && !gotWrite.get(), "second writer tryLock false while another thread holds write");
        rwl.writeLock().unlock();
    }

    static void testHoldCountMatrix() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        // Unheld state
        check(rwl.getReadLockCount() == 0 && rwl.getReadHoldCount() == 0,
              "read counts 0 when nothing held");
        check(rwl.getWriteHoldCount() == 0 && !rwl.isWriteLocked(),
              "write counts 0 when nothing held");
        // Write held: read counts still 0
        rwl.writeLock().lock();
        check(rwl.getReadLockCount() == 0 && rwl.getReadHoldCount() == 0,
              "read counts 0 while only write is held");
        check(rwl.getWriteHoldCount() == 1, "write hold 1");
        // Write + reentrant read x2 (full hold state)
        rwl.readLock().lock();
        rwl.readLock().lock();
        check(rwl.getWriteHoldCount() == 1, "write hold still 1 with reads held");
        check(rwl.getReadLockCount() == 2, "read lock count 2");
        check(rwl.getReadHoldCount() == 2, "read hold count 2 (reentrant)");
        check(rwl.isWriteLocked() && rwl.isWriteLockedByCurrentThread(), "write still held");
        rwl.writeLock().unlock(); // downgrade path leaves the reads
        check(!rwl.isWriteLocked() && rwl.getWriteHoldCount() == 0, "write released");
        check(rwl.getReadLockCount() == 2 && rwl.getReadHoldCount() == 2, "reads survive the write release");
        rwl.readLock().unlock();
        rwl.readLock().unlock();
        check(rwl.getReadLockCount() == 0 && rwl.getReadHoldCount() == 0, "all counts back to 0");
    }

    static void testToStringState() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        check(rwl.toString().contains("Write locks = 0") && rwl.toString().contains("Read locks = 0"),
              "toString reports no locks held");
        rwl.writeLock().lock();
        check(rwl.toString().contains("Write locks = 1"), "toString reports the write lock");
        rwl.readLock().lock();
        rwl.readLock().lock();
        check(rwl.toString().contains("Read locks = 2"), "toString reports both read holds");
        rwl.writeLock().unlock();
        check(rwl.toString().contains("Write locks = 0"), "toString write back to 0");
        rwl.readLock().unlock();
        rwl.readLock().unlock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        check(rl.toString().contains("Read locks = 0"), "ReadLock.toString initial");
        rl.lock();
        check(rl.toString().contains("Read locks = 1"), "ReadLock.toString held");
        rl.unlock();
        check(wl.toString().contains("Unlocked"), "WriteLock.toString unlocked");
        wl.lock();
        check(wl.toString().contains("Locked by thread"), "WriteLock.toString locked");
        wl.unlock();
    }

    static void testUnlockWithoutHold() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock rl = rwl.readLock();
        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        expectThrows(rl::unlock, IllegalMonitorStateException.class,
                     "ReadLock.unlock without hold throws IMSE");
        expectThrows(wl::unlock, IllegalMonitorStateException.class,
                     "WriteLock.unlock without hold throws IMSE");
        // Over-release: read twice, release three times -> IMSE on the third.
        rl.lock();
        rl.lock();
        rl.unlock();
        rl.unlock();
        expectThrows(rl::unlock, IllegalMonitorStateException.class,
                     "ReadLock.unlock beyond hold count throws IMSE");
    }

    static void testBlackBox() {
        ExposedRRWL rwl = new ExposedRRWL();
        check(rwl.owner() == null, "getOwner null when unlocked");

        check(!rwl.hasQueuedThreads(), "hasQueuedThreads false");
        check(rwl.getQueueLength() == 0, "getQueueLength 0");
        check(rwl.queuedThreads().isEmpty(), "getQueuedThreads empty");
        check(rwl.queuedReaderThreads().isEmpty(), "getQueuedReaderThreads empty");
        check(rwl.queuedWriterThreads().isEmpty(), "getQueuedWriterThreads empty");
        Thread t = new Thread(() -> {});
        check(!rwl.hasQueuedThread(t), "hasQueuedThread false for non-queued thread");

        ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();
        wl.lock();
        check(rwl.owner() == Thread.currentThread(), "getOwner current thread when write locked");

        Condition cond = wl.newCondition();
        check(!rwl.hasWaiters(cond), "hasWaiters false");
        check(rwl.getWaitQueueLength(cond) == 0, "getWaitQueueLength 0");
        check(rwl.waitingThreads(cond).isEmpty(), "getWaitingThreads empty");

        check(rwl.toString() != null && rwl.toString().length() > 0, "toString non-null");

        wl.unlock();
        check(rwl.owner() == null, "getOwner null after write unlock");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWriteExclusivityAndReentrancy();
        testReadSharingAndReentrancy();
        testReadSharedAcrossThreads();
        testDowngrade();
        testBlockedUpgrade();
        testReadLockNewConditionThrows();
        testWriteLockConditionAwaitSignal();
        testFairness();
        testReadLockTryLockTimeout();
        testBlackBox();
        testReadSharedBothThreadsHoldSimultaneously();
        testReadExcludesWriters();
        testWriteExcludesReaders();
        testWriteExcludesWriters();
        testHoldCountMatrix();
        testToStringState();
        testUnlockWithoutHold();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
