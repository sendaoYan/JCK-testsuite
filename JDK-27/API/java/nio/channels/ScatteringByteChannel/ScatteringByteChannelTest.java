/*
 * @test
 * @summary java.nio.channels.ScatteringByteChannel conformance: white-box structure + black-box behavior.
 * @run main ScatteringByteChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class ScatteringByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        check(ScatteringByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.ReadableByteChannel.class.isAssignableFrom(ScatteringByteChannel.class),
              "extends ReadableByteChannel");
        int overloads = 0;
        for (Method m : ScatteringByteChannel.class.getMethods())
            if (m.getName().equals("read") && m.getParameterCount() >= 1
                && m.getParameterTypes()[0].getComponentType() == ByteBuffer.class)
                overloads++;
        check(overloads >= 2, "read(ByteBuffer[]) and read(ByteBuffer[],int,int)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("s.bin");
            Files.write(file, new byte[] {1, 2, 3, 4, 5, 6});

            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.READ)) {
                ScatteringByteChannel sbc = fc;

                // full-array scatter: fills buffers sequentially
                ByteBuffer a = ByteBuffer.allocate(2);
                ByteBuffer b = ByteBuffer.allocate(3);
                ByteBuffer c = ByteBuffer.allocate(1);
                long n = sbc.read(new ByteBuffer[] {a, b, c});
                check(n == 6, "scattered 6 bytes");
                check(a.get(0) == 1 && a.get(1) == 2, "buffer a");
                check(b.get(0) == 3 && b.get(2) == 5, "buffer b");
                check(c.get(0) == 6, "buffer c");

                // (offset,length) form selects a subrange of the array
                fc.position(0);
                ByteBuffer x = ByteBuffer.allocate(2);
                ByteBuffer y = ByteBuffer.allocate(2);
                ByteBuffer z = ByteBuffer.allocate(2);
                long m = sbc.read(new ByteBuffer[] {x, y, z}, 1, 1);
                check(m == 2, "only buffer y received 2 bytes");
                check(x.position() == 0 && z.position() == 0, "x and z untouched");
                check(y.get(0) == 1 && y.get(1) == 2, "y got first bytes");

                // partial fill when stream ends mid-array
                fc.position(5);
                ByteBuffer p = ByteBuffer.allocate(4);
                ByteBuffer q = ByteBuffer.allocate(4);
                long partial = sbc.read(new ByteBuffer[] {p, q});
                check(partial == 1, "only 1 byte left to scatter");
                check(p.get(0) == 6 && p.position() == 1, "p holds the byte");
                check(q.position() == 0, "q empty");

                // EOF: -1
                fc.position(6);
                check(sbc.read(new ByteBuffer[] {ByteBuffer.allocate(1)}) == -1, "scatter EOF -1");
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
