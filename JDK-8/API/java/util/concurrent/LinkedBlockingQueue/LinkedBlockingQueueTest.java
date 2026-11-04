/*
 * @test
 * @summary java.util.concurrent.LinkedBlockingQueue API conformance: bounded offer/poll/peek,
 *          and size.
 * @run main LinkedBlockingQueueTest
 */

import java.util.concurrent.LinkedBlockingQueue;

public class LinkedBlockingQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLinkedBlockingQueue() {
        LinkedBlockingQueue<String> q = new LinkedBlockingQueue<>(2);
        check(q.offer("a") && q.offer("b"), "offer");
        check(!q.offer("c"), "offer beyond capacity returns false");
        check("a".equals(q.poll()), "poll");
        check(q.size() == 1, "size");
        check("b".equals(q.peek()), "peek");
    }

    public static void main(String[] args) {
        testLinkedBlockingQueue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
