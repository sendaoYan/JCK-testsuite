/*
 * @test
 * @summary java.nio.channels.WritableByteChannel conformance: white-box structure + black-box behavior.
 * @run main WritableByteChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class WritableByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        check(WritableByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.Channel.class.isAssignableFrom(WritableByteChannel.class),
              "extends Channel");
        Set<String> names = new HashSet<>();
        for (Method m : WritableByteChannel.class.getMethods()) names.add(m.getName());
        check(names.contains("write"), "has write");
        check(!names.contains("read"), "no read in WritableByteChannel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("w.bin");

            // write via a FileChannel viewed as the interface
            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                WritableByteChannel wbc = fc;
                ByteBuffer a = ByteBuffer.wrap(new byte[] {1, 2, 3});
                check(wbc.write(a) == 3, "wrote 3 bytes");
                check(!a.hasRemaining(), "buffer fully consumed");
                ByteBuffer b = ByteBuffer.wrap(new byte[] {4});
                check(wbc.write(b) == 1, "wrote 1 more");
            }
            check(java.util.Arrays.equals(Files.readAllBytes(file), new byte[] {1, 2, 3, 4}),
                  "file content after interface writes");

            // append via interface view continues after existing content
            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND)) {
                WritableByteChannel wbc = fc;
                check(wbc.write(ByteBuffer.wrap(new byte[] {5})) == 1, "appended 1");
            }
            check(Files.readAllBytes(file).length == 5, "file grew to 5");

            // write on closed channel
            FileChannel fc2 = FileChannel.open(file, StandardOpenOption.WRITE);
            WritableByteChannel wbc2 = fc2;
            fc2.close();
            boolean threw = false;
            try { wbc2.write(ByteBuffer.allocate(1)); }
            catch (java.nio.channels.ClosedChannelException e) { threw = true; }
            check(threw, "write after close -> ClosedChannelException");
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
