/*
 * @test
 * @summary java.nio.file.DirectoryIteratorException conformance: IOException cause.
 * @run main DirectoryIteratorExceptionTest
 */

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.util.ConcurrentModificationException;

public class DirectoryIteratorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDirectoryIteratorException() {
        check(ConcurrentModificationException.class.isAssignableFrom(
                DirectoryIteratorException.class), "extends ConcurrentModificationException");
        IOException cause = new IOException("io");
        DirectoryIteratorException e = new DirectoryIteratorException(cause);
        check(e.getCause() == cause, "getCause returns the IOException");
    }

    public static void main(String[] args) {
        testDirectoryIteratorException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
