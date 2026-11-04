/*
 * @test
 * @summary java.nio.channels.FileChannel$MapMode conformance: white-box structure + black-box behavior.
 * @run main MapModeTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
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

    // ---- A5 white-box additions: structure of the MapMode class ----
    static void testWhiteBoxA5() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.FileChannel$MapMode");
        check(c.getSuperclass() == Object.class, "extends Object directly");
        check(!java.lang.Enum.class.isAssignableFrom(c), "not a java.lang.Enum subtype");
        int publicStaticFinal = 0;
        for (Field f : c.getFields())
            if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                    && Modifier.isFinal(f.getModifiers()))
                publicStaticFinal++;
        check(publicStaticFinal == 3, "exactly the three exported constants");
        check(c.getFields().length == 3, "no other public fields");
        boolean privateCtor = true;
        for (Constructor<?> ct : c.getDeclaredConstructors())
            if (!Modifier.isPrivate(ct.getModifiers())) privateCtor = false;
        check(privateCtor && c.getDeclaredConstructors().length == 1,
              "single private ctor MapMode(String) (javap 实测)");
        boolean enumStyle = false;
        for (Method m : c.getMethods())
            if (m.getName().equals("values") || m.getName().equals("valueOf")) enumStyle = true;
        check(!enumStyle, "no enum-style values()/valueOf() methods");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    // ---- A5 black-box additions: constant identity / equals / hashCode / subrange map ----
    static void testBlackBoxA5() throws Exception {
        check(FileChannel.MapMode.READ_ONLY.equals(FileChannel.MapMode.READ_ONLY), "self equals");
        check(!FileChannel.MapMode.READ_ONLY.equals(FileChannel.MapMode.READ_WRITE), "distinct not equal");
        check(!FileChannel.MapMode.READ_ONLY.equals(FileChannel.MapMode.PRIVATE), "READ_ONLY != PRIVATE");
        check(!FileChannel.MapMode.READ_WRITE.equals(FileChannel.MapMode.PRIVATE), "READ_WRITE != PRIVATE");
        check(!FileChannel.MapMode.PRIVATE.equals(null), "equals(null) false");
        check(!FileChannel.MapMode.READ_ONLY.equals("READ_ONLY"), "not equal to same-named String");
        check(FileChannel.MapMode.READ_ONLY.hashCode() == FileChannel.MapMode.READ_ONLY.hashCode(),
              "hashCode stable across calls");
        check(FileChannel.MapMode.READ_ONLY.getClass() == FileChannel.MapMode.PRIVATE.getClass(),
              "all three constants share the MapMode class");

        Path dir = Files.createTempDirectory("jck-a5-mm");
        try {
            Path file = dir.resolve("mm2.bin");
            Files.write(file, new byte[] {10, 20, 30, 40});
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
                check(ch.map(FileChannel.MapMode.READ_ONLY, 2, 2).capacity() == 2,
                      "subrange map honours position+size");
                ByteBuffer slice = ch.map(FileChannel.MapMode.READ_ONLY, 1, 2);
                check(slice.get(0) == 20 && slice.get(1) == 30, "subrange content starts at position");
            }
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                MappedByteBuffer pv = ch.map(FileChannel.MapMode.PRIVATE, 0, 4);
                pv.put(2, (byte) 88);
                pv.force();
                check(pv.get(2) == 88, "PRIVATE map sees its own write");
                check(Files.readAllBytes(file)[2] == 30, "PRIVATE subrange write not visible in file");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxA5();
        testBlackBoxA5();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
