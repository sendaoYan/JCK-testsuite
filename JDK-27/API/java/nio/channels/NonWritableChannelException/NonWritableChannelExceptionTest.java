/*
 * @test
 * @summary java.nio.channels.NonWritableChannelException conformance: white-box structure + black-box behavior.
 * @run main NonWritableChannelExceptionTest
 */

import java.nio.channels.NonWritableChannelException;

public class NonWritableChannelExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(NonWritableChannelException.class), "hierarchy: extends IllegalStateException");
        check(new NonWritableChannelException() != null, "no-arg constructor");
        check(new NonWritableChannelException().getMessage() == null, "no-arg message is null");
        check(new NonWritableChannelException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("nw.bin");
            java.nio.file.Files.write(f, new byte[] {1});
            try (java.nio.channels.FileChannel ch = java.nio.channels.FileChannel.open(
                    f, java.nio.file.StandardOpenOption.READ)) {
                try {
                    ch.write(java.nio.ByteBuffer.wrap(new byte[] {2}));
                    check(false, "write on read-only should throw");
                } catch (NonWritableChannelException e) {
                    passed++;
                }
            }
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("nw.bin"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
