/*
 * @test
 * @summary java.util.concurrent.CancellationException API conformance: constructors and inheritance.
 * @run main CancellationExceptionTest
 */

import java.util.concurrent.CancellationException;

public class CancellationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCancellationException() {
        CancellationException e = new CancellationException("m");
        check(e instanceof IllegalStateException, "extends IllegalStateException");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testCancellationException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
