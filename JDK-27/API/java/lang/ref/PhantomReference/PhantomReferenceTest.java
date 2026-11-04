/*
 * @test
 * @summary java.lang.ref.PhantomReference conformance: white-box structure + black-box behavior.
 * @run main PhantomReferenceTest
 */

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Modifier;

public class PhantomReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ref.PhantomReference");
        check(!Modifier.isAbstract(c.getModifiers()), "PhantomReference is concrete");
        check(c.getSuperclass() == Reference.class, "extends Reference");
        // single public constructor, queue-accepting only
        check(c.getDeclaredConstructors().length == 1, "single constructor");
        check(Modifier.isPublic(c.getDeclaredConstructor(Object.class, ReferenceQueue.class).getModifiers()),
              "public PhantomReference(T, ReferenceQueue)");
        boolean overridesGet = false, overridesRefersToImpl = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("get")) overridesGet = true;
            if (m.getName().equals("refersToImpl")) overridesRefersToImpl = true;
        }
        check(overridesGet, "overrides get() to always return null");
        check(overridesRefersToImpl, "overrides refersToImpl(T) hook");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object o = new Object();
        PhantomReference<Object> pr = new PhantomReference<>(o, q);

        // core semantic: get() ALWAYS null, even while referent is strongly reachable
        check(pr.get() == null, "get always null");
        check(pr.get() == null, "get still null on repeat call");

        // refersTo still identifies the referent
        check(pr.refersTo(o), "refersTo true while referent held");
        check(!pr.refersTo(new Object()), "refersTo false for other");
        check(!pr.refersTo(null), "refersTo false for null");

        // explicit enqueue works while referent alive (deterministic path)
        check(!pr.isEnqueued(), "not enqueued initially");
        check(pr.enqueue(), "manual enqueue while referent alive");
        check(pr.isEnqueued(), "isEnqueued after enqueue");
        check(q.poll() == pr, "poll returns same phantom reference");
        check(q.poll() == null, "queue drained");

        // clear drops the referent; refersTo no longer matches
        Object o2 = new Object();
        PhantomReference<Object> pr2 = new PhantomReference<>(o2, q);
        pr2.clear();
        check(!pr2.refersTo(o2), "refersTo false after clear");
        check(pr2.get() == null, "get null after clear");

        // null queue accepted: reference never enqueued
        PhantomReference<Object> pr3 = new PhantomReference<>(o, null);
        check(!pr3.enqueue(), "null queue: enqueue false");
        check(!pr3.isEnqueued(), "null queue: never enqueued");

        // null referent permitted (rare, but constructor accepts)
        PhantomReference<Object> pr4 = new PhantomReference<>(null, q);
        check(pr4.get() == null, "null referent get null");
        check(pr4.refersTo(null), "null referent refersTo(null) true");

        // usable as plain Reference
        Reference<Object> asRef = pr4;
        check(asRef instanceof PhantomReference, "instanceof via Reference view");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
