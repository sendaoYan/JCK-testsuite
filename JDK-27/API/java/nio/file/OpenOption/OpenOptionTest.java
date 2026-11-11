/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.OpenOption conformance: white-box structure + black-box behavior.
 * @run main OpenOptionTest
 */

import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class OpenOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.OpenOption");
        check(c.isInterface(), "OpenOption is a marker interface");
        check(c.getDeclaredMethods().length == 0, "no methods");
        check(java.nio.file.StandardOpenOption.class.getInterfaces().length > 0
              && java.nio.file.StandardOpenOption.class.getInterfaces()[0] == c,
              "StandardOpenOption implements it");
        check(java.nio.file.LinkOption.class.getInterfaces().length >= 1
              && java.util.Arrays.toString(java.nio.file.LinkOption.class.getInterfaces())
                     .contains("OpenOption"), "LinkOption implements it");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        OpenOption custom = new OpenOption() { };
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("o.txt");
            Files.writeString(f, "x");
            // unknown option rejected by FileChannel.open
            boolean uoe = false;
            try (var ignored = FileChannel.open(f, StandardOpenOption.READ, custom)) { }
            catch (UnsupportedOperationException e) { uoe = true; }
            check(uoe, "unknown OpenOption -> UnsupportedOperationException");

            // standard options accepted through the same slot
            try (var ch = FileChannel.open(f, StandardOpenOption.READ)) {
                check(ch.isOpen(), "standard option accepted");
            }

            // LinkOption doubles as an OpenOption for link-sensitive opens
            Files.createSymbolicLink(dir.resolve("lnk"), f);
            try (var ch = FileChannel.open(dir.resolve("lnk"),
                    java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
                check(false, "opening a link NOFOLLOW should fail");
            } catch (java.io.IOException e) {
                check(true, "NOFOLLOW open of a symlink -> IOException (XDEV family)");
            }
        } finally {
            Files.deleteIfExists(dir.resolve("lnk"));
            Files.deleteIfExists(dir.resolve("o.txt"));
            Files.deleteIfExists(dir);
        }
    }

    // ---- appended: full marker classification + option-slot edges ----
    static void testMarkerAndImplementors() throws Exception {
        Class<?> c = Class.forName("java.nio.file.OpenOption");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getDeclaredMethods().length == 0, "zero declared methods (marker)");
        int fromObject = 0;
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getDeclaringClass() != Object.class) fromObject++;
        check(fromObject == 0, "no methods beyond Object");

        for (StandardOpenOption o : StandardOpenOption.values())
            check(o instanceof OpenOption, "constant " + o + " is an OpenOption");
        boolean loImplements = false;
        for (Class<?> i : java.nio.file.LinkOption.class.getInterfaces())
            if (i == c) loImplements = true;
        check(loImplements, "LinkOption implements OpenOption");
        OpenOption anon = new OpenOption() { };
        check(anon instanceof OpenOption, "anonymous implementation allowed (marker)");
        check(!anon.equals(null), "marker instance not equal to null");
    }

    // ---- appended: option-slot edge cases ----
    static void testOptionSlotEdges() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("e.txt");
            Files.writeString(f, "abcdef");
            // no options at all: open() defaults to read
            try (FileChannel ch = FileChannel.open(f)) {
                check(ch.size() == 6, "open() with no options reads");
                boolean rejected = false;
                try { ch.write(java.nio.ByteBuffer.wrap(new byte[] {1})); }
                catch (java.nio.channels.NonWritableChannelException e) { rejected = true; }
                check(rejected, "default open is read-only");
            }
            // empty options array behaves the same
            try (FileChannel ch = FileChannel.open(f, new OpenOption[0])) {
                check(ch.size() == 6, "empty OpenOption[] reads");
            }
            // null option element -> NullPointerException (measured both JDKs)
            boolean npe = false;
            try (FileChannel ch = FileChannel.open(f, (OpenOption) null)) { }
            catch (NullPointerException e) { npe = true; }
            check(npe, "null OpenOption element -> NullPointerException");
            // NOFOLLOW on a regular file still opens (only symlinks fail)
            try (var ch = FileChannel.open(f, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
                check(ch.size() == 6, "NOFOLLOW on a regular file still opens");
            }
        } finally {
            try { Files.deleteIfExists(dir.resolve("e.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMarkerAndImplementors();
        testOptionSlotEdges();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
