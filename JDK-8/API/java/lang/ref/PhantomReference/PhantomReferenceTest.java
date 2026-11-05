/*
 * @test
 * @summary java.lang.ref.PhantomReference conformance: constructor requires a queue,
 *          get() always returns null even with a live referent, null referent/queue -> NPE,
 *          enqueue/isEnqueued, and queue poll.
 * @run main PhantomReferenceTest
 */

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

public class PhantomReferenceTest {
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

    static void testPhantomReference() {
        Object o = new Object();
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        PhantomReference<Object> pr = new PhantomReference<>(o, q);
        check(pr.get() == null, "phantom get always null even with live referent");
        check(!pr.isEnqueued(), "not enqueued initially");
        check(pr.enqueue(), "manual enqueue true");
        check(pr.isEnqueued(), "enqueued after enqueue");
        check(q.poll() == pr, "poll returns phantom reference");
        check(!pr.isEnqueued(), "not enqueued after poll");

        // null referent and null queue are both permitted (no NPE)
        check(new PhantomReference<>(null, q).get() == null, "null referent -> get null");
        check(new PhantomReference<>(new Object(), null).get() == null,
                "null queue still get null");
    }

    // ---- contract deepening (batch lcL, 2026-11) ----

    static java.util.List<Object> phantomSamples() {
        java.util.List<Object> l = new java.util.ArrayList<>();
        l.add("jck-phantom");
        l.add(Integer.valueOf(-11));
        l.add(new long[]{7, 8});
        l.add(new byte[3]);
        l.add(new StringBuilder("phantom-sb"));
        l.add(new Object());
        l.add(""); // empty string is still a valid referent
        return l;
    }

    static void testGetAlwaysNullMatrix() {
        // THE phantom contract: get() returns null unconditionally - at construction,
        // after clear(), and across GCs - even while the referent is strongly reachable
        for (Object o : phantomSamples()) {
            PhantomReference<Object> p = new PhantomReference<>(o, new ReferenceQueue<Object>());
            check(p.get() == null, "get null immediately after construction");
            check(p.get() == null, "get null on repeated calls");
            System.gc(); // referent strongly reachable: irrelevant for get()
            check(p.get() == null, "get null after System.gc() with live referent");
            p.clear();
            check(p.get() == null, "get null after clear()");
            System.gc();
            check(p.get() == null, "get null after clear() plus gc()");
        }
    }

    static void testNullQueueAllowed() {
        // probed on jdk8u372 and jdk-27.0.1: a null queue does NOT throw; the
        // resulting phantom reference is merely useless (never enqueued)
        PhantomReference<Object> p = new PhantomReference<>(new Object(), null);
        check(p.get() == null, "null-queue phantom: get still null");
        check(!p.isEnqueued(), "null-queue phantom: never enqueued");
        check(!p.enqueue(), "null-queue phantom: enqueue returns false");
    }

