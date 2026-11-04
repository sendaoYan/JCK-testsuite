/*
 * @test
 * @summary java.util.concurrent.BlockingQueue API conformance: offer/poll/take/remainingCapacity.
 * @run main BlockingQueueTest
 */

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlockingQueue() throws Exception {
        BlockingQueue<String> q = new LinkedBlockingQueue<String>(2);
        check(q.offer("a") && q.offer("b"), "offer");
        check(!q.offer("c"), "offer beyond capacity returns false");
        check(q.remainingCapacity() == 0, "remainingCapacity 0 when full");
        check("a".equals(q.poll()), "poll");
        check("b".equals(q.poll(1, TimeUnit.SECONDS)), "poll with timeout");
        check(q.poll() == null, "poll on empty returns null");
    }

    public static void main(String[] args) throws Exception {
        testBlockingQueue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
