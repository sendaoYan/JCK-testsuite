/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.OpenOption conformance: hierarchy and methods.
 * @run main OpenOptionTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class OpenOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOpenOption() {
        check(OpenOption.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : OpenOption.class.getMethods()) names.add(m.getName());
        String[] required = {  };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    // ---- marker-interface contract + implementation classification ----
    static void testMarkerAndImplementors() throws Exception {
        Class<?> c = Class.forName("java.nio.file.OpenOption");
        check(Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getDeclaredMethods().length == 0, "zero declared methods (marker)");
        int fromObject = 0;
        for (Method m : c.getMethods())
            if (m.getDeclaringClass() != Object.class) fromObject++;
        check(fromObject == 0, "no methods beyond Object");

        for (StandardOpenOption o : StandardOpenOption.values())
            check(o instanceof OpenOption, "constant " + o + " is an OpenOption");
        boolean loImplements = false;
        for (Class<?> i : LinkOption.class.getInterfaces())
            if (i == c) loImplements = true;
        check(loImplements, "LinkOption implements OpenOption");
        OpenOption anon = new OpenOption() { };
        check(anon instanceof OpenOption, "anonymous implementation allowed (marker)");
        check(!anon.equals(null), "marker instance not equal to null");
    }

    // ---- black-box: the option slot drives open behavior ----
    static void testOptionSlotBehavior() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("o.txt");
            Files.write(f, "abcdef".getBytes(StandardCharsets.UTF_8));

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

            // unknown option rejected with the exact exception
            boolean uoe = false;
            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.READ,
                    new OpenOption() { })) { }
            catch (UnsupportedOperationException e) { uoe = true; }
            check(uoe, "unknown OpenOption -> UnsupportedOperationException");

            // null option element -> NullPointerException (measured both JDKs)
            boolean npe = false;
            try (FileChannel ch = FileChannel.open(f, (OpenOption) null)) { }
            catch (NullPointerException e) { npe = true; }
            check(npe, "null OpenOption element -> NullPointerException");

            // LinkOption travels through the OpenOption slot (O_NOFOLLOW)
            Path target = dir.resolve("target.txt");
            Files.write(target, "t".getBytes(StandardCharsets.UTF_8));
            Path link = dir.resolve("olink");
            Files.createSymbolicLink(link, target);
            boolean io = false;
            try (FileChannel ch = FileChannel.open(link, StandardOpenOption.READ,
                    LinkOption.NOFOLLOW_LINKS)) { }
            catch (java.io.IOException e) { io = true; }
            check(io, "READ + NOFOLLOW_LINKS on a symlink -> IOException");
            try (FileChannel ch = FileChannel.open(target, StandardOpenOption.READ,
                    LinkOption.NOFOLLOW_LINKS)) {
                check(ch.size() == 1, "NOFOLLOW on a regular file still opens");
            }
        } finally {
            for (String n : new String[] {"o.txt", "target.txt", "olink"})
                try { Files.deleteIfExists(dir.resolve(n)); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testOpenOption();
        testMarkerAndImplementors();
        testOptionSlotBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
