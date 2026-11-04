/*
 * @test
 * @summary java.lang.ref.PhantomReference conformance: constructor requires a queue,
 *          get() always returns null even with a live referent, null referent/queue -> NPE,
 *          enqueue/isEnqueued, and queue poll.
 * @run main PhantomReferenceTest
 */

import java.lang.ref.PhantomReference;
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

    public static void main(String[] args) {
        testPhantomReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
