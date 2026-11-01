/*
 * @test
 * @summary java.nio.file.ClosedDirectoryStreamException conformance: hierarchy and constructor.
 * @run main ClosedDirectoryStreamExceptionTest
 */

import java.nio.file.ClosedDirectoryStreamException;

public class ClosedDirectoryStreamExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClosedDirectoryStreamException() {
        check(IllegalStateException.class.isAssignableFrom(ClosedDirectoryStreamException.class), "extends IllegalStateException");
        ClosedDirectoryStreamException e = new ClosedDirectoryStreamException();
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testClosedDirectoryStreamException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
