/*
 * @test
 * @summary java.util.concurrent.Semaphore conformance: white-box structure + black-box
 *          behavior (acquire/release/tryAcquire/permits/drainPermits/fairness, bulk
 *          acquire/release, acquireUninterruptibly with interrupt-status retention,
 *          timed tryAcquire released during wait, reducePermits probe, negative
 *          arguments/permits, serialization round-trip).
 * @run main SemaphoreTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SemaphoreTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                    + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.Semaphore");
        check(!c.isInterface(), "Semaphore is a class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        hasMethod(c, "acquire");
        hasMethod(c, "acquire", int.class);
        hasMethod(c, "acquireUninterruptibly");
        hasMethod(c, "release");
        hasMethod(c, "release", int.class);
        hasMethod(c, "tryAcquire");
        hasMethod(c, "tryAcquire", long.class, TimeUnit.class);
        hasMethod(c, "availablePermits");
        hasMethod(c, "drainPermits");
        hasMethod(c, "isFair");
        hasMethod(c, "hasQueuedThreads");
        hasMethod(c, "getQueueLength");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Semaphore sem = new Semaphore(2);
        check(sem.availablePermits() == 2, "initial permits");
        sem.acquire();
        check(sem.availablePermits() == 1, "acquire decrements");
        check(sem.tryAcquire(), "tryAcquire succeeds when permits available");
        check(sem.availablePermits() == 0, "permits exhausted");
        check(!sem.tryAcquire(), "tryAcquire fails when empty");
        sem.release();
        check(sem.availablePermits() == 1, "release increments");

        // drainPermits
        Semaphore d = new Semaphore(3);
        check(d.drainPermits() == 3, "drainPermits returns all available permits");
        check(d.availablePermits() == 0, "drainPermits leaves zero permits");

        // bulk acquire/release
        Semaphore b = new Semaphore(5);
        b.acquire(2);
        check(b.availablePermits() == 3, "acquire(2) decrements by two");
        b.release(2);
        check(b.availablePermits() == 5, "release(2) increments by two");

        // tryAcquire(int) bulk form
        Semaphore n = new Semaphore(2);
        check(n.tryAcquire(2), "tryAcquire(2) succeeds with enough permits");
        check(n.availablePermits() == 0, "tryAcquire(2) exhausts");
        check(!n.tryAcquire(1), "tryAcquire(1) fails when exhausted");

        // acquireUninterruptibly on a free semaphore
        Semaphore ui = new Semaphore(1);
        ui.acquireUninterruptibly();
        check(ui.availablePermits() == 0, "acquireUninterruptibly decrements");

        // fairness flag
        Semaphore fair = new Semaphore(1, true);
        check(fair.isFair(), "fair semaphore isFair");
        Semaphore nonFair = new Semaphore(1, false);
        check(!nonFair.isFair(), "non-fair semaphore not isFair");

        // timed tryAcquire on an empty semaphore returns false
        Semaphore z = new Semaphore(0);
        check(!z.tryAcquire(50, TimeUnit.MILLISECONDS), "timed tryAcquire returns false on empty semaphore");
        check(!z.hasQueuedThreads(), "no queued threads");
        check(z.getQueueLength() == 0, "queue length zero");

        // negative initial permits are legal (acquire must wait for a release)
        Semaphore neg = new Semaphore(-1);
        check(neg.availablePermits() == -1, "negative permits constructor legal, availablePermits -1");

        // negative bulk acquire/release rejected; zero is a legal no-op
        expectThrows(IllegalArgumentException.class, () -> new Semaphore(1).acquire(-1), "acquire(-1) rejected");
        expectThrows(IllegalArgumentException.class, () -> new Semaphore(1).release(-1), "release(-1) rejected");
        new Semaphore(1).acquire(0);
        new Semaphore(1).release(0);
        passed += 2; // acquire(0)/release(0) are legal no-ops
    }

    @SuppressWarnings("unchecked")
    static void testSerialization() throws Exception {
        Semaphore s = new Semaphore(3);
        s.acquire();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(s);
        }
        Semaphore copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            copy = (Semaphore) ois.readObject();
        }
        check(copy.availablePermits() == 2, "serialization preserves availablePermits");
        check(copy.isFair() == s.isFair(), "serialization preserves fairness");
        copy.acquire();
        check(copy.availablePermits() == 1, "deserialized instance remains operational");
    }

    // ---- Part 3: contract deepening (appended) ----

    static void testWhiteBoxExtra() throws Exception {
        Class<?> c = cls("java.util.concurrent.Semaphore");
        hasMethod(c, "acquireUninterruptibly", int.class);
        hasMethod(c, "tryAcquire", int.class);
        hasMethod(c, "tryAcquire", int.class, long.class, TimeUnit.class);
        try {
            c.getDeclaredMethod("reducePermits", int.class); // protected member
            check(true, "protected reducePermits(int) is declared");
        } catch (NoSuchMethodException e) {
            check(false, "reducePermits(int) missing");
        }
    }

    // negative bulk arguments are rejected by every form; zero is a no-op
    static void testNegativeArguments() throws Exception {
        try {
            new Semaphore(1).acquire(-1);
            check(false, "acquire(-1) should throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() == null, "acquire(-1) IAE carries no detail message");
        }
        expectThrows(IllegalArgumentException.class, () -> new Semaphore(1).release(-1), "release(-1) rejected");
        expectThrows(IllegalArgumentException.class, () -> new Semaphore(1).tryAcquire(-1), "tryAcquire(-1) rejected");
        expectThrows(IllegalArgumentException.class, () -> new Semaphore(1).acquireUninterruptibly(-1),
                "acquireUninterruptibly(-1) rejected");
        expectThrows(IllegalArgumentException.class,
                () -> new Semaphore(1).tryAcquire(-1, 1, TimeUnit.MILLISECONDS), "tryAcquire(-1, timeout) rejected");
        Semaphore z = new Semaphore(2);
        z.acquire(0);
        z.release(0);
        z.acquireUninterruptibly(0);
        check(z.availablePermits() == 2, "acquire(0)/release(0)/acquireUninterruptibly(0) are no-ops");
        check(z.tryAcquire(0), "tryAcquire(0) succeeds without consuming permits");
        check(z.availablePermits() == 2, "tryAcquire(0) leaves the permit count unchanged");
    }

    // timed tryAcquire: immediate success, timeout failure, success once released during the wait
    static void testTimedTryAcquire() throws Exception {
        Semaphore s = new Semaphore(1);
        check(s.tryAcquire(1, TimeUnit.SECONDS), "timed tryAcquire succeeds immediately when a permit is free");
        check(!s.tryAcquire(50, TimeUnit.MILLISECONDS), "timed tryAcquire returns false on timeout when exhausted");

        Semaphore w = new Semaphore(1);
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                w.acquire();
                entered.countDown();
                release.await(5, TimeUnit.SECONDS);
                w.release();
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                done.countDown();
            }
        });
        t.setDaemon(true);
        t.start();
        check(entered.await(5, TimeUnit.SECONDS), "worker acquired the permit");
        check(w.availablePermits() == 0, "permit held by the worker");
        release.countDown();
        check(w.tryAcquire(5, TimeUnit.SECONDS), "timed tryAcquire succeeds once the permit is released");
        check(done.await(5, TimeUnit.SECONDS), "worker finished");
        t.join(5000);
        check(!t.isAlive(), "worker thread finished");
    }

    // interrupting an acquire: InterruptedException, permit left for others
    static void testAcquireInterrupted() throws Exception {
        final Semaphore s = new Semaphore(0);
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean();
        Thread t = new Thread(() -> {
            entered.countDown();
            try {
                s.acquire();
            } catch (InterruptedException e) {
                interrupted.set(true);
            } finally {
                done.countDown();
            }
        });
        t.setDaemon(true);
        t.start();
        check(entered.await(5, TimeUnit.SECONDS), "worker started waiting for a permit");
        t.interrupt();
        check(done.await(5, TimeUnit.SECONDS), "interrupted acquire returns promptly (bounded)");
        t.join(5000);
        check(!t.isAlive(), "interrupted worker finished");
        check(interrupted.get(), "acquire throws InterruptedException when interrupted");
        check(s.availablePermits() == 0, "no permit was consumed by the interrupted acquire");
    }

    // acquireUninterruptibly: the interrupt does not abort the wait; the interrupt
    // status is still set after the acquire completes
    static void testAcquireUninterruptiblyInterrupt() throws Exception {
        final Semaphore s = new Semaphore(0);
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean acquired = new AtomicBoolean();
        final AtomicBoolean stillInterrupted = new AtomicBoolean();
        Thread t = new Thread(() -> {
            entered.countDown();
            s.acquireUninterruptibly(); // an interrupt while waiting must not abort it
            acquired.set(true);
            stillInterrupted.set(Thread.currentThread().isInterrupted());
            done.countDown();
        });
        t.setDaemon(true);
        t.start();
        check(entered.await(5, TimeUnit.SECONDS), "worker started waiting for a permit");
        t.interrupt();
        check(!acquired.get(), "an interrupt alone does not grant a permit");
        s.release();
        check(done.await(5, TimeUnit.SECONDS), "worker acquired the permit despite the interrupt (bounded)");
        t.join(5000);
        check(!t.isAlive(), "worker thread finished");
        check(acquired.get(), "acquireUninterruptibly completed after the release");
        check(stillInterrupted.get(), "interrupt status is preserved by acquireUninterruptibly");
        check(s.availablePermits() == 0, "permit consumed by the worker");
    }

    // reducePermits is protected: a subclass probe may drive permits negative
    static class ReducibleSemaphore extends Semaphore {
        ReducibleSemaphore(int permits) { super(permits); }
        void reduce(int reduction) { reducePermits(reduction); }
    }

    static void testReducePermits() {
        ReducibleSemaphore s = new ReducibleSemaphore(2);
        s.reduce(3);
        check(s.availablePermits() == -1, "reducePermits may drive the permit count negative (legal)");
        s.release(4);
        check(s.availablePermits() == 3, "releases compensate reduced permits");
        try {
            new ReducibleSemaphore(1).reduce(-1);
            check(false, "negative reduction should throw");
        } catch (IllegalArgumentException e) {
            check(true, "reducePermits(negative) throws IAE");
        }
    }

    // releasing without acquiring may exceed the initial permit count
    static void testReleaseBeyondInitial() {
        Semaphore s = new Semaphore(1);
        s.release();
        check(s.availablePermits() == 2, "release() without acquire exceeds the initial count");
        s.release(3);
        check(s.availablePermits() == 5, "release(n) may push permits well beyond the initial count");
    }

    // negative initial permits: legal; acquires must wait for releases to compensate
    static void testNegativePermits() throws Exception {
        Semaphore neg = new Semaphore(-1);
        check(neg.availablePermits() == -1, "negative initial permits are legal");
        check(!neg.tryAcquire(50, TimeUnit.MILLISECONDS), "acquire waits until releases compensate the deficit");
        neg.release(2);
        check(neg.availablePermits() == 1, "two releases compensate one negative permit");
    }

    static void testToStringAndDrain() {
        check(new Semaphore(5).toString().contains("Permits = 5"), "toString reports the permit count");
        Semaphore f = new Semaphore(2, true);
        check(f.drainPermits() == 2, "fair drainPermits returns all available permits");
        check(f.availablePermits() == 0, "fair drainPermits leaves zero permits");
        Semaphore neg = new Semaphore(-3, false);
        check(neg.drainPermits() == -3, "drainPermits on negative permits reports the negative count");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWhiteBoxExtra();
        testBlackBox();
        testNegativeArguments();
        testTimedTryAcquire();
        testAcquireInterrupted();
        testAcquireUninterruptiblyInterrupt();
        testReducePermits();
        testReleaseBeyondInitial();
        testNegativePermits();
        testToStringAndDrain();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
