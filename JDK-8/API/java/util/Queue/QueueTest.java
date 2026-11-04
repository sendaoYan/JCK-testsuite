/*
 * @test
 * @summary java.util.Queue API conformance: offer/poll/peek return semantics on a non-empty
 *          and empty queue, and element()/remove() throwing NoSuchElementException.
 * @run main QueueTest
 */

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

public class QueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOfferPollPeek() {
        Queue<String> q = new ArrayDeque<>();
        check(q.offer("a") && q.offer("b"), "offer returns true");
        check("a".equals(q.peek()), "peek head");
        check("a".equals(q.poll()), "poll removes head");
        check("b".equals(q.peek()), "peek after poll");
        check("b".equals(q.poll()), "poll remaining");
        check(q.poll() == null, "poll on empty returns null");
        check(q.peek() == null, "peek on empty returns null");
    }

    static void testElementRemove() {
        Queue<String> q = new ArrayDeque<>();
        q.offer("x");
        check("x".equals(q.element()), "element returns head");
        check("x".equals(q.remove()), "remove removes head");
        try {
            q.element();
            throw new AssertionError("element on empty: expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            passed++;
        }
        try {
            q.remove();
            throw new AssertionError("remove on empty: expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            passed++;
        }
    }

    public static void main(String[] args) {
        testOfferPollPeek();
        testElementRemove();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
