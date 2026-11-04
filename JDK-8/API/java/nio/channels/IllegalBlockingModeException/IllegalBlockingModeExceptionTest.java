/*
 * @test
 * @summary java.nio.channels.IllegalBlockingModeException conformance: hierarchy and constructor.
 * @run main IllegalBlockingModeExceptionTest
 */

import java.nio.channels.IllegalBlockingModeException;

public class IllegalBlockingModeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalBlockingModeException() {
        check(IllegalStateException.class.isAssignableFrom(IllegalBlockingModeException.class), "extends IllegalStateException");
        IllegalBlockingModeException e = new IllegalBlockingModeException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testIllegalBlockingModeException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
