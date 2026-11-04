/*
 * @test
 * @summary java.nio.file.StandardOpenOption conformance: white-box structure + black-box behavior.
 * @run main StandardOpenOptionTest
 */

import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class StandardOpenOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.StandardOpenOption");
        check(c.isEnum(), "StandardOpenOption is an enum");
        check(c.getEnumConstants().length == 10, "exactly 10 constants");
        for (String n : new String[] {"READ", "WRITE", "APPEND", "TRUNCATE_EXISTING",
                "CREATE", "CREATE_NEW", "DELETE_ON_CLOSE", "SPARSE", "SYNC", "DSYNC"})
            if (!n.equals("SPARSE") || true)
                check(java.util.Arrays.stream(c.getEnumConstants())
                      .anyMatch(k -> ((Enum<?>) k).name().equals(n)), "constant " + n);
        check(java.nio.file.OpenOption.class.isAssignableFrom(c), "implements OpenOption");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(StandardOpenOption.values().length == 10, "values length");
        check(StandardOpenOption.values()[0] == StandardOpenOption.READ, "READ first");
        check(StandardOpenOption.valueOf("DELETE_ON_CLOSE")
              == StandardOpenOption.DELETE_ON_CLOSE, "valueOf");

        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("opt.txt");

            // CREATE_NEW refuses an existing file
            Files.writeString(f, "one");
            boolean refused = false;
            try (var ch = FileChannel.open(f, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE_NEW)) { }
            catch (java.nio.file.FileAlreadyExistsException e) { refused = true; }
            check(refused, "CREATE_NEW on existing -> FileAlreadyExistsException");

            // APPEND writes at the end
            try (var ch = FileChannel.open(f, StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND)) {
                ch.write(java.nio.ByteBuffer.wrap("two".getBytes()));
            }
            check(Files.readString(f).equals("onetwo"), "APPEND extends content");

            // TRUNCATE_EXISTING empties first
            try (var ch = FileChannel.open(f, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ch.write(java.nio.ByteBuffer.wrap("T".getBytes()));
            }
            check(Files.readString(f).equals("T"), "TRUNCATE_EXISTING resets");

            // READ-only channel rejects writes
            try (var ch = FileChannel.open(f, StandardOpenOption.READ)) {
                boolean rejected = false;
                try { ch.write(java.nio.ByteBuffer.wrap(new byte[] {1})); }
                catch (java.nio.channels.NonWritableChannelException e) { rejected = true; }
                check(rejected, "READ channel -> NonWritableChannelException");
            }

            // WRITE-only channel rejects reads
            try (var ch = FileChannel.open(f, StandardOpenOption.WRITE)) {
                boolean rejected = false;
                try { ch.read(java.nio.ByteBuffer.allocate(1)); }
                catch (java.nio.channels.NonReadableChannelException e) { rejected = true; }
                check(rejected, "WRITE channel -> NonReadableChannelException");
            }

            // DELETE_ON_CLOSE removes the file when the channel closes
            Path temp = dir.resolve("del.txt");
            Files.writeString(temp, "bye");
            try (var ch = FileChannel.open(temp, StandardOpenOption.READ,
                    StandardOpenOption.DELETE_ON_CLOSE)) { }
            check(!Files.exists(temp), "DELETE_ON_CLOSE removes the file");

            // missing file without CREATE -> NoSuchFileException
            boolean missing = false;
            try (var ch = FileChannel.open(dir.resolve("ghost"),
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
