/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicInteger API conformance: get/set, getAndSet,
 *          increment/decrement, addAndGet, and compareAndSet success/failure.
 * @run main AtomicIntegerTest
 */

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAtomicInteger() {
        AtomicInteger a = new AtomicInteger();
        check(a.get() == 0, "initial 0");
        a.set(5);
        check(a.get() == 5, "set/get");
        check(a.getAndSet(7) == 5 && a.get() == 7, "getAndSet");
        check(a.incrementAndGet() == 8, "incrementAndGet");
        check(a.getAndIncrement() == 8 && a.get() == 9, "getAndIncrement");
        check(a.decrementAndGet() == 8, "decrementAndGet");
        check(a.addAndGet(10) == 18, "addAndGet");
        check(a.compareAndSet(18, 99) && a.get() == 99, "compareAndSet success");
        check(!a.compareAndSet(0, 1) && a.get() == 99, "compareAndSet failure");
    }

    public static void main(String[] args) {
        testAtomicInteger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
