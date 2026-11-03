/*
 * @test
 * @summary java.util.concurrent.CompletionException conformance: white-box structure + black-box behavior.
 * @run main CompletionExceptionTest
 */

import java.util.concurrent.*;

public class CompletionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasCtor(Class<?> c, Class<?>... p) {
        try { c.getConstructor(p); } catch (NoSuchMethodException e) { throw new AssertionError("missing ctor on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.CompletionException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        hasCtor(c, String.class, Throwable.class);
        hasCtor(c, Throwable.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Throwable cause = new RuntimeException("root");
        CompletionException e = new CompletionException("m", cause);
        check(e.getMessage().equals("m"), "message");
        check(e.getCause() == cause, "cause");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        CompletionException e2 = new CompletionException(cause);
        check(e2.getCause() == cause, "cause-only ctor");
        check(e2.getMessage() != null, "(Throwable) ctor sets message to cause.toString()");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
