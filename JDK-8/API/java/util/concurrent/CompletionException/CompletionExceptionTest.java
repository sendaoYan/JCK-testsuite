/*
 * @test
 * @summary java.util.concurrent.CompletionException API conformance: cause and inheritance.
 * @run main CompletionExceptionTest
 */

import java.util.concurrent.CompletionException;

public class CompletionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCompletionException() {
        RuntimeException cause = new RuntimeException();
        CompletionException e = new CompletionException(cause);
        check(e instanceof RuntimeException, "extends RuntimeException");
        check(e.getCause() == cause, "getCause");

        CompletionException e2 = new CompletionException("m", cause);
        check("m".equals(e2.getMessage()), "message from (String,Throwable) ctor");
        check(e2.getCause() == cause, "cause from (String,Throwable) ctor");
    }

    public static void main(String[] args) {
        testCompletionException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
