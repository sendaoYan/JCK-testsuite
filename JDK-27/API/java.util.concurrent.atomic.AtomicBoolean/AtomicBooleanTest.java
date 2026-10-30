/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicBoolean API conformance: get/set, getAndSet,
 *          and compareAndSet success/failure.
 * @run main AtomicBooleanTest
 */

import java.util.concurrent.atomic.AtomicBoolean;

public class AtomicBooleanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAtomicBoolean() {
        AtomicBoolean b = new AtomicBoolean();
        check(!b.get(), "initial false");
        b.set(true);
        check(b.get(), "set/get");
        check(b.getAndSet(false) && !b.get(), "getAndSet");
        check(b.compareAndSet(false, true) && b.get(), "compareAndSet success");
        check(!b.compareAndSet(false, true) && b.get(), "compareAndSet failure");
    }

    public static void main(String[] args) {
        testAtomicBoolean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
