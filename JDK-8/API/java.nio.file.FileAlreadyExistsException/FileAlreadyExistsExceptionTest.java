/*
 * @test
 * @summary java.nio.file.FileAlreadyExistsException conformance: hierarchy and constructor.
 * @run main FileAlreadyExistsExceptionTest
 */

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;

public class FileAlreadyExistsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileAlreadyExistsException() {
        check(FileSystemException.class.isAssignableFrom(FileAlreadyExistsException.class), "extends FileSystemException");
        FileAlreadyExistsException e = new FileAlreadyExistsException("f");
        check(e instanceof FileSystemException, "instanceof FileSystemException");
    }

    public static void main(String[] args) {
        testFileAlreadyExistsException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
