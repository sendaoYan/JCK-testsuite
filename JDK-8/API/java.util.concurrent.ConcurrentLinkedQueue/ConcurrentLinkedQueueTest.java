/*
 * @test
 * @summary java.util.concurrent.ConcurrentLinkedQueue API conformance: offer/poll/peek
 *          return semantics, size, and null rejection.
 * @run main ConcurrentLinkedQueueTest
 */

import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentLinkedQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testQueue() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();
        check(q.offer("a") && q.offer("b"), "offer");
        check("a".equals(q.peek()), "peek");
        check("a".equals(q.poll()), "poll");
        check("b".equals(q.poll()) && q.poll() == null, "poll to empty");
        check(q.peek() == null && q.size() == 0, "empty");
    }

    static void testNullRejection() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();
        try {
            q.offer(null);
            throw new AssertionError("offer(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    public static void main(String[] args) {
        testQueue();
        testNullRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
