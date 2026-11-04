/*
 * @test
 * @summary java.lang.ref.Reference conformance: get/clear/enqueue/isEnqueued via WeakReference.
 * @run main ReferenceTest
 */

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class ReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReference() {
        Object o = new Object();
        Reference<Object> ref = new WeakReference<>(o);
        check(ref.get() == o, "get returns referent");
        check(!ref.isEnqueued(), "not enqueued initially");
        check(!ref.enqueue(), "enqueue without queue returns false");
        ref.clear();
        check(ref.get() == null, "clear nulls referent");

        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Reference<Object> ref2 = new WeakReference<>(o, q);
        check(ref2.get() == o, "get with queue");
        check(!ref2.isEnqueued(), "with queue not enqueued");
        check(ref2.enqueue(), "manual enqueue returns true");
        check(ref2.isEnqueued(), "isEnqueued after enqueue");
        check(q.poll() == ref2, "poll returns enqueued reference");
        check(!ref2.isEnqueued(), "isEnqueued false after poll");
    }

    public static void main(String[] args) {
        testReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
