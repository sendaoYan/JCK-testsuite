/*
 * @test
 * @summary java.nio.channels.OverlappingFileLockException conformance: white-box structure + black-box behavior.
 * @run main OverlappingFileLockExceptionTest
 */

import java.nio.channels.OverlappingFileLockException;

public class OverlappingFileLockExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(OverlappingFileLockException.class), "hierarchy: extends IllegalStateException");
        check(new OverlappingFileLockException() != null, "no-arg constructor");
        check(new OverlappingFileLockException().getMessage() == null, "no-arg message is null");
        check(new OverlappingFileLockException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("o.bin");
            java.nio.file.Files.write(f, new byte[16]);
            try (java.nio.channels.FileChannel ch = java.nio.channels.FileChannel.open(
                    f, java.nio.file.StandardOpenOption.READ, java.nio.file.StandardOpenOption.WRITE)) {
                java.nio.channels.FileLock first = ch.lock(0, 8, false);
                try {
                    ch.tryLock(4, 4, false);
                    check(false, "overlapping lock should throw");
                } catch (OverlappingFileLockException e) {
                    passed++;
                }
                first.release();
                check(ch.tryLock(4, 4, false) != null, "non-overlapping now allowed after release");
            }
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("o.bin"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
