/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicIntegerArray API conformance: length, get/set,
 *          getAndIncrement/incrementAndGet, addAndGet, and compareAndSet.
 * @run main AtomicIntegerArrayTest
 */

import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicIntegerArrayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testArray() {
        AtomicIntegerArray a = new AtomicIntegerArray(3);
        check(a.length() == 3, "length");
        check(a.get(0) == 0, "initial 0");
        a.set(0, 5);
        check(a.get(0) == 5, "set/get");
        check(a.getAndIncrement(0) == 5 && a.get(0) == 6, "getAndIncrement");
        check(a.incrementAndGet(1) == 1, "incrementAndGet");
        check(a.addAndGet(0, 10) == 16, "addAndGet");
        check(a.compareAndSet(0, 16, 99) && a.get(0) == 99, "compareAndSet success");
        check(!a.compareAndSet(0, 0, 1) && a.get(0) == 99, "compareAndSet failure");
    }

    public static void main(String[] args) {
        testArray();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
