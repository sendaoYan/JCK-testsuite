/*
 * @test
 * @summary java.util.concurrent.LinkedTransferQueue API conformance: offer/poll FIFO.
 * @run main LinkedTransferQueueTest
 */

import java.util.concurrent.LinkedTransferQueue;

public class LinkedTransferQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLinkedTransferQueue() {
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        q.offer(1);
        q.offer(2);
        check(q.poll() == 1 && q.poll() == 2, "FIFO poll");
        check(q.isEmpty(), "empty");
    }

    public static void main(String[] args) {
        testLinkedTransferQueue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
