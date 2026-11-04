/*
 * @test
 * @summary java.lang.ref.ReferenceQueue conformance: poll empty -> null, remove(long)
 *          timeout -> null, remove(long) negative -> IllegalArgumentException (Java 8),
 *          enqueue -> poll/remove, and remove() with an already-enqueued reference.
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

    static void testEmptyQueue() throws Exception {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        check(q.poll() == null, "poll empty null");
        check(q.remove(1) == null, "remove(1) empty null");
        checkThrows(IllegalArgumentException.class, () -> q.remove(-1),
                "remove negative timeout IAE");
        checkThrows(IllegalArgumentException.class, () -> q.remove(Integer.MIN_VALUE),
                "remove MIN_VALUE timeout IAE");
    }

    static void testPollAndRemove() throws Exception {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object o = new Object();
        Reference<Object> r = new WeakReference<>(o, q);
        check(q.poll() == null, "poll before enqueue null");
        check(r.enqueue(), "enqueue true");
        check(q.poll() == r, "poll after enqueue returns reference");
        check(q.poll() == null, "poll after consumption null");

        Reference<Object> r2 = new WeakReference<>(new Object(), q);
        check(r2.enqueue(), "enqueue r2");
        check(q.remove(1) == r2, "remove(1) returns r2");

        // remove() blocks until available; enqueue first then remove() returns
        Reference<Object> r3 = new WeakReference<>(new Object(), q);
        check(r3.enqueue(), "enqueue r3");
        check(q.remove() == r3, "remove() returns r3");
    }

    public static void main(String[] args) throws Exception {
        testEmptyQueue();
        testPollAndRemove();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
