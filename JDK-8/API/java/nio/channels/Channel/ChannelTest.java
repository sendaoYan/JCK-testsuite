/*
 * @test
 * @summary java.nio.channels.Channel conformance: interface hierarchy and methods.
 * @run main ChannelTest
 */

import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
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

    static void testChannel() {
        check(Channel.class.isInterface(), "is interface");
        check(Closeable.class.isAssignableFrom(Channel.class), "extends Closeable");
        Set<String> names = new HashSet<String>();
        for (Method m : Channel.class.getMethods()) names.add(m.getName());
        String[] required = { "isOpen", "close" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    /** Minimal user implementation: the interface has no default methods to lean on. */
    static final class FlagChannel implements Channel {
        private boolean open = true;
        int closeCount;
        public boolean isOpen() { return open; }
        public void close() { open = false; closeCount++; }
    }

    // ---- A5 white-box: exact surface of the base interface ----
    static void testWhiteBoxA5() throws Exception {
        int own = 0;
        for (Method m : Channel.class.getMethods())
            if (m.getDeclaringClass() == Channel.class) own++;
        check(own == 2, "exactly isOpen and close declared on the interface");
        boolean anyDefault = false;
        for (Method m : Channel.class.getMethods())
            if (m.isDefault()) anyDefault = true;
        check(!anyDefault, "no default methods (implementers must supply both)");
        check(Channel.class.getMethod("isOpen").getReturnType() == boolean.class,
              "isOpen returns boolean");
        check(Channel.class.getMethod("close").getReturnType() == void.class, "close returns void");
        Class<?>[] ex = Channel.class.getMethod("close").getExceptionTypes();
        check(ex.length == 1 && ex[0] == IOException.class, "close declares throws IOException");
        check(java.lang.AutoCloseable.class.isAssignableFrom(Channel.class), "extends AutoCloseable");
        check(!ReadableByteChannel.class.isAssignableFrom(Channel.class),
              "base interface has no read direction");
    }

    // ---- A5 black-box: contract via custom impl and real channels ----
    static void testBlackBoxA5() throws Exception {
        FlagChannel fc = new FlagChannel();
        check(fc.isOpen(), "custom channel starts open");
        try (Channel c = fc) {
            check(c == fc && c.isOpen(), "usable inside try-with-resources");
        }
        check(!fc.isOpen(), "try-with-resources closed it exactly once");
        check(fc.closeCount == 1, "close invoked once");
        fc.close();
        check(fc.closeCount == 2 && !fc.isOpen(), "extra close tolerated by the implementation");

        Path dir = Files.createTempDirectory("jck-a5-ch");
        try {
            Path file = dir.resolve("ch.bin");
            Files.write(file, new byte[] {1, 2});
            Channel ch = FileChannel.open(file, StandardOpenOption.READ);
            check(ch.isOpen(), "file channel open seen as Channel");
            ReadableByteChannel rbc = (ReadableByteChannel) ch;
            ByteBuffer buf = ByteBuffer.allocate(2);
            check(rbc.read(buf) == 2, "I/O through the readable face");
            ch.close();
            check(!ch.isOpen(), "closed through the Channel reference");
            ch.close();
            check(!ch.isOpen(), "double close harmless");

            try (Channel nbc = Files.newByteChannel(file, StandardOpenOption.READ)) {
                check(nbc.isOpen(), "newByteChannel result is a Channel");
            }

            Channel dc = java.nio.channels.DatagramChannel.open();
            check(dc.isOpen(), "datagram channel open via Channel");
            dc.close();
            check(!dc.isOpen(), "datagram channel closed");

            java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
            try {
                Channel sink = pipe.sink(), source = pipe.source();
                check(sink.isOpen() && source.isOpen(), "pipe ends open");
                ((WritableByteChannel) sink).write(ByteBuffer.wrap(new byte[] {9}));
                ByteBuffer got = ByteBuffer.allocate(1);
                check(((ReadableByteChannel) source).read(got) == 1 && got.get(0) == 9,
                      "pipe I/O through Channel faces");
            } finally {
                try { pipe.sink().close(); } catch (Exception ignore) { }
                try { pipe.source().close(); } catch (Exception ignore) { }
            }
        } finally {
            try (java.util.stream.Stream<Path> s = Files.list(dir)) {
                for (Path p : s.collect(java.util.stream.Collectors.toList())) Files.deleteIfExists(p);
            }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testChannel();
        testWhiteBoxA5();
        testBlackBoxA5();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
