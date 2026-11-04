/*
 * @test
 * @summary java.lang.ref.WeakReference conformance: white-box structure + black-box behavior.
 * @run main WeakReferenceTest
 */

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;

public class WeakReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ref.WeakReference");
        check(!Modifier.isAbstract(c.getModifiers()), "WeakReference is concrete");
        check(c.getSuperclass() == Reference.class, "extends Reference");
        check(Modifier.isPublic(c.getDeclaredConstructor(Object.class).getModifiers()),
              "public WeakReference(T)");
        check(Modifier.isPublic(c.getDeclaredConstructor(Object.class, ReferenceQueue.class).getModifiers()),
              "public WeakReference(T, ReferenceQueue)");
        // does NOT override get()
        boolean overridesGet = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("get")) overridesGet = true;
        }
        check(!overridesGet, "get() inherited from Reference (no override)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // strong referent: get deterministic without GC
        StringBuilder sb = new StringBuilder("weak");
        WeakReference<StringBuilder> wr = new WeakReference<>(sb);
        check(wr.get() == sb, "get returns referent while strongly reachable");
        check(wr.refersTo(sb), "refersTo true");

        // interning/lookup pattern: key kept alive, identity comparison via refersTo
        Object key = new Object();
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        WeakReference<Object> kref = new WeakReference<>(key, q);
        check(kref.enqueue(), "manual enqueue works while referent alive");
        check(q.poll() == kref, "poll round-trip");
        // enqueue() clears the referent before queueing (JDK semantics)
        check(kref.get() == null, "get null after manual enqueue");
        check(!kref.refersTo(key), "refersTo false after manual enqueue");

        // clear
        wr.clear();
        check(wr.get() == null, "clear nulls referent");
        check(!wr.isEnqueued(), "clear alone does not enqueue");

        // null referent allowed
        WeakReference<Object> nref = new WeakReference<>(null, null);
        check(nref.get() == null, "null referent, null queue accepted");
        check(!nref.enqueue(), "enqueue with null queue false");
        check(!nref.isEnqueued(), "never enqueued with null queue");

        // subtype relationships usable via Reference
        Reference<Object> asSuper = new WeakReference<>(sb);
        check(asSuper.get() == sb, "usable as Reference<T>");
        check(WeakReference.class.isAssignableFrom(asSuper.getClass()), "assignable back");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
