/*
 * @test
 * @summary java.nio.file.StandardOpenOption conformance: white-box structure + black-box behavior (JDK-8).
 * @run main StandardOpenOptionTest
 */

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class StandardOpenOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.StandardOpenOption");
        check(c.isEnum(), "StandardOpenOption is an enum");
        check(c.getEnumConstants().length == 10, "exactly 10 constants");
        for (String n : new String[] {"READ", "WRITE", "APPEND", "TRUNCATE_EXISTING",
                "CREATE", "CREATE_NEW", "DELETE_ON_CLOSE", "SPARSE", "SYNC", "DSYNC"})
            check(java.util.Arrays.stream(c.getEnumConstants())
                  .anyMatch(k -> ((Enum<?>) k).name().equals(n)), "constant " + n);
        check(java.nio.file.OpenOption.class.isAssignableFrom(c), "implements OpenOption");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        check(StandardOpenOption.values().length == 10, "values length");
        check(StandardOpenOption.values()[0] == StandardOpenOption.READ, "READ first");
        check(StandardOpenOption.valueOf("DELETE_ON_CLOSE")
              == StandardOpenOption.DELETE_ON_CLOSE, "valueOf");

        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("opt.txt");

            Files.write(f, "one".getBytes(StandardCharsets.UTF_8));
            boolean refused = false;
            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE_NEW)) { }
            catch (java.nio.file.FileAlreadyExistsException e) { refused = true; }
            check(refused, "CREATE_NEW on existing -> FileAlreadyExistsException");

            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND)) {
                ch.write(ByteBuffer.wrap("two".getBytes(StandardCharsets.UTF_8)));
            }
            check(new String(Files.readAllBytes(f), StandardCharsets.UTF_8).equals("onetwo"),
                  "APPEND extends content");

            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ch.write(ByteBuffer.wrap("T".getBytes(StandardCharsets.UTF_8)));
            }
            check(new String(Files.readAllBytes(f), StandardCharsets.UTF_8).equals("T"),
                  "TRUNCATE_EXISTING resets");

            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.READ)) {
                boolean rejected = false;
                try { ch.write(ByteBuffer.wrap(new byte[] {1})); }
                catch (java.nio.channels.NonWritableChannelException e) { rejected = true; }
                check(rejected, "READ channel -> NonWritableChannelException");
            }

            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.WRITE)) {
                boolean rejected = false;
                try { ch.read(ByteBuffer.allocate(1)); }
                catch (java.nio.channels.NonReadableChannelException e) { rejected = true; }
                check(rejected, "WRITE channel -> NonReadableChannelException");
            }

            Path temp = dir.resolve("del.txt");
            Files.write(temp, "bye".getBytes(StandardCharsets.UTF_8));
            try (FileChannel ch = FileChannel.open(temp, StandardOpenOption.READ,
                    StandardOpenOption.DELETE_ON_CLOSE)) { }
            check(!Files.exists(temp), "DELETE_ON_CLOSE removes the file");

            boolean missing = false;
            try (FileChannel ch = FileChannel.open(dir.resolve("ghost"),
                    StandardOpenOption.READ)) { }
            catch (java.nio.file.NoSuchFileException e) { missing = true; }
            check(missing, "READ missing without CREATE -> NoSuchFileException");
        } finally {
            Files.deleteIfExists(dir.resolve("opt.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
