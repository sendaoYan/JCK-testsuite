/*
 * @test
 * @summary java.lang.ref.Reference conformance: white-box structure + black-box behavior.
 * @run main ReferenceTest
 */

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;

public class ReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ref.Reference");
        check(Modifier.isAbstract(c.getModifiers()), "Reference is abstract");
        check(c.getSuperclass() == Object.class, "superclass is Object");
        check(c.getTypeParameters().length == 1, "generic type <T>");
        // public API surface
        check(c.getDeclaredMethod("get").getReturnType() == Object.class, "get() returns T");
        check(Modifier.isFinal(c.getDeclaredMethod("refersTo", Object.class).getModifiers()),
              "refersTo is final");
        check(Modifier.isFinal(c.getDeclaredMethod("isEnqueued").getModifiers()) == false,
              "isEnqueued not final (implementation detail: keep loose)");
        check(Modifier.isProtected(c.getDeclaredMethod("clone").getModifiers()), "clone protected");
        check(Modifier.isStatic(c.getDeclaredMethod("reachabilityFence", Object.class).getModifiers()),
              "reachabilityFence static");
        // constructors are package-private
        check(c.getDeclaredConstructors().length > 0
              && !Modifier.isPublic(c.getDeclaredConstructors()[0].getModifiers()),
              "no public constructor: only subclasses instantiate");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // get/refersTo/clear round-trip on a strongly-reachable referent (no GC involved)
        Object o = new Object();
        Reference<Object> ref = new WeakReference<>(o);
        check(ref.get() == o, "get returns referent");
        check(ref.refersTo(o), "refersTo true for same referent");
        check(!ref.refersTo(new Object()), "refersTo false for other object");
        check(!ref.refersTo(null), "refersTo false for null");
        check(!ref.isEnqueued(), "not enqueued initially");
        check(!ref.enqueue(), "enqueue without registered queue returns false");
        check(!ref.isEnqueued(), "still not enqueued after failed enqueue");

        ref.clear();
        check(ref.get() == null, "clear nulls referent");
        check(!ref.refersTo(o), "refersTo false after clear");
        check(ref.refersTo(null), "refersTo(null) true after clear (no referent)");

        // explicit enqueue path: fully deterministic, no System.gc()
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object o2 = new Object();
        Reference<Object> ref2 = new WeakReference<>(o2, q);
        check(ref2.get() == o2, "get with queue");
        check(ref2.isEnqueued() == false, "with queue not enqueued");
        check(ref2.enqueue(), "manual enqueue returns true");
        check(ref2.get() == null, "enqueue() clears the referent (JDK semantics)");
        check(ref2.isEnqueued(), "isEnqueued after enqueue");
        check(q.poll() == ref2, "poll returns the very same reference");
        check(!ref2.isEnqueued(), "isEnqueued false after poll");
        check(!ref2.enqueue(), "second enqueue returns false");
        // enqueue after clear: cleared+registered reference enqueues, get stays null
        Reference<Object> ref3 = new WeakReference<>(new Object(), q);
        ref3.clear();
        check(ref3.enqueue(), "enqueue succeeds after clear (registered)");
        check(q.poll() == ref3, "cleared reference still enqueued and polled");

        // null referent is allowed for weak references
        Reference<Object> nullRef = new WeakReference<>(null);
        check(nullRef.get() == null, "null referent get null");
        check(nullRef.refersTo(null), "null referent refersTo(null) true");

        // reachabilityFence is a spec-guaranteed no-op API: callable with anything
        Reference.reachabilityFence(o);
        Reference.reachabilityFence(null);
        check(true, "reachabilityFence callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
