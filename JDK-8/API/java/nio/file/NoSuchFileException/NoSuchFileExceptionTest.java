/*
 * @test
 * @summary java.nio.file.NoSuchFileException conformance: hierarchy and constructor.
 * @run main NoSuchFileExceptionTest
 */

import java.nio.file.NoSuchFileException;
import java.nio.file.FileSystemException;

public class NoSuchFileExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNoSuchFileException() {
        check(FileSystemException.class.isAssignableFrom(NoSuchFileException.class), "extends FileSystemException");
        NoSuchFileException e = new NoSuchFileException("f");
        check(e instanceof FileSystemException, "instanceof FileSystemException");
    }

    public static void main(String[] args) {
        testNoSuchFileException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
