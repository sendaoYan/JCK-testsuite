/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicLong API conformance: get/set, getAndSet,
 *          increment/decrement, addAndGet, and compareAndSet success/failure.
 * @run main AtomicLongTest
 */

import java.util.concurrent.atomic.AtomicLong;

public class AtomicLongTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAtomicLong() {
        AtomicLong a = new AtomicLong();
        check(a.get() == 0L, "initial 0");
        a.set(5L);
        check(a.get() == 5L, "set/get");
        check(a.getAndSet(7L) == 5L && a.get() == 7L, "getAndSet");
        check(a.incrementAndGet() == 8L, "incrementAndGet");
        check(a.getAndIncrement() == 8L && a.get() == 9L, "getAndIncrement");
        check(a.decrementAndGet() == 8L, "decrementAndGet");
        check(a.addAndGet(10L) == 18L, "addAndGet");
        check(a.compareAndSet(18L, 99L) && a.get() == 99L, "compareAndSet success");
        check(!a.compareAndSet(0L, 1L) && a.get() == 99L, "compareAndSet failure");
    }

    public static void main(String[] args) {
        testAtomicLong();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
