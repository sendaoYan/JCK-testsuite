/*
 * @test
 * @summary java.util.concurrent.RejectedExecutionException API conformance: constructors and inheritance.
 * @run main RejectedExecutionExceptionTest
 */

import java.util.concurrent.RejectedExecutionException;

public class RejectedExecutionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRejectedExecutionException() {
        RejectedExecutionException e = new RejectedExecutionException("m");
        check(e instanceof RuntimeException, "extends RuntimeException");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testRejectedExecutionException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
