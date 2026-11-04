/*
 * @test
 * @summary java.util.EmptyStackException conformance: white-box structure + black-box behavior.
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

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.EmptyStackException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        c.getDeclaredConstructor();
        check(c.getDeclaredConstructors().length == 1, "single constructor");
    }

    static void testBlackBox() {
        check(new EmptyStackException().getMessage() == null, "no-arg message null");
        final Stack<String> s = new Stack<>();
        expectThrows(s::pop, EmptyStackException.class, "empty pop");
        s.push("x");
        check("x".equals(s.pop()), "non-empty pop works");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
