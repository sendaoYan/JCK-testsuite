/*
 * @test
 * @summary java.lang.ref.PhantomReference conformance: get always null, queue required.
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

    static void testPhantomReference() {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object o = new Object();
        PhantomReference<Object> pr = new PhantomReference<>(o, q);
        check(pr.get() == null, "get always null");
        check(!pr.isEnqueued(), "not enqueued initially");
    }

    public static void main(String[] args) {
        testPhantomReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
