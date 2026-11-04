/*
 * @test
 * @summary java.nio.channels.ClosedByInterruptException conformance: hierarchy and constructor.
 * @run main ClosedByInterruptExceptionTest
 */

import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.AsynchronousCloseException;

public class ClosedByInterruptExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClosedByInterruptException() {
        check(AsynchronousCloseException.class.isAssignableFrom(ClosedByInterruptException.class), "extends AsynchronousCloseException");
        ClosedByInterruptException e = new ClosedByInterruptException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof AsynchronousCloseException, "instanceof AsynchronousCloseException");
    }

    public static void main(String[] args) {
        testClosedByInterruptException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
