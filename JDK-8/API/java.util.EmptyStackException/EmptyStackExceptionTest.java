/*
 * @test
 * @summary java.util.EmptyStackException API conformance: hierarchy, and Stack.pop()/peek()
 *          throwing it on an empty stack.
 * @run main EmptyStackExceptionTest
 */

import java.util.EmptyStackException;
import java.util.Stack;

public class EmptyStackExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        EmptyStackException e = new EmptyStackException();
        check(e instanceof RuntimeException, "is a RuntimeException");
    }

    static void testThrownByStack() {
        Stack<String> s = new Stack<>();
        try {
            s.pop();
            throw new AssertionError("pop on empty: expected EmptyStackException");
        } catch (EmptyStackException expected) {
            passed++;
        }
        try {
            s.peek();
            throw new AssertionError("peek on empty: expected EmptyStackException");
        } catch (EmptyStackException expected) {
            passed++;
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testThrownByStack();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
