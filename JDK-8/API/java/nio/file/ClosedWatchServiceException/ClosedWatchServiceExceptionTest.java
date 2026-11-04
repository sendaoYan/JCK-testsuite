/*
 * @test
 * @summary java.nio.file.ClosedWatchServiceException conformance: hierarchy and constructor.
 * @run main ClosedWatchServiceExceptionTest
 */

import java.nio.file.ClosedWatchServiceException;

public class ClosedWatchServiceExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClosedWatchServiceException() {
        check(IllegalStateException.class.isAssignableFrom(ClosedWatchServiceException.class), "extends IllegalStateException");
        ClosedWatchServiceException e = new ClosedWatchServiceException();
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testClosedWatchServiceException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
