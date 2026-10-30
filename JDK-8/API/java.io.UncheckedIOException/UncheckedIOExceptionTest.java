/*
 * @test
 * @summary java.io.UncheckedIOException API conformance: extends RuntimeException, the
 *          (String, IOException) and (IOException) constructors, and getCause.
 * @run main UncheckedIOExceptionTest
 */

import java.io.IOException;
import java.io.UncheckedIOException;

public class UncheckedIOExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUncheckedIOException() {
        IOException cause = new IOException("cause");
        UncheckedIOException e = new UncheckedIOException("msg", cause);
        check(e instanceof RuntimeException, "extends RuntimeException");
        check("msg".equals(e.getMessage()), "(String, IOException) message");
        check(e.getCause() == cause, "getCause");
        UncheckedIOException e2 = new UncheckedIOException(cause);
        check(e2.getCause() == cause && cause.toString().equals(e2.getMessage()),
              "(IOException) constructor");
    }

    public static void main(String[] args) {
        testUncheckedIOException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
