/*
 * @test
 * @summary java.nio.channels.ByteChannel conformance: white-box structure + black-box behavior.
 * @run main ByteChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class ByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        check(ByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.ReadableByteChannel.class.isAssignableFrom(ByteChannel.class),
              "extends ReadableByteChannel");
        check(java.nio.channels.WritableByteChannel.class.isAssignableFrom(ByteChannel.class),
              "extends WritableByteChannel");
        Set<String> names = new HashSet<>();
        for (Method m : ByteChannel.class.getMethods()) names.add(m.getName());
        check(names.contains("read") && names.contains("write"), "inherits read+write");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("b.bin");
            Files.write(file, new byte[] {5, 6, 7});

            // read+write through the same ByteChannel reference
            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                ByteChannel bc = fc;
                ByteBuffer r = ByteBuffer.allocate(3);
                check(bc.read(r) == 3, "read 3 via ByteChannel");
                check(r.get(0) == 5 && r.get(2) == 7, "read content");
                fc.position(0);
                check(bc.write(ByteBuffer.wrap(new byte[] {8})) == 1, "write 1 via ByteChannel");
                ByteBuffer v = ByteBuffer.allocate(3);
                fc.position(0);
                check(bc.read(v) == 3, "re-read 3");
                check(v.get(0) == 8 && v.get(1) == 6 && v.get(2) == 7, "overwrite visible");
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
