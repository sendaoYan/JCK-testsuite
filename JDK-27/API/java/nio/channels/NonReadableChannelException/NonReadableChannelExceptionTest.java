/*
 * @test
 * @summary java.nio.channels.NonReadableChannelException conformance: white-box structure + black-box behavior.
 * @run main NonReadableChannelExceptionTest
 */

import java.nio.channels.NonReadableChannelException;

public class NonReadableChannelExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(NonReadableChannelException.class), "hierarchy: extends IllegalStateException");
        check(new NonReadableChannelException() != null, "no-arg constructor");
        check(new NonReadableChannelException().getMessage() == null, "no-arg message is null");
        check(new NonReadableChannelException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("nr.bin");
            java.nio.file.Files.write(f, new byte[] {1});
            try (java.nio.channels.FileChannel ch = java.nio.channels.FileChannel.open(
                    f, java.nio.file.StandardOpenOption.WRITE)) {
                try {
                    ch.read(java.nio.ByteBuffer.allocate(1));
                    check(false, "read on write-only should throw");
                } catch (NonReadableChannelException e) {
                    passed++;
                }
            }
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("nr.bin"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
