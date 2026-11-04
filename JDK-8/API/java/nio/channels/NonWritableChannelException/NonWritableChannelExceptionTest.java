/*
 * @test
 * @summary java.nio.channels.NonWritableChannelException conformance: hierarchy and constructor.
 * @run main NonWritableChannelExceptionTest
 */

import java.nio.channels.NonWritableChannelException;

public class NonWritableChannelExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNonWritableChannelException() {
        check(IllegalStateException.class.isAssignableFrom(NonWritableChannelException.class), "extends IllegalStateException");
        NonWritableChannelException e = new NonWritableChannelException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testNonWritableChannelException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
