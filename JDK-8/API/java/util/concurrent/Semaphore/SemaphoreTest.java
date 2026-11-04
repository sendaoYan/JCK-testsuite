/*
 * @test
 * @summary java.util.concurrent.Semaphore conformance: white-box structure + black-box
 *          behavior (acquire/release/tryAcquire/permits/drainPermits/fairness, bulk
 *          acquire/release, acquireUninterruptibly, negative-count validation,
 *          serialization round-trip).
 * @run main SemaphoreTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
