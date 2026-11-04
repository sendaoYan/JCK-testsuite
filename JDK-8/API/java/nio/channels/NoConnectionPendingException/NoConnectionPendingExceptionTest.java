/*
 * @test
 * @summary java.nio.channels.NoConnectionPendingException conformance: hierarchy and constructor.
 * @run main NoConnectionPendingExceptionTest
 */

import java.nio.channels.NoConnectionPendingException;

public class NoConnectionPendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNoConnectionPendingException() {
        check(IllegalStateException.class.isAssignableFrom(NoConnectionPendingException.class), "extends IllegalStateException");
        NoConnectionPendingException e = new NoConnectionPendingException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testNoConnectionPendingException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
