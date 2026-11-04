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

    // ---- appended: full 10-constant enum matrix ----
    static void testEnumMatrix() {
        StandardOpenOption[] vals = StandardOpenOption.values();
        check(vals.length == 10, "exactly 10 constants");
        String[] expected = {"READ", "WRITE", "APPEND", "TRUNCATE_EXISTING", "CREATE",
                "CREATE_NEW", "DELETE_ON_CLOSE", "SPARSE", "SYNC", "DSYNC"};
        for (int i = 0; i < expected.length; i++)
            check(vals[i].name().equals(expected[i]) && vals[i].ordinal() == i,
                  "values[" + i + "] == " + expected[i] + " (order measured on both JDKs)");
        for (StandardOpenOption o : StandardOpenOption.values()) {
            check(StandardOpenOption.valueOf(o.name()) == o, "valueOf round-trip " + o);
            check(o.toString().equals(o.name()), "toString == name for " + o);
        }
        for (int i = 1; i < vals.length; i++)
            check(vals[i - 1].compareTo(vals[i]) < 0, "compareTo order at " + i);
        check(java.util.EnumSet.allOf(StandardOpenOption.class).size() == 10,
              "EnumSet allOf size 10");
        check(java.util.EnumSet.of(StandardOpenOption.READ, StandardOpenOption.WRITE).size() == 2,
              "EnumSet of two");
        boolean iae = false;
        try { StandardOpenOption.valueOf("read"); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "valueOf wrong case -> IllegalArgumentException");
    }

    // ---- appended: option-combination matrix (measured on Linux) ----
    static void testCombinationMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("comb.txt");
            Files.writeString(f, "abcdef");

            boolean iae1 = false;
            try (var ch = FileChannel.open(f, StandardOpenOption.READ,
                    StandardOpenOption.APPEND)) { }
            catch (IllegalArgumentException e) { iae1 = true; }
            check(iae1, "READ + APPEND -> IllegalArgumentException");

            boolean iae2 = false;
            try (var ch = FileChannel.open(f, StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND, StandardOpenOption.TRUNCATE_EXISTING)) { }
            catch (IllegalArgumentException e) { iae2 = true; }
            check(iae2, "WRITE + APPEND + TRUNCATE_EXISTING -> IllegalArgumentException");

            try (var ch = FileChannel.open(f, StandardOpenOption.APPEND)) {
                ch.write(java.nio.ByteBuffer.wrap("gh".getBytes()));
                check(ch.size() == 8, "APPEND alone writes at the end");
            }
            try (var ch = FileChannel.open(f, StandardOpenOption.TRUNCATE_EXISTING)) {
                check(ch.size() == 8, "TRUNCATE_EXISTING without WRITE keeps content");
            }
            try (var ch = FileChannel.open(f, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE)) {
                check(ch.size() == 8, "CREATE on existing keeps content");
            }
            Path fresh = dir.resolve("fresh.bin");
            try (var ch = FileChannel.open(fresh, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.SPARSE)) {
                check(ch.size() == 0, "SPARSE open via CREATE_NEW succeeds");
            }
            check(Files.exists(fresh), "SPARSE target created");
            try (var ch = FileChannel.open(dir.resolve("sync.bin"),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.SYNC)) {
                ch.write(java.nio.ByteBuffer.wrap("s".getBytes()));
                check(true, "SYNC write succeeds");
            }
            try (var ch = FileChannel.open(dir.resolve("dsync.bin"),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.DSYNC)) {
                ch.write(java.nio.ByteBuffer.wrap("d".getBytes()));
                check(true, "DSYNC write succeeds");
            }
        } finally {
            try (var st = Files.walk(dir)) {
                java.util.List<Path> l = new java.util.ArrayList<>();
                st.forEach(l::add);
                java.util.Collections.reverse(l);
                for (Path p : l) { try { Files.deleteIfExists(p); } catch (Exception ignore) { } }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumMatrix();
        testCombinationMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
