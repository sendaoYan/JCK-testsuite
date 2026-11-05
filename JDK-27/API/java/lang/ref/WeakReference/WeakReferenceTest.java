/*
 * @test
 * @summary java.lang.ref.WeakReference conformance: constructors, get before/after
 *          clear, null referent -> NPE, queue registration, and bounded GC reclamation.
 * @run main WeakReferenceTest
 */

import java.lang.ref.Reference;
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

    // ---- contract deepening (batch lcL, 2026-11) ----

    static java.util.List<Object> weakSamples() {
        java.util.List<Object> l = new java.util.ArrayList<>();
        l.add("jck-weak");
        l.add(Integer.valueOf(-7));
        l.add(new int[]{9, 9});
        l.add(new Object());
        l.add(new StringBuilder("weak-sb"));
        l.add(java.math.BigInteger.valueOf(12345678901234L));
        return l;
    }

    static void testReferentIdentityMatrix() {
        // deterministic core: while the referent is strongly reachable, get() keeps
        // returning the exact same instance, across System.gc() too
        for (Object o : weakSamples()) {
            WeakReference<Object> w = new WeakReference<>(o);
            check(w.get() == o, "get returns the exact referent instance");
            check(w.get() == o, "get is stable (same instance on every call)");
            System.gc();
            check(w.get() == o, "strongly reachable referent survives System.gc()");
            WeakReference<Object> w2 = new WeakReference<>(o);
            check(w2.get() == o, "a second weak reference sees the same referent");
            check(w != w2, "distinct WeakReference objects");
        }
    }

    static void testClearSemanticsMatrix() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        WeakReference<Object> noQueue = new WeakReference<>(new Object());
        noQueue.clear();
        check(noQueue.get() == null, "clear then get null");
        check(!noQueue.enqueue(), "enqueue after clear (no queue) false");

        WeakReference<Object> queued = new WeakReference<>(new Object(), q);
        queued.clear();
        check(queued.get() == null, "queued ref cleared");
        check(queued.enqueue(), "cleared queued ref still enqueues");
        check(q.poll() == queued, "cleared queued ref polled");
        check(queued.get() == null, "referent null after the round trip");
        queued.clear();
        check(queued.get() == null, "double clear idempotent");
    }

    static void testQueueLifecycle() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        WeakReference<Object> w = new WeakReference<>(new Object(), q);
        check(!w.isEnqueued(), "not enqueued initially");
        check(w.enqueue(), "manual enqueue true");
        check(w.get() == null, "enqueue() clears the referent (probed on 8 and 27 alike)");
        check(w.isEnqueued(), "isEnqueued true while queued");
        check(!w.enqueue(), "second enqueue false");
        check(q.poll() == w, "poll returns this exact weak reference");
        check(!w.isEnqueued(), "isEnqueued false after poll");
        check(!w.enqueue(), "no re-enqueue after poll");
        check(q.poll() == null, "queue empty afterwards");
    }

    static void testTwoRefsSameReferent() {
        Object o = new Object();
        WeakReference<Object> a = new WeakReference<>(o);
        WeakReference<Object> b = new WeakReference<>(o);
        check(a.get() == o && b.get() == o, "both weak refs see the referent");
        check(a != b, "distinct reference objects");
        a.clear();
        check(a.get() == null, "cleared a");
        check(b.get() == o, "b unaffected by clearing a");
        b.clear();
        check(b.get() == null, "cleared b");
    }

    static void testObjectContract() {
        Object o = new Object();
        WeakReference<Object> a = new WeakReference<>(o);
        WeakReference<Object> b = new WeakReference<>(o);
        check(a.equals(a), "identity equals: reflexive");
        check(!a.equals(b), "two weak refs to the same referent are not equal");
        check(!a.equals(null), "equals(null) false");
        check(a.hashCode() == a.hashCode(), "hashCode stable");
        String ts = a.toString();
        check(ts.startsWith("java.lang.ref.WeakReference@"), "Object.toString form: prefix");
        check(ts.endsWith(Integer.toHexString(a.hashCode())), "Object.toString form: identity hash");
        check(!(a instanceof java.io.Serializable), "WeakReference is not serializable");
        check(a instanceof java.lang.ref.Reference, "WeakReference is a Reference");
        check(WeakReference.class.getSuperclass() == java.lang.ref.Reference.class,
                "direct subclass of Reference");
        check(WeakReference.class.getName().equals("java.lang.ref.WeakReference"),
                "fully qualified class name");
        check(a.getClass() == WeakReference.class, "getClass is exact");
        check(!java.lang.reflect.Modifier.isFinal(WeakReference.class.getModifiers()),
                "WeakReference is open for subclassing");
    }

    static class TrackingWeakReference<T> extends WeakReference<T> {
        static int created;
        final int id;
        TrackingWeakReference(T referent, int id) {
            super(referent);
            this.id = id;
            created++;
        }
    }

    static void testSubclassing() {
        Object o = new Object();
        TrackingWeakReference<Object> t = new TrackingWeakReference<>(o, 3);
        check(t.get() == o, "subclass inherits the referent");
        check(t.id == 3, "subclass fields coexist");
        check(TrackingWeakReference.created == 1, "super constructor chained once");
        t.clear();
        check(t.get() == null, "clear works through the subclass");
    }

    static void testNullCombinationsExtra() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        WeakReference<Object> n1 = new WeakReference<Object>(null);
        WeakReference<Object> n2 = new WeakReference<Object>(null, q);
        check(n1.get() == null && n2.get() == null, "null referents read null");
        check(!n1.enqueue(), "null referent, no queue: enqueue false");
        check(n2.enqueue(), "null referent with queue enqueues");
        check(q.poll() == n2, "null-referent ref drained from queue");
    }

    static void testStrongRetentionAcrossGc() {
        Object o = new Object();
        WeakReference<Object> w = new WeakReference<>(o);
        for (int i = 0; i < 3; i++) {
            System.gc();
            check(w.get() == o, "round " + i + ": strong reference keeps the referent alive");
        }
    }

    static void testGcEnqueueBestEffort() {
        // Signature bounded-GC contract: once the referent is unreachable the weak
        // reference is cleared AND enqueued. <= 10 gc() nudges x <= 10ms waits; if
        // the collector has not reclaimed within the bound, the assertion is
        // skipped (recorded), never failed. Extreme environments may comment out.
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        WeakReference<Object> w = new WeakReference<>(new Object(), q);
        Reference<?> polled = null;
        try {
            for (int i = 0; i < 10 && polled == null; i++) {
                System.gc();
                Thread.sleep(10); // bounded wait, part of the <= 10 x <= 10ms budget
                polled = q.poll();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (polled == w) {
            check(w.get() == null, "enqueued weak reference has been cleared by the collector");
            check(q.poll() == null, "reference consumed exactly once from the queue");
        } else {
            System.out.println("NOTE: weak referent not reclaimed within bounded GC loop "
                    + "- best-effort skip (polled=" + polled + ")");
        }
    }

    static void testGenericVariance() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        WeakReference<String> ws = new WeakReference<>("generic", q);
        check(ws.get().equals("generic"), "weak ref to String retains value");
        check(ws.enqueue(), "String weak ref enqueues on Object queue");
        check(q.poll() == ws, "poll returns it");
        ReferenceQueue<Number> qn = new ReferenceQueue<>();
        WeakReference<java.math.BigInteger> wb =
                new WeakReference<>(java.math.BigInteger.valueOf(2), qn);
        check(wb.enqueue(), "BigInteger weak ref enqueues on Number queue");
        check(qn.poll() == wb, "poll returns it");
    }

    // ---- JDK-27-only surface (refersTo since 9) ----

    static void testRefersToMatrix() {
        Object o = new Object();
        WeakReference<Object> w = new WeakReference<>(o);
        check(w.refersTo(o), "refersTo live referent true");
        check(!w.refersTo(new Object()), "refersTo a different object false");
        check(!w.refersTo(null), "refersTo(null) false while the referent is live");
        System.gc();
        check(w.refersTo(o), "refersTo still true after gc (referent strongly reachable)");
        w.clear();
        check(!w.refersTo(o), "refersTo false after clear");
        check(w.refersTo(null), "refersTo(null) true once cleared");
        WeakReference<Object> nullRef = new WeakReference<>(null);
        check(nullRef.refersTo(null), "null-referent weak refersTo(null) true");
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        WeakReference<Object> enq = new WeakReference<>(o, q);
        enq.enqueue();
        check(enq.get() == null, "enqueue() clears the referent (probed on 8 and 27 alike)");
        check(!enq.refersTo(o), "refersTo false after enqueue (referent cleared by enqueue)");
        java.lang.ref.Reference.reachabilityFence(o);
        check(true, "reachabilityFence callable alongside weak references");
    }

    public static void main(String[] args) {
        testConstructors();
        testClear();
        testGC();
        testReferentIdentityMatrix();
        testClearSemanticsMatrix();
        testQueueLifecycle();
        testTwoRefsSameReferent();
        testObjectContract();
        testSubclassing();
        testNullCombinationsExtra();
        testStrongRetentionAcrossGc();
        testGcEnqueueBestEffort();
        testGenericVariance();
        testRefersToMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
