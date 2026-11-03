/*
 * @test
 * @summary java.nio.channels.AsynchronousChannel conformance: white-box structure + black-box behavior.
 * @run main AsynchronousChannelTest
 */

import java.lang.reflect.Method;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class AsynchronousChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.AsynchronousChannel");
        check(c.isInterface(), "AsynchronousChannel is an interface");
        check(java.nio.channels.Channel.class.isAssignableFrom(c), "extends Channel");
        check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals("close")),
              "declares close");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // close() through the interface type on a real asynchronous file channel
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("a.bin");
            Files.write(file, new byte[] {1});
            AsynchronousChannel ch = AsynchronousFileChannel.open(file, StandardOpenOption.READ);
            check(ch instanceof java.nio.channels.Channel, "is a Channel");
            check(((java.nio.channels.Channel) ch).isOpen(), "open via Channel face");
            ch.close(); // interface close
            check(!((java.nio.channels.Channel) ch).isOpen(), "closed via AsynchronousChannel.close");

            // further I/O on the closed channel fails
            boolean threw = false;
            try {
                ((AsynchronousFileChannel) ch).read(java.nio.ByteBuffer.allocate(1), 0).get();
            } catch (Exception e) {
                threw = true;
            }
            check(threw, "read after close fails");

            // the same contract holds for async socket channels
            AsynchronousChannel sock = java.nio.channels.AsynchronousSocketChannel.open();
            check(sock instanceof java.nio.channels.AsynchronousSocketChannel, "async socket");
            sock.close();
            check(!((java.nio.channels.Channel) sock).isOpen(), "async socket closed");
        } finally {
            try (var s = Files.list(dir)) { s.forEach(p -> { try { Files.delete(p); } catch (Exception ignore) { } }); }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
