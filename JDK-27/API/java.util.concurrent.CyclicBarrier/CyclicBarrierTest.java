/*
 * @test
 * @summary java.util.concurrent.CyclicBarrier API conformance: getParties, getNumberWaiting,
 *          await on a single-party barrier, and isBroken.
 * @run main CyclicBarrierTest
 */

import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCyclicBarrier() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(1);
        check(barrier.getParties() == 1, "getParties");
        check(barrier.getNumberWaiting() == 0, "getNumberWaiting");
        check(barrier.await() == 0, "await on single-party returns 0");
        check(!barrier.isBroken(), "not broken");
    }

    public static void main(String[] args) throws Exception {
        testCyclicBarrier();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
