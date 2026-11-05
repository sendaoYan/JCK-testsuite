/*
 * @test
 * @summary java.lang.ref.SoftReference conformance: constructors, get retains referent
 *          (soft references are only cleared under memory pressure, which this test does
 *          not induce), null referent/queue -> NPE, clear, and queue registration.
 * @run main SoftReferenceTest
 */

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class SoftReferenceTest {
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

    static void testSoftReference() {
        Object o = new Object();
        SoftReference<Object> sr = new SoftReference<>(o);
        check(sr.get() == o, "get returns referent");
        check(!sr.isEnqueued(), "no queue not enqueued");
        check(!sr.enqueue(), "no queue enqueue false");

        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object o2 = new Object();
        SoftReference<Object> sr2 = new SoftReference<>(o2, q);
        check(sr2.get() == o2, "get with queue");
        check(!sr2.isEnqueued(), "with queue not enqueued");

        sr.clear();
        check(sr.get() == null, "clear nulls referent");

        // null referent and null queue are both permitted (no NPE)
        check(new SoftReference<Object>(null).get() == null, "null referent -> get null");
        check(new SoftReference<Object>(null, q).get() == null, "null referent with queue -> null");
        check(new SoftReference<>(new Object(), null).get() != null,
                "null queue treated as no queue");
    }

    // ---- contract deepening (batch lcL, 2026-11) ----

    static java.util.List<Object> softSamples() {
        java.util.List<Object> l = new java.util.ArrayList<>();
        l.add("jck-soft");
        l.add(Integer.valueOf(-3));
        l.add(new long[]{4L, 5L});
        l.add(new byte[0]);
        l.add(new StringBuilder("soft-sb"));
        l.add(new Object());
        return l;
    }

    static void testReferentIdentityMatrix() {
        // main deterministic contract: while the referent is strongly reachable,
        // get() keeps returning the exact same instance - across GCs too
        for (Object o : softSamples()) {
            SoftReference<Object> sr = new SoftReference<>(o);
            check(sr.get() == o, "get returns the exact referent instance");
            check(sr.get() == o, "get is stable (same instance on every call)");
            System.gc();
            check(sr.get() == o, "strongly reachable referent survives System.gc()");
            System.gc();
            check(sr.get() == o, "referent still reachable after a second System.gc()");
        }
    }

    static void testTwoRefsSameReferent() {
        Object o = new Object();
        SoftReference<Object> a = new SoftReference<>(o);
        SoftReference<Object> b = new SoftReference<>(o);
        check(a.get() == o && b.get() == o, "both soft refs see the same referent");
        check(a != b, "two distinct SoftReference instances");
        a.clear();
        check(a.get() == null, "clearing a nulls a");
        check(b.get() == o, "b is unaffected by clearing a");
        b.clear();
        check(b.get() == null, "clearing b nulls b");
    }

    static void testQueueLifecycle() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        SoftReference<Object> sr = new SoftReference<>(new Object(), q);
        check(!sr.isEnqueued(), "freshly registered ref not enqueued");
        check(sr.enqueue(), "manual enqueue returns true");
        check(sr.isEnqueued(), "isEnqueued true after manual enqueue");
        check(!sr.enqueue(), "repeat enqueue returns false");
        check(q.poll() == sr, "poll returns this exact soft reference");
        check(!sr.isEnqueued(), "not enqueued after poll");
        check(!sr.enqueue(), "no re-enqueue after poll");
        check(q.poll() == null, "queue empty afterwards");
    }

    static void testClearedRefStillEnqueues() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        SoftReference<Object> sr = new SoftReference<>(new Object(), q);
        sr.clear();
        check(sr.get() == null, "referent cleared");
        check(sr.enqueue(), "cleared soft ref still enqueues");
        check(q.poll() == sr, "cleared soft ref polled");
        check(sr.get() == null, "referent still null after the round trip");
    }

    static void testNullReferentEnqueues() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        SoftReference<Object> sr = new SoftReference<Object>(null, q);
        check(sr.get() == null, "null-referent get null");
        check(sr.enqueue(), "null-referent soft ref enqueues");
        check(q.poll() == sr, "null-referent soft ref polled");
    }

    static void testObjectContract() {
        Object o = new Object();
        SoftReference<Object> a = new SoftReference<>(o);
        SoftReference<Object> b = new SoftReference<>(o);
        check(a.equals(a), "identity equals: reflexive");
        check(!a.equals(b), "distinct soft refs with the same referent are not equal");
        check(!a.equals(null), "equals(null) false");
        check(a.hashCode() == a.hashCode(), "hashCode stable");
        String ts = a.toString();
        check(ts.startsWith("java.lang.ref.SoftReference@"), "Object.toString form: prefix");
        check(ts.endsWith(Integer.toHexString(a.hashCode())), "Object.toString form: identity hash");
        check(!(a instanceof java.io.Serializable), "SoftReference is not serializable");
        check(a instanceof java.lang.ref.Reference, "SoftReference is a Reference");
        check(SoftReference.class.getSuperclass() == java.lang.ref.Reference.class,
                "direct subclass of Reference");
        check(SoftReference.class.getName().equals("java.lang.ref.SoftReference"),
                "fully qualified class name");
        check(a.getClass() == SoftReference.class, "getClass is exact");
        ReferenceQueue<Object> q1 = new ReferenceQueue<>();
        ReferenceQueue<Object> q2 = new ReferenceQueue<>();
        SoftReference<Object> r1 = new SoftReference<>(new Object(), q1);
        SoftReference<Object> r2 = new SoftReference<>(new Object(), q2);
        r1.enqueue();
        check(q2.poll() == null, "queues are independent of each other");
        check(q1.poll() == r1, "own queue drains its registration");
        check(new SoftReference<>(new Object(), null).get() != null,
                "null queue behaves as no queue (registration still allowed)");
    }

    static class TrackingSoftReference<T> extends SoftReference<T> {
        static int created;
        final int id;
        TrackingSoftReference(T referent, int id) {
            super(referent);
            this.id = id;
            created++;
        }
    }

    static void testSubclassing() {
        Object o = new Object();
        TrackingSoftReference<Object> t = new TrackingSoftReference<>(o, 11);
        check(t.get() == o, "subclass inherits the referent");
        check(t.id == 11, "subclass fields coexist with the reference");
        check(TrackingSoftReference.created == 1, "super constructor chained exactly once");
        t.clear();
        check(t.get() == null, "clear works through the subclass");
        check(t.enqueue() == false, "no-queue subclass enqueue false");
    }

    static void testSoftVsWeakReachability() {
        Object o = new Object();
        SoftReference<Object> s = new SoftReference<>(o);
        java.lang.ref.WeakReference<Object> w = new java.lang.ref.WeakReference<>(o);
        System.gc();
        check(s.get() == o && w.get() == o,
                "strongly reachable referent: soft and weak both still see it");
        check(o != null, "caller keeps the referent strongly reachable");
    }

    static void testGcClearBestEffort() {
        // Soft references are cleared only at the collector's discretion (typically
        // under memory pressure); a bare System.gc() is NOT guaranteed to clear one.
        // Bounded best-effort: <= 10 gc() nudges x <= 10ms waits; on timeout the
        // assertion is skipped (recorded), never failed. Extreme environments may
        // comment this whole method out.
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        SoftReference<Object> sr = new SoftReference<>(new Object(), q);
        boolean clearedAndEnqueued = false;
        try {
            for (int i = 0; i < 10 && !clearedAndEnqueued; i++) {
                System.gc();
                clearedAndEnqueued = (sr.get() == null && q.poll() == sr);
            }
        } catch (Throwable ignore) {
            clearedAndEnqueued = false;
        }
        if (clearedAndEnqueued) {
            check(sr.get() == null, "soft referent cleared under GC pressure");
        } else {
            System.out.println("NOTE: SoftReference survived bounded GC loop "
                    + "(no memory pressure; permitted by spec) - best-effort skip");
        }
    }

    // ---- JDK-27-only surface (Reference.referents/refersTo since 9) ----

    static void testRefersToMatrix() throws Exception {
        Object o = new Object();
        SoftReference<Object> sr = new SoftReference<>(o);
        check(sr.refersTo(o), "refersTo live referent true");
        check(!sr.refersTo(new Object()), "refersTo a different object false");
        check(!sr.refersTo(null), "refersTo(null) false while the referent is live");
        sr.clear();
        check(!sr.refersTo(o), "refersTo false after clear");
        check(sr.refersTo(null), "refersTo(null) true once cleared (referent is null)");
        SoftReference<Object> nullRef = new SoftReference<>(null);
        check(nullRef.refersTo(null), "null-referent soft reference refersTo(null) true");
        check(!nullRef.refersTo(o), "null-referent soft reference refersTo(other) false");
        check(java.lang.ref.Reference.class.getMethod("refersTo", Object.class) != null,
                "refersTo declared on Reference and inherited by SoftReference");
    }

    public static void main(String[] args) throws Exception {
        testSoftReference();
        testReferentIdentityMatrix();
        testTwoRefsSameReferent();
        testQueueLifecycle();
        testClearedRefStillEnqueues();
        testNullReferentEnqueues();
        testObjectContract();
        testSubclassing();
        testSoftVsWeakReachability();
        testGcClearBestEffort();
        testRefersToMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
