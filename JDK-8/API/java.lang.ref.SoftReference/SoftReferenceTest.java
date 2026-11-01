/*
 * @test
 * @summary java.lang.ref.SoftReference conformance: constructors/get/clear.
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

    static void testSoftReference() {
        Object o = new Object();
        SoftReference<Object> sr = new SoftReference<>(o);
        check(sr.get() == o, "get");
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        SoftReference<Object> sr2 = new SoftReference<>(o, q);
        check(sr2.get() == o, "get with queue");
        sr2.clear();
        check(sr2.get() == null, "clear");
    }

    public static void main(String[] args) {
        testSoftReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
