/*
 * @test
 * @summary java.nio.channels.CancelledKeyException conformance: hierarchy and constructor.
 * @run main CancelledKeyExceptionTest
 */

import java.nio.channels.CancelledKeyException;

public class CancelledKeyExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCancelledKeyException() {
        check(IllegalStateException.class.isAssignableFrom(CancelledKeyException.class), "extends IllegalStateException");
        CancelledKeyException e = new CancelledKeyException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testCancelledKeyException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
