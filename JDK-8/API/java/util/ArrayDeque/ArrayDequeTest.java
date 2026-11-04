/*
 * @test
 * @summary java.util.ArrayDeque API conformance: add/peek/poll (FIFO), addFirst/addLast,
 *          peekLast, removeFirst/removeLast, size/contains, and null rejection.
 * @run main ArrayDequeTest
 */

import java.util.ArrayDeque;

public class ArrayDequeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testQueue() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        check(d.isEmpty(), "empty");
        check(d.add(1) && d.add(2), "add");
        check(d.peek() == 1, "peek head");
        check(d.poll() == 1, "poll removes head");
        check(d.peek() == 2, "peek after poll");
        check(!d.contains(1) && d.contains(2), "contains");
    }

    static void testDeque() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        d.addFirst(0);
        d.addLast(3);
        check(d.peek() == 0 && d.peekLast() == 3, "addFirst/addLast");
        check(d.size() == 2, "size");
        check(d.removeFirst() == 0 && d.removeLast() == 3, "removeFirst/removeLast");
        check(d.isEmpty(), "empty after removes");
    }

    static void testNullRejection() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        try {
            d.add(null);
            throw new AssertionError("add(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    public static void main(String[] args) {
        testQueue();
        testDeque();
        testNullRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
