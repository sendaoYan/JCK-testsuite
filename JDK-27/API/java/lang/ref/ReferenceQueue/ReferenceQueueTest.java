/*
 * @test
 * @summary java.lang.ref.ReferenceQueue conformance: white-box structure + black-box behavior.
 * @run main ReferenceQueueTest
 */

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;

public class ReferenceQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ref.ReferenceQueue");
        check(!Modifier.isAbstract(c.getModifiers()), "ReferenceQueue is concrete");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(c.getTypeParameters().length == 1, "generic <T>");
        check(Modifier.isPublic(c.getDeclaredConstructor().getModifiers()), "public no-arg constructor");
        check(c.getDeclaredMethod("poll").getReturnType() == Reference.class, "poll returns Reference");
        check(c.getDeclaredMethod("remove", long.class).getExceptionTypes().length == 1
              && c.getDeclaredMethod("remove", long.class).getExceptionTypes()[0] == InterruptedException.class,
              "remove(long) declares InterruptedException");
        check(c.getDeclaredMethod("remove").getExceptionTypes()[0] == InterruptedException.class,
              "remove() declares InterruptedException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // empty queue: poll/remove(timeout) are deterministic null (no blocking beyond timeout)
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        check(q.poll() == null, "poll on empty queue null");
        check(q.remove(10) == null, "remove(timeout) on empty queue null");
        check(q.remove(1) == null, "remove(1) on empty queue null");

        // manual enqueue -> poll -> empty again
        Object o = new Object();
        WeakReference<Object> ref = new WeakReference<>(o, q);
        check(ref.enqueue(), "manual enqueue");
        check(q.poll() == ref, "poll returns enqueued reference");
        check(q.poll() == null, "poll empty again after removal");
        check(q.remove(1) == null, "remove(timeout) null after removal");

        // multiple enqueued references: each is returned exactly once (order unspecified)
        ReferenceQueue<Object> q2 = new ReferenceQueue<>();
        Object a = new Object(), b = new Object(), c = new Object();
        WeakReference<Object> ra = new WeakReference<>(a, q2);
        WeakReference<Object> rb = new WeakReference<>(b, q2);
        WeakReference<Object> rc = new WeakReference<>(c, q2);
        check(ra.enqueue() && rb.enqueue() && rc.enqueue(), "enqueue three");
        java.util.Set<Reference<Object>> drained = new java.util.HashSet<>();
        drained.add((Reference<Object>) q2.poll());
        drained.add((Reference<Object>) q2.poll());
        drained.add((Reference<Object>) q2.poll());
        check(drained.size() == 3 && drained.contains(ra) && drained.contains(rb) && drained.contains(rc),
              "all three references drained exactly once");
        check(q2.poll() == null, "queue empty after draining three");

        // remove()/remove(0) return an already-present reference without blocking
        ReferenceQueue<Object> q3 = new ReferenceQueue<>();
        WeakReference<Object> r3 = new WeakReference<>(new Object(), q3);
        check(r3.enqueue(), "enqueue for remove()");
        check(q3.remove() == r3, "remove() picks up present reference");
        WeakReference<Object> r4 = new WeakReference<>(new Object(), q3);
        check(r4.enqueue(), "enqueue for remove(0)");
        check(q3.remove(0) == r4, "remove(0) picks up present reference");
        check(q3.poll() == null, "queue drained");

        // remove(long) interrupt behavior: pre-set interrupt flag makes wait() throw immediately
        Thread.currentThread().interrupt();
        try {
            Object res = q.remove(60_000); // would block indefinitely; interrupt flag is already set
            check(res == null && false, "expected InterruptedException, got " + res);
        } catch (InterruptedException expected) {
            check(true, "remove interrupted");
        }
        Thread.interrupted(); // clear flag for subsequent code

        // enqueue of an already-enqueued reference is rejected by the queue contract
        check(!r4.enqueue(), "re-enqueue after remove rejected");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
