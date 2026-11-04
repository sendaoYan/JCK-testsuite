/*
 * @test
 * @summary java.nio.channels.FileLockInterruptionException conformance: hierarchy and constructor.
 * @run main FileLockInterruptionExceptionTest
 */

import java.nio.channels.FileLockInterruptionException;
import java.io.IOException;

public class FileLockInterruptionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileLockInterruptionException() {
        check(IOException.class.isAssignableFrom(FileLockInterruptionException.class), "extends IOException");
        FileLockInterruptionException e = new FileLockInterruptionException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IOException, "instanceof IOException");
    }

    public static void main(String[] args) {
        testFileLockInterruptionException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
