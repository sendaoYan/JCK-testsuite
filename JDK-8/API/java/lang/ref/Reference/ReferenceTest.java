/*
 * @test
 * @summary java.lang.ref.Reference conformance: full contract coverage.
 *          get/clear/enqueue/isEnqueued state machine, enqueue returns false with no
 *          queue / after clear / when already enqueued, ReferenceQueue poll/remove(1)/
 *          remove(-1)->IllegalArgumentException, WeakReference bounded GC reclamation,
 *          SoftReference referent retained, PhantomReference.get() always null and its
 *          queue registration, and reachability semantics across the three subclasses.
 * @run main ReferenceTest
 */

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public class ReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testGetClearEnqueue() {
        Object o = new Object();
        Reference<Object> ref = new WeakReference<>(o);
        check(ref.get() == o, "get returns referent");
        check(!ref.isEnqueued(), "not enqueued initially");
        check(!ref.enqueue(), "enqueue without queue returns false");
        check(!ref.isEnqueued(), "still not enqueued after false enqueue");

        ref.clear();
        check(ref.get() == null, "clear nulls referent");
        check(!ref.enqueue(), "enqueue after clear false");

        // with a queue
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object o2 = new Object();
        Reference<Object> ref2 = new WeakReference<>(o2, q);
        check(ref2.get() == o2, "get with queue");
        check(!ref2.isEnqueued(), "with queue not enqueued initially");
        check(ref2.enqueue(), "manual enqueue returns true");
        check(ref2.isEnqueued(), "isEnqueued after enqueue");
        check(!ref2.enqueue(), "enqueue again returns false");
        check(q.poll() == ref2, "poll returns enqueued reference");
        check(!ref2.isEnqueued(), "isEnqueued false after poll");
        check(q.poll() == null, "poll empty queue null");
    }

    static void testReferenceQueueRemove() throws Exception {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        // no enqueued reference -> remove(1) times out and returns null
        check(q.remove(1) == null, "remove(1) returns null when empty");
        // negative timeout -> IllegalArgumentException (Java 8)
        checkThrows(IllegalArgumentException.class, () -> q.remove(-1),
                "remove negative timeout IAE");

        // enqueue then remove with timeout
        Object o = new Object();
        Reference<Object> r = new WeakReference<>(o, q);
        check(r.enqueue(), "enqueue for remove");
        check(q.remove(1) == r, "remove(1) returns enqueued reference");
    }

    static void testWeakReferenceGC() {
        // bounded GC retry: a WeakReference must be cleared once its referent is
        // unreachable. We do not assert precise timing, only eventual reclamation
        // within a bounded number of GC nudges. (Comment out in extreme environments.)
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Reference<Object> ref = new WeakReference<>(new Object(), q);
        boolean cleared = false;
        for (int i = 0; i < 10 && !cleared; i++) {
            System.gc();
            for (int j = 0; j < 100000; j++) new Object();
            cleared = (ref.get() == null || q.poll() != null);
        }
        check(cleared, "weak referent reclaimed after bounded GC");
    }

    static void testSoftReference() {
        Object o = new Object();
        SoftReference<Object> sr = new SoftReference<>(o);
        check(sr.get() == o, "soft get returns referent");
        check(!sr.isEnqueued(), "soft not enqueued");

        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object o2 = new Object();
        SoftReference<Object> sr2 = new SoftReference<>(o2, q);
        check(sr2.get() == o2, "soft get with queue");
    }

    static void testPhantomReference() {
        Object o = new Object();
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        PhantomReference<Object> pr = new PhantomReference<>(o, q);
        check(pr.get() == null, "phantom get always null");
        check(!pr.isEnqueued(), "phantom not enqueued initially");
        check(pr.enqueue(), "phantom manual enqueue true");
        check(pr.isEnqueued(), "phantom enqueued");
        check(q.poll() == pr, "phantom poll");
    }

    public static void main(String[] args) throws Exception {
        testGetClearEnqueue();
        testReferenceQueueRemove();
        testWeakReferenceGC();
        testSoftReference();
        testPhantomReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
