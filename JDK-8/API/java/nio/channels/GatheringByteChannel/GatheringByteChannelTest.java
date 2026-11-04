/*
 * @test
 * @summary java.nio.channels.GatheringByteChannel conformance: white-box structure + black-box behavior.
 * @run main GatheringByteChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GatheringByteChannelTest {
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
        check(GatheringByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.WritableByteChannel.class.isAssignableFrom(GatheringByteChannel.class),
              "extends WritableByteChannel");
        int overloads = 0;
        for (Method m : GatheringByteChannel.class.getMethods())
            if (m.getName().equals("write") && m.getParameterTypes().length >= 1
                && m.getParameterTypes()[0].getComponentType() == ByteBuffer.class)
                overloads++;
        check(overloads >= 2, "write(ByteBuffer[]) and write(ByteBuffer[],int,int)");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("g.bin");

            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                GatheringByteChannel gbc = fc;

                ByteBuffer a = ByteBuffer.wrap(new byte[] {1, 2});
                ByteBuffer b = ByteBuffer.wrap(new byte[] {3});
                long n = gbc.write(new ByteBuffer[] {a, b});
                check(n == 3, "gathered 3 bytes");
                check(!a.hasRemaining() && !b.hasRemaining(), "both drained");

                ByteBuffer x = ByteBuffer.wrap(new byte[] {9});
                ByteBuffer y = ByteBuffer.wrap(new byte[] {8, 8});
                ByteBuffer z = ByteBuffer.wrap(new byte[] {7});
                long m = gbc.write(new ByteBuffer[] {x, y, z}, 1, 1);
                check(m == 2, "only y gathered (2 bytes)");
                check(x.hasRemaining() && z.hasRemaining(), "x and z not consumed");
                check(!y.hasRemaining(), "y drained");
            }
            check(java.util.Arrays.equals(Files.readAllBytes(file), new byte[] {1, 2, 3, 8, 8}),
                  "gathered file content in order");

            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND)) {
                GatheringByteChannel gbc = fc;
                check(gbc.write(new ByteBuffer[0]) == 0, "empty gather is 0");
                check(Files.readAllBytes(file).length == 5, "file unchanged");
            }
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
