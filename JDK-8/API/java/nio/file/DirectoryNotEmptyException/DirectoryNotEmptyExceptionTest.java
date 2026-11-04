/*
 * @test
 * @summary java.nio.file.DirectoryNotEmptyException conformance: hierarchy and constructor.
 * @run main DirectoryNotEmptyExceptionTest
 */

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystemException;

public class DirectoryNotEmptyExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDirectoryNotEmptyException() {
        check(FileSystemException.class.isAssignableFrom(DirectoryNotEmptyException.class), "extends FileSystemException");
        DirectoryNotEmptyException e = new DirectoryNotEmptyException("f");
        check(e instanceof FileSystemException, "instanceof FileSystemException");
    }

    public static void main(String[] args) {
        testDirectoryNotEmptyException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
