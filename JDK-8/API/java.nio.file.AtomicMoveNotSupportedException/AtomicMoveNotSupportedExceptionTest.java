/*
 * @test
 * @summary java.nio.file.AtomicMoveNotSupportedException conformance: hierarchy and constructor.
 * @run main AtomicMoveNotSupportedExceptionTest
 */

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystemException;

public class AtomicMoveNotSupportedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAtomicMoveNotSupportedException() {
        check(FileSystemException.class.isAssignableFrom(AtomicMoveNotSupportedException.class), "extends FileSystemException");
        AtomicMoveNotSupportedException e = new AtomicMoveNotSupportedException("f", "o", "r");
        check(e instanceof FileSystemException, "instanceof FileSystemException");
    }

    public static void main(String[] args) {
        testAtomicMoveNotSupportedException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
