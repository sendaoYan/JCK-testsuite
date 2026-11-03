/*
 * @test
 * @summary java.nio.channels.SeekableByteChannel conformance: white-box structure + black-box behavior.
 * @run main SeekableByteChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class SeekableByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        check(SeekableByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.ByteChannel.class.isAssignableFrom(SeekableByteChannel.class),
              "extends ByteChannel");
        Set<String> names = new HashSet<>();
        for (Method m : SeekableByteChannel.class.getMethods()) names.add(m.getName());
        for (String n : new String[] {"read", "write", "position", "size", "truncate"})
            check(names.contains(n), "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("sb.bin");
            Files.write(file, new byte[] {1, 2, 3, 4});

            // Files.newByteChannel returns the interface type: full seek/read/write cycle
            try (SeekableByteChannel sbc = Files.newByteChannel(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                check(sbc.size() == 4, "size 4");
                check(sbc.position() == 0, "starts at 0");

                // position(long) returns this for chaining
                check(sbc.position(2) == sbc, "position(long) returns this");
                ByteBuffer r = ByteBuffer.allocate(2);
                check(sbc.read(r) == 2, "read from position 2");
                check(r.get(0) == 3 && r.get(1) == 4, "tail bytes");
                check(sbc.position() == 4, "position advanced");

                // truncate shrinks and clamps position
                check(sbc.truncate(2) == sbc, "truncate returns this");
                check(sbc.size() == 2, "size 2 after truncate");
                check(sbc.position() == 2, "position clamped to size");

                // write extends the file
                sbc.write(ByteBuffer.wrap(new byte[] {9}));
                check(sbc.size() == 3, "write extended to 3");
                ByteBuffer all = ByteBuffer.allocate(3);
                sbc.position(0);
                check(sbc.read(all) == 3, "read all 3");
                check(all.get(0) == 1 && all.get(1) == 2 && all.get(2) == 9, "content after cycle");

                // seeking beyond size is legal; size unchanged until write
                sbc.position(10);
                check(sbc.position() == 10, "seek beyond size allowed");
                check(sbc.size() == 3, "size unchanged by seeking");
                sbc.write(ByteBuffer.wrap(new byte[] {7}));
                check(sbc.size() == 11, "sparse write extended file to 11");
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
