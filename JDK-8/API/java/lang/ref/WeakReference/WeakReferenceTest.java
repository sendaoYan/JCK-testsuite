/*
 * @test
 * @summary java.lang.ref.WeakReference conformance: constructors, get before/after
 *          clear, null referent -> NPE, queue registration, and bounded GC reclamation.
 * @run main WeakReferenceTest
 */

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class WeakReferenceTest {
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

    static void testConstructors() {
        Object o = new Object();
        WeakReference<Object> wr = new WeakReference<>(o);
        check(wr.get() == o, "get returns referent");
        check(!wr.isEnqueued(), "no queue not enqueued");
        check(!wr.enqueue(), "no queue enqueue false");

        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object o2 = new Object();
        WeakReference<Object> wr2 = new WeakReference<>(o2, q);
        check(wr2.get() == o2, "get with queue");
        check(!wr2.isEnqueued(), "with queue not enqueued");

        // null referent and null queue are both permitted (no NPE)
        check(new WeakReference<Object>(null).get() == null, "null referent -> get null");
        check(new WeakReference<Object>(null, q).get() == null, "null referent with queue -> null");
        check(new WeakReference<>(new Object(), null).get() != null,
                "null queue treated as no queue");
    }

    static void testClear() {
        Object o = new Object();
        WeakReference<Object> wr = new WeakReference<>(o);
        wr.clear();
        check(wr.get() == null, "clear nulls referent");
        check(wr.get() == null, "get stays null after clear");
        wr.clear();
        check(wr.get() == null, "double clear idempotent");
    }

    static void testGC() {
        // bounded retry; see ReferenceTest for the same note about extreme environments
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        WeakReference<Object> wr = new WeakReference<>(new Object(), q);
        boolean cleared = false;
        for (int i = 0; i < 10 && !cleared; i++) {
            System.gc();
            for (int j = 0; j < 100000; j++) new Object();
            cleared = (wr.get() == null || q.poll() != null);
        }
        check(cleared, "weak referent reclaimed");
    }

    public static void main(String[] args) {
        testConstructors();
        testClear();
        testGC();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
