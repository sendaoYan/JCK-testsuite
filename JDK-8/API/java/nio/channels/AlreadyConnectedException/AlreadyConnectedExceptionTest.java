/*
 * @test
 * @summary java.nio.channels.AlreadyConnectedException conformance: hierarchy and constructor.
 * @run main AlreadyConnectedExceptionTest
 */

import java.nio.channels.AlreadyConnectedException;

public class AlreadyConnectedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAlreadyConnectedException() {
        check(IllegalStateException.class.isAssignableFrom(AlreadyConnectedException.class), "extends IllegalStateException");
        AlreadyConnectedException e = new AlreadyConnectedException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testAlreadyConnectedException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
