/*
 * @test
 * @summary java.util.concurrent.ArrayBlockingQueue API conformance: bounded offer/poll,
 *          remainingCapacity, and size.
 * @run main ArrayBlockingQueueTest
 */

import java.util.concurrent.ArrayBlockingQueue;

public class ArrayBlockingQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testArrayBlockingQueue() {
        ArrayBlockingQueue<String> q = new ArrayBlockingQueue<>(2);
        check(q.offer("a") && q.offer("b"), "offer within capacity");
        check(!q.offer("c"), "offer beyond capacity returns false");
        check(q.remainingCapacity() == 0, "remainingCapacity when full");
        check("a".equals(q.poll()), "poll");
        check(q.remainingCapacity() == 1, "remainingCapacity after poll");
        check(q.offer("c"), "offer after space freed");
        check(q.size() == 2, "size");
    }

    public static void main(String[] args) {
        testArrayBlockingQueue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
