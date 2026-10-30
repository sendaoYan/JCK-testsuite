/*
 * @test
 * @summary java.util.Stack API conformance: push/pop (LIFO), peek (non-destructive),
 *          empty, search (1-based position from top), and EmptyStackException.
 * @run main StackTest
 */

import java.util.Stack;

public class StackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPushPop() {
        Stack<String> s = new Stack<>();
        check(s.empty(), "empty initially");
        check(s.push("a").equals("a") && s.push("b").equals("b"), "push returns element");
        check(!s.empty(), "!empty after push");
        check(s.pop().equals("b"), "pop LIFO");
        check(s.pop().equals("a"), "pop LIFO");
        check(s.empty(), "empty after pops");
        try {
            s.pop();
            throw new AssertionError("pop empty: expected EmptyStackException");
        } catch (java.util.EmptyStackException expected) {
            passed++;
        }
    }

    static void testPeekSearch() {
        Stack<String> s = new Stack<>();
        s.push("a");
        s.push("b");
        s.push("c");
        check(s.peek().equals("c"), "peek top without removing");
        check(s.size() == 3, "peek non-destructive");
        check(s.search("c") == 1, "search top == 1");
        check(s.search("b") == 2, "search middle == 2");
        check(s.search("a") == 3, "search bottom == 3");
        check(s.search("z") == -1, "search missing == -1");
    }

    public static void main(String[] args) {
        testPushPop();
        testPeekSearch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
