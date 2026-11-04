/*
 * @test
 * @summary java.nio.file.NotDirectoryException conformance: hierarchy and constructor.
 * @run main NotDirectoryExceptionTest
 */

import java.nio.file.NotDirectoryException;
import java.nio.file.FileSystemException;

public class NotDirectoryExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNotDirectoryException() {
        check(FileSystemException.class.isAssignableFrom(NotDirectoryException.class), "extends FileSystemException");
        NotDirectoryException e = new NotDirectoryException("f");
        check(e instanceof FileSystemException, "instanceof FileSystemException");
    }

    public static void main(String[] args) {
        testNotDirectoryException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