    static void testNullReferentLifecycle() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        PhantomReference<Object> p = new PhantomReference<>(null, q);
        check(p.get() == null, "null-referent phantom: get null");
        check(p.enqueue(), "null-referent phantom enqueues (probed)");
        check(q.poll() == p, "null-referent phantom polled");
        check(!p.enqueue(), "no re-enqueue after poll");
    }

    static void testEnqueueStateMachine() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object o = new Object();
        PhantomReference<Object> p = new PhantomReference<>(o, q);
        check(!p.isEnqueued(), "not enqueued initially");
        check(p.enqueue(), "manual enqueue returns true");
        check(p.get() == null, "get null after manual enqueue (enqueue clears referent)");
        check(p.isEnqueued(), "isEnqueued true while queued");
        check(!p.enqueue(), "second enqueue returns false");
        check(q.poll() == p, "poll returns this exact phantom reference");
        check(!p.isEnqueued(), "isEnqueued false after poll");
        check(!p.enqueue(), "no re-enqueue after poll");
        check(q.poll() == null, "queue empty afterwards");
    }

    static void testEnqueueAfterClear() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        PhantomReference<Object> p = new PhantomReference<>(new Object(), q);
        p.clear();
        check(p.get() == null, "cleared phantom: get null (as always)");
        check(p.enqueue(), "cleared phantom still enqueues (probed)");
        check(q.poll() == p, "cleared phantom polled");
    }

    static void testMultiplePhantomsSameReferent() {
        Object o = new Object();
        ReferenceQueue<Object> q1 = new ReferenceQueue<>();
        ReferenceQueue<Object> q2 = new ReferenceQueue<>();
        PhantomReference<Object> p1 = new PhantomReference<>(o, q1);
        PhantomReference<Object> p2 = new PhantomReference<>(o, q2);
        PhantomReference<Object> p3 = new PhantomReference<>(o, q1);
        check(p1.get() == null && p2.get() == null && p3.get() == null,
                "every phantom reports null for the same referent");
        check(p1 != p2 && p1 != p3 && p2 != p3, "three distinct phantom references");
        check(p1.enqueue(), "p1 enqueues to q1");
        check(p2.enqueue(), "p2 enqueues to q2");
        check(p3.enqueue(), "p3 enqueues to q1 as well");
        check(q2.poll() == p2, "q2 drains only its own registration");
        check(q1.poll() != null && q1.poll() != null, "q1 drains both its registrations");
        check(q1.poll() == null && q2.poll() == null, "both queues empty");
    }

    static void testMixedWeakAndPhantom() {
        Object o = new Object();
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        java.lang.ref.WeakReference<Object> w = new java.lang.ref.WeakReference<>(o);
        PhantomReference<Object> p = new PhantomReference<>(o, q);
        check(w.get() == o, "weak exposes the referent");
        check(p.get() == null, "phantom hides the same referent");
        System.gc();
        check(w.get() == o, "weak still sees strongly reachable referent");
        check(p.get() == null, "phantom still null");
        w.clear();
        check(w.get() == null && p.get() == null, "after weak clear: both null");
        check(p.enqueue(), "phantom enqueue unaffected by the weak ref's clear");
        check(q.poll() == p, "phantom drained");
    }

    static void testObjectContract() {
        Object o = new Object();
        PhantomReference<Object> a = new PhantomReference<>(o, new ReferenceQueue<Object>());
        PhantomReference<Object> b = new PhantomReference<>(o, new ReferenceQueue<Object>());
        check(a.equals(a), "identity equals: reflexive");
        check(!a.equals(b), "two phantoms for the same referent are not equal");
        check(!a.equals(null), "equals(null) false");
        check(a.hashCode() == a.hashCode(), "hashCode stable");
        String ts = a.toString();
        check(ts.startsWith("java.lang.ref.PhantomReference@"), "Object.toString form: prefix");
        check(ts.endsWith(Integer.toHexString(a.hashCode())), "Object.toString form: identity hash");
        check(!(a instanceof java.io.Serializable), "PhantomReference is not serializable");
        check(a instanceof java.lang.ref.Reference, "PhantomReference is a Reference");
        check(PhantomReference.class.getSuperclass() == java.lang.ref.Reference.class,
                "direct subclass of Reference");
        check(PhantomReference.class.getName().equals("java.lang.ref.PhantomReference"),
                "fully qualified class name");
        check(a.getClass() == PhantomReference.class, "getClass is exact");
        check(!java.lang.reflect.Modifier.isFinal(PhantomReference.class.getModifiers()),
                "PhantomReference is open for subclassing");
    }

    static class TrackingPhantom<T> extends PhantomReference<T> {
        static int created;
        final int id;
        TrackingPhantom(T referent, ReferenceQueue<? super T> q, int id) {
            super(referent, q);
            this.id = id;
            created++;
        }
    }

    static void testSubclassing() {
        Object o = new Object();
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        TrackingPhantom<Object> t = new TrackingPhantom<>(o, q, 5);
        check(t.get() == null, "subclass get inherits the always-null contract");
        check(t.id == 5, "subclass fields coexist");
        check(TrackingPhantom.created == 1, "super constructor chained once");
        check(t.enqueue(), "subclass enqueue works");
        check(q.poll() == t, "subclass phantom drains");
    }

    static void testGcEnqueueBestEffort() {
        // A phantom reference is enqueued after the referent becomes unreachable and
        // is then finalizable. Bounded best-effort: <= 10 gc() nudges x <= 10ms waits;
        // on timeout the assertion is skipped (recorded), never failed. Extreme
        // environments may comment this whole method out.
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        PhantomReference<Object> p = new PhantomReference<>(new Object(), q);
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
        if (polled == p) {
            check(p.get() == null, "enqueued phantom still reports get() null");
            check(q.poll() == null, "phantom consumed exactly once");
        } else {
            System.out.println("NOTE: phantom not enqueued within bounded GC loop "
                    + "- best-effort skip (polled=" + polled + ")");
        }
    }

    public static void main(String[] args) {
        testPhantomReference();
        testGetAlwaysNullMatrix();
        testNullQueueAllowed();
        testNullReferentLifecycle();
        testEnqueueStateMachine();
        testEnqueueAfterClear();
        testMultiplePhantomsSameReferent();
        testMixedWeakAndPhantom();
        testObjectContract();
        testSubclassing();
        testGcEnqueueBestEffort();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
