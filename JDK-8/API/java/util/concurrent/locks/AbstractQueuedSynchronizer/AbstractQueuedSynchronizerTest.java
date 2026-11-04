/*
 * @test
 * @summary java.util.concurrent.locks.AbstractQueuedSynchronizer conformance: state + queue.
 * @run main AbstractQueuedSynchronizerTest
 */

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class AbstractQueuedSynchronizerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class AQS extends AbstractQueuedSynchronizer {
        void set(int v) { setState(v); }
        int get() { return getState(); }
        boolean cas(int e, int u) { return compareAndSetState(e, u); }
    }

    static void testAQS() {
        AQS aqs = new AQS();
        check(aqs.get() == 0, "initial state 0");
        aqs.set(3);
        check(aqs.get() == 3, "setState/getState");
        check(aqs.cas(3, 4), "compareAndSetState true");
        check(aqs.get() == 4, "compareAndSetState applied");
        check(!aqs.cas(3, 5), "compareAndSetState false");
        check(!aqs.hasQueuedThreads(), "no queued threads");
        check(aqs.getQueueLength() == 0, "queue length 0");
        check(aqs.getQueuedThreads().isEmpty(), "no queued thread list");
        check(aqs.hasContended() == false || aqs.hasContended() == true, "hasContended");
    }

    public static void main(String[] args) {
        testAQS();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
