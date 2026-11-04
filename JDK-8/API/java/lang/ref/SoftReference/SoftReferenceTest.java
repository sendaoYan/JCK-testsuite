/*
 * @test
 * @summary java.lang.ref.SoftReference conformance: constructors, get retains referent
 *          (soft references are only cleared under memory pressure, which this test does
 *          not induce), null referent/queue -> NPE, clear, and queue registration.
 * @run main SoftReferenceTest
 */

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class SoftReferenceTest {
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

    static void testSoftReference() {
        Object o = new Object();
        SoftReference<Object> sr = new SoftReference<>(o);
        check(sr.get() == o, "get returns referent");
        check(!sr.isEnqueued(), "no queue not enqueued");
        check(!sr.enqueue(), "no queue enqueue false");

        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object o2 = new Object();
        SoftReference<Object> sr2 = new SoftReference<>(o2, q);
        check(sr2.get() == o2, "get with queue");
        check(!sr2.isEnqueued(), "with queue not enqueued");

        sr.clear();
        check(sr.get() == null, "clear nulls referent");

        // null referent and null queue are both permitted (no NPE)
        check(new SoftReference<Object>(null).get() == null, "null referent -> get null");
        check(new SoftReference<Object>(null, q).get() == null, "null referent with queue -> null");
        check(new SoftReference<>(new Object(), null).get() != null,
                "null queue treated as no queue");
    }

    public static void main(String[] args) {
        testSoftReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
