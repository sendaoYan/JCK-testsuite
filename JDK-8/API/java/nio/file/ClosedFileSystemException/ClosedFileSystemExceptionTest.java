/*
 * @test
 * @summary java.nio.file.ClosedFileSystemException conformance: hierarchy and constructor.
 * @run main ClosedFileSystemExceptionTest
 */

import java.nio.file.ClosedFileSystemException;

public class ClosedFileSystemExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClosedFileSystemException() {
        check(IllegalStateException.class.isAssignableFrom(ClosedFileSystemException.class), "extends IllegalStateException");
        ClosedFileSystemException e = new ClosedFileSystemException();
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testClosedFileSystemException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
