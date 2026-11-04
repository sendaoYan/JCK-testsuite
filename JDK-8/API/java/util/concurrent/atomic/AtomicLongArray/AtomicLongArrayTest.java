/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicLongArray API conformance: length, get/set,
 *          getAndIncrement/incrementAndGet, addAndGet, and compareAndSet.
 * @run main AtomicLongArrayTest
 */

import java.util.concurrent.atomic.AtomicLongArray;

public class AtomicLongArrayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testArray() {
        AtomicLongArray a = new AtomicLongArray(3);
        check(a.length() == 3, "length");
        check(a.get(0) == 0L, "initial 0");
        a.set(0, 5L);
        check(a.get(0) == 5L, "set/get");
        check(a.getAndIncrement(0) == 5L && a.get(0) == 6L, "getAndIncrement");
        check(a.incrementAndGet(1) == 1L, "incrementAndGet");
        check(a.addAndGet(0, 10L) == 16L, "addAndGet");
        check(a.compareAndSet(0, 16L, 99L) && a.get(0) == 99L, "compareAndSet success");
        check(!a.compareAndSet(0, 0L, 1L) && a.get(0) == 99L, "compareAndSet failure");
    }

    public static void main(String[] args) {
        testArray();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
