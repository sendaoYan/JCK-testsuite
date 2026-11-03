/*
 * @test
 * @summary java.nio.channels.Channel conformance: white-box structure + black-box behavior.
 * @run main ChannelTest
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class ChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        check(Channel.class.isInterface(), "is interface");
        check(java.io.Closeable.class.isAssignableFrom(Channel.class), "extends Closeable");
        check(java.lang.AutoCloseable.class.isAssignableFrom(Channel.class), "AutoCloseable");
        Set<String> names = new HashSet<>();
        for (Method m : Channel.class.getMethods()) names.add(m.getName());
        for (String n : new String[] {"isOpen", "close"})
            check(names.contains(n), "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // every FileChannel IS-A Channel: drive isOpen/close through the interface
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("ch.bin");
            Files.write(file, new byte[] {1});

            Channel ch = FileChannel.open(file, StandardOpenOption.READ);
            check(ch.isOpen(), "isOpen true on fresh channel");
            // the same object still supports typed I/O after being held as Channel
            check(ch instanceof java.nio.channels.ReadableByteChannel, "readable face exists");
            java.nio.channels.ReadableByteChannel rbc = (java.nio.channels.ReadableByteChannel) ch;
            ByteBuffer buf = ByteBuffer.allocate(1);
            check(rbc.read(buf) == 1 && buf.get(0) == 1, "I/O through interface view");
            ch.close();
            check(!ch.isOpen(), "isOpen false after Channel.close");

            // close is idempotent per Channel contract
            ch.close();
            check(!ch.isOpen(), "double close keeps channel closed");

            // SocketChannel and Pipe ends also satisfy the Channel contract
            Channel sock = java.nio.channels.SocketChannel.open();
            check(sock.isOpen(), "socket channel open via Channel");
            sock.close();
            check(!sock.isOpen(), "socket channel closed via Channel");
            java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
            try {
                Channel sink = pipe.sink(), source = pipe.source();
                check(sink.isOpen() && source.isOpen(), "pipe ends open");
                ((java.nio.channels.WritableByteChannel) sink).write(ByteBuffer.wrap(new byte[] {9}));
                ByteBuffer got = ByteBuffer.allocate(1);
                check(((java.nio.channels.ReadableByteChannel) source).read(got) == 1
                      && got.get(0) == 9, "pipe I/O via Channel");
            } finally {
                try { pipe.sink().close(); } catch (Exception ignore) { }
                try { pipe.source().close(); } catch (Exception ignore) { }
            }
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
