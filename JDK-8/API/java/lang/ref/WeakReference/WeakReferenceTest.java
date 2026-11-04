/*
 * @test
 * @summary java.lang.ref.WeakReference conformance: constructors/get/clear.
 * @run main WeakReferenceTest
 */

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class WeakReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWeakReference() {
        Object o = new Object();
        WeakReference<Object> wr = new WeakReference<>(o);
        check(wr.get() == o, "get");
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        WeakReference<Object> wr2 = new WeakReference<>(o, q);
        check(wr2.get() == o, "get with queue");
        wr2.clear();
        check(wr2.get() == null, "clear");
    }

    public static void main(String[] args) {
        testWeakReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
