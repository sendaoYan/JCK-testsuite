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

    // ---- contract deepening (batch lcL, 2026-11) ----

    static void testGetIdentityStability() {
        Object[] samples = {
            "jck-string", Integer.valueOf(42), Long.valueOf(9L),
            new int[]{1, 2, 3}, new Object(), new StringBuilder("sb")
        };
        for (Object o : samples) {
            Reference<Object> r = new WeakReference<>(o);
            check(r.get() == o, "get returns the exact referent instance");
            check(r.get() == o, "repeated get returns the same instance");
            System.gc(); // referent is strongly reachable here, so survival is spec-guaranteed
            check(r.get() == o, "strongly reachable referent survives System.gc()");
            Reference<Object> r2 = new WeakReference<>(o);
            check(r2.get() == o, "a second reference sees the same referent");
            check(r != r2, "distinct Reference objects for the same referent");
            r.clear();
            check(r.get() == null, "clear affects only the cleared reference");
            check(r2.get() == o, "the other reference is untouched by clear");
        }
    }

    static void testEnqueueDequeueStateMachine() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object o = new Object();
        Reference<Object> cleared = new WeakReference<>(o, q);
        cleared.clear();
        check(cleared.enqueue(), "manual enqueue succeeds even after clear (registered ref)");
        check(q.poll() == cleared, "cleared ref polled from its queue");
        check(!cleared.enqueue(), "enqueue after dequeue returns false");
        check(q.poll() == null, "queue empty after drain");

        Reference<Object> nullRef = new WeakReference<Object>(null, q);
        check(nullRef.enqueue(), "null-referent ref enqueues");
        check(q.poll() == nullRef, "null-referent ref polled");
        check(nullRef.get() == null, "null-referent get stays null");

        Reference<Object> again = new WeakReference<>(o, q);
        check(again.enqueue(), "queue reusable after being drained");
        try {
            check(q.remove(1) == again, "remove(1) returns the re-enqueued ref");
        } catch (InterruptedException e) {
            throw new AssertionError("unexpected interrupt", e);
        }
        check(q.poll() == null, "queue empty again");
    }

    static void testReferenceKindsOnSameObject() {
        Object o = new Object();
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        WeakReference<Object> w = new WeakReference<>(o);
        SoftReference<Object> s = new SoftReference<>(o);
        PhantomReference<Object> p = new PhantomReference<>(o, q);
        check(w.get() == o, "weak reference sees the referent");
        check(s.get() == o, "soft reference sees the referent");
        check(p.get() == null, "phantom reference never exposes the referent");
        w.clear();
        check(s.get() == o, "clearing the weak ref leaves the soft ref");
        check(p.get() == null, "phantom still null after weak clear");
        s.clear();
        check(w.get() == null && s.get() == null, "both cleared refs report null");
        check(p.enqueue(), "phantom enqueue allowed on a live referent");
        check(p.get() == null, "phantom get null after manual enqueue with live referent");
        check(q.poll() == p, "phantom drained from its queue");
    }

    static void testReferenceObjectContract() {
        Object o = new Object();
        Reference<Object> a = new WeakReference<>(o);
        Reference<Object> b = new WeakReference<>(o);
        check(a.equals(a), "reference equals itself (identity equality)");
        check(!a.equals(b), "two references to the same referent are not equal");
        check(!b.equals(a), "identity equality symmetric across distinct refs");
        check(!a.equals(null), "equals(null) is false");
        check(a.hashCode() == a.hashCode(), "hashCode stable across calls");
        String ts = a.toString();
        check(ts.startsWith("java.lang.ref.WeakReference@"), "Object.toString form: prefix");
        check(ts.endsWith(Integer.toHexString(a.hashCode())), "Object.toString form: identity hash");
        check(!(a instanceof java.io.Serializable), "references are not serializable");
        check(a instanceof Reference, "WeakReference is a Reference");
        check(WeakReference.class.getSuperclass() == Reference.class, "WeakReference direct subclass");
        check(SoftReference.class.getSuperclass() == Reference.class, "SoftReference direct subclass");
        check(PhantomReference.class.getSuperclass() == Reference.class, "PhantomReference direct subclass");
        check(java.lang.reflect.Modifier.isAbstract(Reference.class.getModifiers()),
                "Reference is abstract and cannot be instantiated");
        check(Reference.class.getName().equals("java.lang.ref.Reference"), "fully qualified name");
        Reference<Object> qRef = new WeakReference<>(new ReferenceQueue<Object>());
        check(qRef.get() instanceof ReferenceQueue, "referent of any type is retained intact");
    }

    static void testQueueIndependence() {
        ReferenceQueue<Object> q1 = new ReferenceQueue<>();
        ReferenceQueue<Object> q2 = new ReferenceQueue<>();
        Reference<Object> r1 = new WeakReference<>(new Object(), q1);
        Reference<Object> r2 = new WeakReference<>(new Object(), q2);
        check(q1.poll() == null && q2.poll() == null, "both queues start empty");
        check(r1.enqueue(), "enqueue r1 into q1");
        check(q2.poll() == null, "q2 unaffected by r1's enqueue");
        check(q1.poll() == r1, "q1 returns r1");
        check(r2.enqueue(), "enqueue r2 into q2");
        check(q1.poll() == null, "q1 unaffected by r2's enqueue");
        check(q2.poll() == r2, "q2 returns r2");
    }

    static void testIsEnqueuedDeprecationSurface() throws Exception {
        java.lang.reflect.Method m = Reference.class.getMethod("isEnqueued");
        // probed on jdk8u372: isEnqueued carries no @Deprecated annotation
        check(!m.isAnnotationPresent(Deprecated.class),
                "isEnqueued is not deprecated on JDK-8");
        check(m.getReturnType() == boolean.class, "isEnqueued returns boolean");
    }

    public static void main(String[] args) throws Exception {
        testGetClearEnqueue();
        testReferenceQueueRemove();
        testWeakReferenceGC();
        testSoftReference();
        testPhantomReference();
        testGetIdentityStability();
        testEnqueueDequeueStateMachine();
        testReferenceKindsOnSameObject();
        testReferenceObjectContract();
        testQueueIndependence();
        testIsEnqueuedDeprecationSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
