/*
 * @test
 * @summary java.nio.channels.NotYetConnectedException conformance: hierarchy and constructor.
 * @run main NotYetConnectedExceptionTest
 */

import java.nio.channels.NotYetConnectedException;

public class NotYetConnectedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNotYetConnectedException() {
        check(IllegalStateException.class.isAssignableFrom(NotYetConnectedException.class), "extends IllegalStateException");
        NotYetConnectedException e = new NotYetConnectedException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testNotYetConnectedException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
