/*
 * @test
 * @summary java.lang.ref.SoftReference conformance: white-box structure + black-box behavior.
 * @run main SoftReferenceTest
 */

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;

public class SoftReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ref.SoftReference");
        check(!Modifier.isAbstract(c.getModifiers()), "SoftReference is concrete");
        check(c.getSuperclass() == Reference.class, "extends Reference");
        check(Modifier.isPublic(c.getDeclaredConstructor(Object.class).getModifiers()),
              "public SoftReference(T)");
        check(Modifier.isPublic(c.getDeclaredConstructor(Object.class, ReferenceQueue.class).getModifiers()),
              "public SoftReference(T, ReferenceQueue)");
        // get() is overridden (covariant with Reference.get)
        check(c.getDeclaredMethod("get") != null, "overrides get()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // construction + get with strong referent: fully deterministic
        String o = "soft-referent-" + 42;
        SoftReference<String> sr = new SoftReference<>(o);
        check(sr.get() == o, "get returns referent while strongly reachable");
        check(sr.refersTo(o), "refersTo true");
        check(!sr.isEnqueued(), "not enqueued");

        // two SoftReferences to the same referent are independent objects
        SoftReference<String> sr2 = new SoftReference<>(o);
        check(sr != sr2 && sr.get() == sr2.get(), "independent references, same referent");
        check(sr.equals(sr2) == false, "equals is identity");

        // queue-registered variant + manual enqueue round-trip (no GC)
        ReferenceQueue<String> q = new ReferenceQueue<>();
        SoftReference<String> sr3 = new SoftReference<>(o, q);
        check(sr3.get() == o, "get with queue");
        check(sr3.enqueue(), "manual enqueue");
        check(q.poll() == sr3, "poll returns same reference");

        // clear semantics inherited from Reference
        sr3.clear();
        check(sr3.get() == null, "clear nulls referent");
        check(sr3.refersTo(null), "refersTo(null) after clear");

        // null referent is permitted
        SoftReference<Object> nref = new SoftReference<>(null);
        check(nref.get() == null, "null referent get null");

        // static type view: usable as plain Reference
        Reference<String> asRef = new SoftReference<>(o);
        check(asRef.get() == o, "usable as Reference<T>");
        check(asRef instanceof SoftReference, "instanceof SoftReference");
        Object otherRef = new WeakReference<>(o);
        check(!(otherRef instanceof SoftReference), "WeakReference is not SoftReference");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
