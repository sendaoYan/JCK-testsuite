/*
 * @test
 * @summary java.util.concurrent.locks.AbstractQueuedLongSynchronizer conformance: long state + queue.
 * @run main AbstractQueuedLongSynchronizerTest
 */

import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;

public class AbstractQueuedLongSynchronizerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class AQLS extends AbstractQueuedLongSynchronizer {
        void set(long v) { setState(v); }
        long get() { return getState(); }
        boolean cas(long e, long u) { return compareAndSetState(e, u); }
    }

    static void testAQLS() {
        AQLS aqls = new AQLS();
        check(aqls.get() == 0L, "initial state 0");
        aqls.set(3L);
        check(aqls.get() == 3L, "setState/getState");
        check(aqls.cas(3L, 4L), "compareAndSetState true");
        check(aqls.get() == 4L, "compareAndSetState applied");
        check(!aqls.cas(3L, 5L), "compareAndSetState false");
        check(!aqls.hasQueuedThreads(), "no queued threads");
        check(aqls.getQueueLength() == 0, "queue length 0");
        check(aqls.getQueuedThreads().isEmpty(), "no queued thread list");
    }

    public static void main(String[] args) {
        testAQLS();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
