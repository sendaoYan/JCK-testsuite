/*
 * @test
 * @summary java.util.Deque API conformance: addFirst/addLast, getFirst/getLast,
 *          removeFirst/removeLast, push/pop (LIFO), and peekFirst.
 * @run main DequeTest
 */

import java.util.ArrayDeque;
import java.util.Deque;

public class DequeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDeque() {
        Deque<String> d = new ArrayDeque<>();
        d.addFirst("a");
        d.addLast("b");
        check("a".equals(d.getFirst()) && "b".equals(d.getLast()), "getFirst/getLast");
        d.addFirst("x");
        check("x".equals(d.removeFirst()), "removeFirst");
        check("b".equals(d.removeLast()), "removeLast");
        d.push("top");
        check("top".equals(d.pop()), "push/pop LIFO");
        check("a".equals(d.peekFirst()), "peekFirst");
        check(d.size() == 1, "size");
    }

    public static void main(String[] args) {
        testDeque();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
