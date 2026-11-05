/*
 * @test
 * @summary java.lang.ref.ReferenceQueue conformance: poll empty -> null, remove(long)
 *          timeout -> null, remove(long) negative -> IllegalArgumentException (Java 8),
 *          enqueue -> poll/remove, and remove() with an already-enqueued reference.
 * @run main ReferenceQueueTest
 */

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
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

    // ---- contract deepening (batch lcL, 2026-11) ----

    static void testEmptyPollRepeatAndTimeouts() throws Exception {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        boolean allNull = true;
        for (int i = 0; i < 1000; i++) allNull = allNull && (q.poll() == null);
        check(allNull, "poll never blocks and stays null on an empty queue");
        check(q.remove(1) == null, "remove(1) times out to null");
        check(q.remove(3) == null, "remove(3) times out to null");
        check(q.poll() == null, "poll still null after timed-out removes");
    }

    static void testNegativeTimeoutExactMessage() {
        // message locked empirically on jdk8u372 and jdk-27.0.1 (identical)
        try {
            new ReferenceQueue<Object>().remove(-1);
            check(false, "remove(-1) must throw");
        } catch (IllegalArgumentException e) {
            check("Negative timeout value".equals(e.getMessage()), "remove(-1) message exact");
        } catch (InterruptedException e) {
            check(false, "unexpected InterruptedException for remove(-1)");
        }
        long[] negatives = {-2L, -1000L, Integer.MIN_VALUE + 1L, Long.MIN_VALUE};
        for (long t : negatives) {
            try {
                new ReferenceQueue<Object>().remove(t);
                check(false, "remove(" + t + ") must throw");
            } catch (IllegalArgumentException e) {
                check("Negative timeout value".equals(e.getMessage()),
                        "remove(" + t + ") message exact");
            } catch (InterruptedException e) {
                check(false, "unexpected InterruptedException for remove(" + t + ")");
            }
        }
    }

    static void testDrainSetSemantics() throws Exception {
        // NOTE: the relative order of manually enqueued references is NOT specified
        // (probed: it is not FIFO on either JDK), so only set semantics are asserted.
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        java.util.ArrayList<Reference<Object>> registered = new java.util.ArrayList<>();
        int n = 5;
        for (int i = 0; i < n; i++) {
            Reference<Object> r = new WeakReference<>(new Object(), q);
            registered.add(r);
            check(r.enqueue(), "enqueue ref #" + i);
        }
        for (int i = 0; i < n; i++) {
            Reference<?> got = q.poll();
            check(got != null, "poll returns a reference while draining (" + i + ")");
            check(registered.contains(got), "drained ref is one of the enqueued refs");
        }
        check(q.poll() == null, "queue empty after draining all enqueued refs");
        check(q.remove(1) == null, "remove(1) null after full drain");
    }

    static void testClearedAndNullReferentRefs() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Reference<Object> cleared = new WeakReference<>(new Object(), q);
        cleared.clear();
        check(cleared.enqueue(), "cleared ref can still be enqueued manually");
        Reference<?> p1 = q.poll();
        check(p1 == cleared, "cleared ref polled");
        check(p1.get() == null, "cleared ref has a null referent");
        Reference<Object> nullRef = new WeakReference<Object>(null, q);
        check(nullRef.enqueue(), "null-referent ref can be enqueued");
        check(q.poll() == nullRef, "null-referent ref polled");
        check(!cleared.enqueue(), "already-drained ref cannot re-enqueue");
    }

    static void testCrossThreadHandoff() throws Exception {
        final ReferenceQueue<Object> q = new ReferenceQueue<>();
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        final Object[] result = new Object[1];
        final Reference<Object> r = new WeakReference<>(new Object(), q);
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    result[0] = q.remove();
                } catch (Throwable t) {
                    result[0] = t;
                } finally {
                    done.countDown();
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
        // deterministic either way: remove() returns an already-enqueued ref immediately,
        // or blocks until this enqueue becomes visible
        r.enqueue();
        check(done.await(10, java.util.concurrent.TimeUnit.SECONDS),
                "cross-thread remove finished within the bounded wait");
        check(result[0] == r, "blocked remove() was handed the enqueued reference");
        worker.join(10000);
    }

    static void testInterruptedRemove() throws Exception {
        final ReferenceQueue<Object> q = new ReferenceQueue<>();
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        final boolean[] interrupted = new boolean[1];
        final boolean[] returned = new boolean[1];
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    q.remove();
                    returned[0] = true;
                } catch (InterruptedException e) {
                    interrupted[0] = true;
                } catch (Throwable t) {
                    returned[0] = true; // unexpected path: fail below
                } finally {
                    done.countDown();
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
        // wait (bounded busy-check) until the worker is actually parked inside remove()
        boolean parked = false;
        for (int i = 0; i < 2000000 && !parked; i++) {
            Thread.State s = worker.getState();
            parked = (s == Thread.State.WAITING || s == Thread.State.TIMED_WAITING);
        }
        worker.interrupt();
        check(done.await(10, java.util.concurrent.TimeUnit.SECONDS),
                "interrupted remove finished within the bounded wait");
        check(interrupted[0], "remove() throws InterruptedException when interrupted while blocked");
        check(!returned[0], "remove() did not return normally after interruption");
        worker.join(10000);
        check(!worker.isAlive(), "worker thread terminated after interruption");
    }

    static void testGenericQueueVariance() {
        ReferenceQueue<Object> qObj = new ReferenceQueue<>();
        WeakReference<String> ws = new WeakReference<>("value", qObj);
        check(ws.enqueue(), "WeakReference<String> registers on ReferenceQueue<Object> (<? super T>)");
        check(qObj.poll() == ws, "poll returns the string reference");

        ReferenceQueue<Number> qNum = new ReferenceQueue<>();
        SoftReference<java.math.BigInteger> bi =
                new SoftReference<>(java.math.BigInteger.valueOf(7), qNum);
        check(bi.enqueue(), "SoftReference<BigInteger> registers on ReferenceQueue<Number>");
        check(qNum.poll() == bi, "poll returns the BigInteger reference");

        ReferenceQueue<Object> q = new ReferenceQueue<>();
        PhantomReference<String> ps = new PhantomReference<>("p", q);
        check(ps.enqueue(), "PhantomReference<String> registers on ReferenceQueue<Object>");
        Reference<? extends Object> polled = q.poll();
        check(polled == ps, "poll result type is Reference<? extends T>");
    }

    static void testQueueReuseAcrossCycles() throws Exception {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        for (int round = 0; round < 3; round++) {
            Reference<Object> r = new WeakReference<>(new Object(), q);
            check(r.enqueue(), "enqueue round " + round);
            check(q.remove(1) == r, "remove(1) drains round " + round);
            check(q.poll() == null, "empty between rounds " + round);
        }
    }

    static void testEnqueueOncePerReference() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Reference<Object> r = new WeakReference<>(new Object(), q);
        check(!r.isEnqueued(), "not enqueued before enqueue");
        check(r.enqueue(), "first enqueue returns true");
        check(r.isEnqueued(), "isEnqueued true while queued");
        check(!r.enqueue(), "second enqueue returns false");
        check(r.isEnqueued(), "isEnqueued stays true until polled");
        q.poll();
        check(!r.isEnqueued(), "isEnqueued false after poll");
        check(!r.enqueue(), "no re-enqueue after the reference was polled");
    }

    public static void main(String[] args) throws Exception {
        testEmptyQueue();
        testPollAndRemove();
        testEmptyPollRepeatAndTimeouts();
        testNegativeTimeoutExactMessage();
        testDrainSetSemantics();
        testClearedAndNullReferentRefs();
        testCrossThreadHandoff();
        testInterruptedRemove();
        testGenericQueueVariance();
        testQueueReuseAcrossCycles();
        testEnqueueOncePerReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
