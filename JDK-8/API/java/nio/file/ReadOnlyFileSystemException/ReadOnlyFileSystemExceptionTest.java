/*
 * @test
 * @summary java.nio.file.ReadOnlyFileSystemException conformance: hierarchy and constructor.
 * @run main ReadOnlyFileSystemExceptionTest
 */

import java.nio.file.ReadOnlyFileSystemException;

public class ReadOnlyFileSystemExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReadOnlyFileSystemException() {
        check(UnsupportedOperationException.class.isAssignableFrom(ReadOnlyFileSystemException.class), "extends UnsupportedOperationException");
        ReadOnlyFileSystemException e = new ReadOnlyFileSystemException();
        check(e instanceof UnsupportedOperationException, "instanceof UnsupportedOperationException");
    }

    public static void main(String[] args) {
        testReadOnlyFileSystemException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
