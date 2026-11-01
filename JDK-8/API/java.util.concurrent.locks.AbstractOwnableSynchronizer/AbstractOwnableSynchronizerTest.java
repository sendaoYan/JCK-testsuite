/*
 * @test
 * @summary java.util.concurrent.locks.AbstractOwnableSynchronizer conformance: owner thread.
 * @run main AbstractOwnableSynchronizerTest
 */

import java.util.concurrent.locks.AbstractOwnableSynchronizer;

public class AbstractOwnableSynchronizerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class AOS extends AbstractOwnableSynchronizer {
        void set(Thread t) { setExclusiveOwnerThread(t); }
        Thread get() { return getExclusiveOwnerThread(); }
    }

    static void testAOS() {
        AOS aos = new AOS();
        check(aos.get() == null, "initial owner null");
        aos.set(Thread.currentThread());
        check(aos.get() == Thread.currentThread(), "set/get owner");
    }

    public static void main(String[] args) {
        testAOS();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
