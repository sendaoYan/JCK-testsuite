/*
 * @test
 * @summary java.nio.channels.NonReadableChannelException conformance: hierarchy and constructor.
 * @run main NonReadableChannelExceptionTest
 */

import java.nio.channels.NonReadableChannelException;

public class NonReadableChannelExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNonReadableChannelException() {
        check(IllegalStateException.class.isAssignableFrom(NonReadableChannelException.class), "extends IllegalStateException");
        NonReadableChannelException e = new NonReadableChannelException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testNonReadableChannelException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
