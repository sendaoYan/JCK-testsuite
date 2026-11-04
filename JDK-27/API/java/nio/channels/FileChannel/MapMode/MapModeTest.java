/*
 * @test
 * @summary java.nio.channels.FileChannel$MapMode conformance: white-box structure + black-box behavior.
 * @run main MapModeTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MapModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void deleteRecursively(Path p) throws Exception {
        if (Files.isDirectory(p)) {
            try (var s = Files.list(p)) { for (Path c : s.toList()) deleteRecursively(c); }
        }
        Files.deleteIfExists(p);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.FileChannel$MapMode");
        check(!Modifier.isInterface(c.getModifiers()) && !c.isEnum(), "MapMode is a class");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()) || Modifier.isPrivate(ct.getModifiers()),
                  "non-public ctor");
        for (String f : new String[] {"READ_ONLY", "READ_WRITE", "PRIVATE"})
            check(Modifier.isPublic(c.getField(f).getModifiers())
                  && Modifier.isStatic(c.getField(f).getModifiers())
                  && Modifier.isFinal(c.getField(f).getModifiers()), f + " is a constant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // canonical constants + toString
        check(FileChannel.MapMode.READ_ONLY.toString().equals("READ_ONLY"), "READ_ONLY name");
        check(FileChannel.MapMode.READ_WRITE.toString().equals("READ_WRITE"), "READ_WRITE name");
        check(FileChannel.MapMode.PRIVATE.toString().equals("PRIVATE"), "PRIVATE name");

        // each mode maps a region with distinct write visibility
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("m.bin");
            Files.write(file, new byte[] {1, 2, 3, 4});

            // READ_ONLY: buffer is read-only, writes rejected
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
                var ro = ch.map(FileChannel.MapMode.READ_ONLY, 0, 4);
                check(ro.isReadOnly(), "READ_ONLY buffer is read only");
                boolean threw = false;
                try { ro.put(0, (byte) 9); }
                catch (java.nio.ReadOnlyBufferException e) { threw = true; }
                check(threw, "put on READ_ONLY map -> ReadOnlyBufferException");
                check(ro.get(0) == 1, "content visible");
            }

            // READ_WRITE: modifications reach the file
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                var rw = ch.map(FileChannel.MapMode.READ_WRITE, 0, 4);
                rw.put(0, (byte) 77);
                rw.force();
                check(Files.readAllBytes(file)[0] == 77, "READ_WRITE write persisted");
            }

            // PRIVATE: modifications do not reach the file
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                var pv = ch.map(FileChannel.MapMode.PRIVATE, 0, 4);
                check(!pv.isReadOnly(), "PRIVATE buffer writable");
                pv.put(0, (byte) 88);
                pv.force();
                check(pv.get(0) == 88, "PRIVATE sees its own write");
                check(Files.readAllBytes(file)[0] == 77, "PRIVATE write not visible in file");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
