/*
 * @test
 * @summary java.nio.file.AccessDeniedException conformance: hierarchy and constructor.
 * @run main AccessDeniedExceptionTest
 */

import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;

public class AccessDeniedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAccessDeniedException() {
        check(FileSystemException.class.isAssignableFrom(AccessDeniedException.class), "extends FileSystemException");
        AccessDeniedException e = new AccessDeniedException("f");
        check(e instanceof FileSystemException, "instanceof FileSystemException");
    }

    public static void main(String[] args) {
        testAccessDeniedException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
