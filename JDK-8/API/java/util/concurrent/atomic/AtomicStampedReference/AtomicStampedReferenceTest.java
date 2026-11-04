/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicStampedReference API conformance: reference
 *          and stamp accessors and compareAndSet.
 * @run main AtomicStampedReferenceTest
 */

import java.util.concurrent.atomic.AtomicStampedReference;

public class AtomicStampedReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAtomicStampedReference() {
        AtomicStampedReference<Integer> ref = new AtomicStampedReference<>(0, 100);
        check(ref.getReference() == 0, "getReference");
        check(ref.getStamp() == 100, "getStamp");
        ref.set(1, 101);
        check(ref.getReference() == 1 && ref.getStamp() == 101, "set");

        int[] stamp = new int[1];
        check(ref.get(stamp) == 1, "get with stamp holder");
        check(stamp[0] == 101, "stamp holder");

        boolean cas = ref.compareAndSet(1, 2, 101, 102);
        check(cas, "compareAndSet");
        check(ref.getReference() == 2 && ref.getStamp() == 102, "after CAS");
    }

    public static void main(String[] args) {
        testAtomicStampedReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
