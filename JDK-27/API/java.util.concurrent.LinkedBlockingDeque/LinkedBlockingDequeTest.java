/*
 * @test
 * @summary java.util.concurrent.LinkedBlockingDeque API conformance: add/peek/poll
 *          first/last.
 * @run main LinkedBlockingDequeTest
 */

import java.util.concurrent.LinkedBlockingDeque;

public class LinkedBlockingDequeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLinkedBlockingDeque() throws Exception {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>();
        d.addFirst(1);
        d.addLast(2);
        check(d.peekFirst() == 1 && d.peekLast() == 2, "peek first/last");
        check(d.pollFirst() == 1 && d.pollFirst() == 2, "poll first");
        check(d.isEmpty(), "empty");
    }

    public static void main(String[] args) throws Exception {
        testLinkedBlockingDeque();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
