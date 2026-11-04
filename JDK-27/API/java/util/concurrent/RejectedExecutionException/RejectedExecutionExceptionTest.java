/*
 * @test
 * @summary java.util.concurrent.RejectedExecutionException conformance: white-box structure + black-box behavior.
 * @run main RejectedExecutionExceptionTest
 */

import java.util.concurrent.*;

public class RejectedExecutionExceptionTest {
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
        Class<?> c = cls("java.util.concurrent.RejectedExecutionException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        hasCtor(c);
        hasCtor(c, String.class);
        hasCtor(c, String.class, Throwable.class);
        hasCtor(c, Throwable.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RejectedExecutionException e = new RejectedExecutionException("m");
        check(e.getMessage().equals("m"), "message");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        Throwable cause = new RuntimeException("root");
        RejectedExecutionException e2 = new RejectedExecutionException("x", cause);
        check(e2.getCause() == cause, "cause from (String,Throwable)");
        check(new RejectedExecutionException().getMessage() == null, "no-arg null message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
