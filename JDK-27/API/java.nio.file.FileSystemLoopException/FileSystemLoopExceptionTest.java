/*
 * @test
 * @summary java.nio.file.FileSystemLoopException conformance: hierarchy and constructor.
 * @run main FileSystemLoopExceptionTest
 */

import java.nio.file.FileSystemLoopException;
import java.nio.file.FileSystemException;

public class FileSystemLoopExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileSystemLoopException() {
        check(FileSystemException.class.isAssignableFrom(FileSystemLoopException.class), "extends FileSystemException");
        FileSystemLoopException e = new FileSystemLoopException("f");
        check(e instanceof FileSystemException, "instanceof FileSystemException");
    }

    public static void main(String[] args) {
        testFileSystemLoopException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
