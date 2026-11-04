/*
 * @test
 * @summary java.nio.channels.ReadableByteChannel conformance: white-box structure + black-box behavior.
 * @run main ReadableByteChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadableByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void cleanup(Path dir) throws Exception {
        try (Stream<Path> s = Files.list(dir)) {
            for (Path p : s.collect(Collectors.toList())) Files.deleteIfExists(p);
        }
        Files.deleteIfExists(dir);
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        check(ReadableByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.Channel.class.isAssignableFrom(ReadableByteChannel.class),
              "extends Channel");
        Set<String> names = new HashSet<>();
        for (Method m : ReadableByteChannel.class.getMethods()) names.add(m.getName());
        check(names.contains("read"), "has read");
        check(!names.contains("write"), "no write in ReadableByteChannel");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("r.bin");
            Files.write(file, new byte[] {10, 20, 30});

            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.READ)) {
                ReadableByteChannel rbc = fc;
                ByteBuffer a = ByteBuffer.allocate(2);
                check(rbc.read(a) == 2, "first read fills 2");
                check(a.get(0) == 10 && a.get(1) == 20, "first chunk content");
                ByteBuffer b = ByteBuffer.allocate(4);
                check(rbc.read(b) == 1, "second read returns remaining 1");
                check(b.get(0) == 30, "last byte content");
                ByteBuffer c = ByteBuffer.allocate(2);
                check(rbc.read(c) == -1, "read at EOF returns -1");
                check(c.position() == 0, "EOF read leaves buffer untouched");
            }

            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.READ)) {
                ReadableByteChannel rbc = fc;
                boolean threw = false;
                try { rbc.read(ByteBuffer.allocate(4).asReadOnlyBuffer()); }
                catch (IllegalArgumentException e) { threw = true; }
                check(threw, "read into read-only buffer -> IllegalArgumentException");
            }

            FileChannel fc2 = FileChannel.open(file, StandardOpenOption.READ);
            ReadableByteChannel rbc2 = fc2;
            fc2.close();
            boolean threw = false;
            try { rbc2.read(ByteBuffer.allocate(1)); }
            catch (java.nio.channels.ClosedChannelException e) { threw = true; }
            check(threw, "read after close -> ClosedChannelException");
        } finally {
            cleanup(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
