/*
 * @test
 * @summary java.lang.ref.ReferenceQueue conformance: poll/remove timeout on empty queue and
 *          manual enqueue round-trip.
 * @run main ReferenceQueueTest
 */

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class ReferenceQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReferenceQueue() throws Exception {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        check(q.poll() == null, "poll empty null");
        check(q.remove(10) == null, "remove(timeout) empty null");

        Object o = new Object();
        WeakReference<Object> ref = new WeakReference<>(o, q);
        check(ref.enqueue(), "manual enqueue");
        Reference<?> r = q.poll();
        check(r == ref, "poll returns enqueued reference");
        check(q.poll() == null, "poll empty again");
    }

    public static void main(String[] args) throws Exception {
        testReferenceQueue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
