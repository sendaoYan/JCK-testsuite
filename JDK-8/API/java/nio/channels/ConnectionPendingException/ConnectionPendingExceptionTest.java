/*
 * @test
 * @summary java.nio.channels.ConnectionPendingException conformance: hierarchy and constructor.
 * @run main ConnectionPendingExceptionTest
 */

import java.nio.channels.ConnectionPendingException;

public class ConnectionPendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConnectionPendingException() {
        check(IllegalStateException.class.isAssignableFrom(ConnectionPendingException.class), "extends IllegalStateException");
        ConnectionPendingException e = new ConnectionPendingException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testConnectionPendingException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
