/*
 * @test
 * @summary java.nio.file.NotLinkException conformance: hierarchy and constructor.
 * @run main NotLinkExceptionTest
 */

import java.nio.file.NotLinkException;
import java.nio.file.FileSystemException;

public class NotLinkExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNotLinkException() {
        check(FileSystemException.class.isAssignableFrom(NotLinkException.class), "extends FileSystemException");
        NotLinkException e = new NotLinkException("f");
        check(e instanceof FileSystemException, "instanceof FileSystemException");
    }

    public static void main(String[] args) {
        testNotLinkException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
