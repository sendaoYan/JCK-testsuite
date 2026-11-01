/*
 * @test
 * @summary java.nio.channels.OverlappingFileLockException conformance: hierarchy and constructor.
 * @run main OverlappingFileLockExceptionTest
 */

import java.nio.channels.OverlappingFileLockException;

public class OverlappingFileLockExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOverlappingFileLockException() {
        check(IllegalStateException.class.isAssignableFrom(OverlappingFileLockException.class), "extends IllegalStateException");
        OverlappingFileLockException e = new OverlappingFileLockException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testOverlappingFileLockException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
