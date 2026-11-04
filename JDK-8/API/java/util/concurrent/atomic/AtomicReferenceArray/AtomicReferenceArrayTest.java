/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicReferenceArray API conformance: get/set and
 *          compareAndSet.
 * @run main AtomicReferenceArrayTest
 */

import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicReferenceArrayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAtomicReferenceArray() {
        AtomicReferenceArray<String> arr = new AtomicReferenceArray<>(3);
        check(arr.length() == 3, "length");
        arr.set(0, "a");
        check("a".equals(arr.get(0)), "get");
        check(arr.compareAndSet(0, "a", "b"), "compareAndSet");
        check("b".equals(arr.get(0)), "after CAS");
        check(!arr.compareAndSet(0, "a", "c"), "CAS with stale expected fails");
    }

    public static void main(String[] args) {
        testAtomicReferenceArray();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
