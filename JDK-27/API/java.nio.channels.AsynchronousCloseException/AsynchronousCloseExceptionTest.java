/*
 * @test
 * @summary java.nio.channels.AsynchronousCloseException conformance: hierarchy and constructor.
 * @run main AsynchronousCloseExceptionTest
 */

import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;

public class AsynchronousCloseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousCloseException() {
        check(ClosedChannelException.class.isAssignableFrom(AsynchronousCloseException.class), "extends ClosedChannelException");
        AsynchronousCloseException e = new AsynchronousCloseException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof ClosedChannelException, "instanceof ClosedChannelException");
    }

    public static void main(String[] args) {
        testAsynchronousCloseException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
