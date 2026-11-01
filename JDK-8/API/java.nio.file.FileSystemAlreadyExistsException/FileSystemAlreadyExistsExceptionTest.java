/*
 * @test
 * @summary java.nio.file.FileSystemAlreadyExistsException conformance: hierarchy and constructor.
 * @run main FileSystemAlreadyExistsExceptionTest
 */

import java.nio.file.FileSystemAlreadyExistsException;

public class FileSystemAlreadyExistsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileSystemAlreadyExistsException() {
        check(RuntimeException.class.isAssignableFrom(FileSystemAlreadyExistsException.class), "extends RuntimeException");
        FileSystemAlreadyExistsException e = new FileSystemAlreadyExistsException("m");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
    }

    public static void main(String[] args) {
        testFileSystemAlreadyExistsException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
