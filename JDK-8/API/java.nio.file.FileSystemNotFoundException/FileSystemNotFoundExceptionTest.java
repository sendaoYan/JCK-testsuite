/*
 * @test
 * @summary java.nio.file.FileSystemNotFoundException conformance: hierarchy and constructor.
 * @run main FileSystemNotFoundExceptionTest
 */

import java.nio.file.FileSystemNotFoundException;

public class FileSystemNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileSystemNotFoundException() {
        check(RuntimeException.class.isAssignableFrom(FileSystemNotFoundException.class), "extends RuntimeException");
        FileSystemNotFoundException e = new FileSystemNotFoundException();
        check(e instanceof RuntimeException, "instanceof RuntimeException");
    }

    public static void main(String[] args) {
        testFileSystemNotFoundException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
