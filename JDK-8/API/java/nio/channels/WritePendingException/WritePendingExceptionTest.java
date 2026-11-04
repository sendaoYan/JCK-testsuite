/*
 * @test
 * @summary java.nio.channels.WritePendingException conformance: hierarchy and constructor.
 * @run main WritePendingExceptionTest
 */

import java.nio.channels.WritePendingException;

public class WritePendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWritePendingException() {
        check(IllegalStateException.class.isAssignableFrom(WritePendingException.class), "extends IllegalStateException");
        WritePendingException e = new WritePendingException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testWritePendingException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
