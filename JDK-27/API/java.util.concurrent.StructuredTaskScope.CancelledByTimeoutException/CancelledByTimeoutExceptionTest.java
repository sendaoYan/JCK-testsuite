/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope.CancelledByTimeoutException
 *          conformance: constructor and superclass.
 * @run main CancelledByTimeoutExceptionTest
 * @enablePreview
 */

import java.util.concurrent.StructuredTaskScope;

public class CancelledByTimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(RuntimeException.class.isAssignableFrom(
                StructuredTaskScope.CancelledByTimeoutException.class),
              "extends RuntimeException");
        check(!StructuredTaskScope.CancelledByTimeoutException.class.isInterface(),
              "is a final class");

        StructuredTaskScope.CancelledByTimeoutException e =
                new StructuredTaskScope.CancelledByTimeoutException();
        check(e != null, "no-arg constructor");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
