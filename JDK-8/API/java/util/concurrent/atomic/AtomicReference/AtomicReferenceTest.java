/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicReference API conformance: get/set, getAndSet,
 *          and compareAndSet success/failure.
 * @run main AtomicReferenceTest
 */

import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAtomicReference() {
        AtomicReference<String> r = new AtomicReference<>();
        check(r.get() == null, "initial null");
        r.set("a");
        check("a".equals(r.get()), "set/get");
        check("a".equals(r.getAndSet("b")) && "b".equals(r.get()), "getAndSet");
        check(r.compareAndSet("b", "c") && "c".equals(r.get()), "compareAndSet success");
        check(!r.compareAndSet("x", "y") && "c".equals(r.get()), "compareAndSet failure");
    }

    public static void main(String[] args) {
        testAtomicReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
