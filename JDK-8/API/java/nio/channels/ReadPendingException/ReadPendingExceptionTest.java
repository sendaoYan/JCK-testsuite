/*
 * @test
 * @summary java.nio.channels.ReadPendingException conformance: hierarchy and constructor.
 * @run main ReadPendingExceptionTest
 */

import java.nio.channels.ReadPendingException;

public class ReadPendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReadPendingException() {
        check(IllegalStateException.class.isAssignableFrom(ReadPendingException.class), "extends IllegalStateException");
        ReadPendingException e = new ReadPendingException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testReadPendingException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
