/*
 * @test
 * @summary java.util.concurrent.PriorityBlockingQueue API conformance: natural-order
 *          poll ordering.
 * @run main PriorityBlockingQueueTest
 */

import java.util.concurrent.PriorityBlockingQueue;

public class PriorityBlockingQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPriorityBlockingQueue() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        q.offer(3);
        q.offer(1);
        q.offer(2);
        check(q.poll() == 1, "poll 1");
        check(q.poll() == 2, "poll 2");
        check(q.poll() == 3, "poll 3");
    }

    public static void main(String[] args) {
        testPriorityBlockingQueue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
