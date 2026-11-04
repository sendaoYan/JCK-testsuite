/*
 * @test
 * @summary java.nio.channels.FileLockInterruptionException conformance: white-box structure + black-box behavior.
 * @run main FileLockInterruptionExceptionTest
 */

import java.nio.channels.FileLockInterruptionException;

public class FileLockInterruptionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.io.IOException.class.isAssignableFrom(FileLockInterruptionException.class), "hierarchy: extends java.io.IOException");
        check(new FileLockInterruptionException() != null, "no-arg constructor");
        check(new FileLockInterruptionException().getMessage() == null, "no-arg message is null");
        check(new FileLockInterruptionException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // constructor + state behavior; the interrupt race itself is not deterministic
        FileLockInterruptionException e = new FileLockInterruptionException();
        check(e.getMessage() == null, "no message");
        String s = e.toString();
        check(s.contains("FileLockInterruptionException"), "toString names the class");
        check(java.io.IOException.class.isAssignableFrom(e.getClass()), "IOException family");
        // fillInStackTrace yields a usable stack for logging
        check(e.getStackTrace() != null, "stack trace available");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
