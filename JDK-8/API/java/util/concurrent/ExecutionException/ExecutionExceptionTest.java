/*
 * @test
 * @summary java.util.concurrent.ExecutionException API conformance: cause and inheritance.
 * @run main ExecutionExceptionTest
 */

import java.util.concurrent.ExecutionException;

public class ExecutionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testExecutionException() {
        RuntimeException cause = new RuntimeException();
        ExecutionException e = new ExecutionException(cause);
        check(e instanceof Exception, "extends Exception");
        check(e.getCause() == cause, "getCause");
    }

    public static void main(String[] args) {
        testExecutionException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
