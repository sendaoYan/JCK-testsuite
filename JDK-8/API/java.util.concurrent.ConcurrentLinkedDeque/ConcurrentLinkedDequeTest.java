/*
 * @test
 * @summary java.util.concurrent.ConcurrentLinkedDeque API conformance: first/last
 *          offer/peek/poll.
 * @run main ConcurrentLinkedDequeTest
 */

import java.util.concurrent.ConcurrentLinkedDeque;

public class ConcurrentLinkedDequeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConcurrentLinkedDeque() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        d.offerFirst(1);
        d.offerLast(2);
        check(d.peekFirst() == 1 && d.peekLast() == 2, "peek first/last");
        check(d.pollFirst() == 1 && d.pollFirst() == 2, "poll first");
        check(d.isEmpty(), "empty after poll");
    }

    public static void main(String[] args) {
        testConcurrentLinkedDeque();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
