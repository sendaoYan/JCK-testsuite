/*
 * @test
 * @summary java.nio.channels.ClosedChannelException conformance: white-box structure + black-box behavior.
 * @run main ClosedChannelExceptionTest
 */

import java.nio.channels.ClosedChannelException;

public class ClosedChannelExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.io.IOException.class.isAssignableFrom(ClosedChannelException.class), "hierarchy: extends java.io.IOException");
        check(new ClosedChannelException() != null, "no-arg constructor");
        check(new ClosedChannelException().getMessage() == null, "no-arg message is null");
        check(new ClosedChannelException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("c.bin");
            java.nio.file.Files.write(f, new byte[] {1});
            java.nio.channels.SeekableByteChannel ch =
                java.nio.file.Files.newByteChannel(f, java.nio.file.StandardOpenOption.READ);
            ch.close();
            try {
                ch.read(java.nio.ByteBuffer.allocate(1));
                check(false, "read after close should throw");
            } catch (ClosedChannelException e) {
                passed++;
            }
            // thrown as the cause of AsynchronousCloseException family
            check(new java.nio.channels.AsynchronousCloseException()
                  instanceof ClosedChannelException, "family relationship");
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("c.bin"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
