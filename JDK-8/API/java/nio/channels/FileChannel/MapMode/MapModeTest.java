/*
 * @test
 * @summary java.nio.channels.FileChannel.MapMode conformance: constants.
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
            try (java.util.stream.Stream<Path> s = Files.list(p)) {
                for (Path c : s.collect(java.util.stream.Collectors.toList())) deleteRecursively(c);
            }
        }
        Files.deleteIfExists(p);
    }

    static void testMapMode() {
        check(FileChannel.MapMode.READ_ONLY != null, "READ_ONLY");
        check(FileChannel.MapMode.READ_WRITE != null, "READ_WRITE");
        check(FileChannel.MapMode.PRIVATE != null, "PRIVATE");
        check(FileChannel.MapMode.READ_ONLY != FileChannel.MapMode.READ_WRITE, "distinct modes");
        check(FileChannel.MapMode.READ_ONLY.toString() != null, "toString");
    }

    // ---- A5 white-box: structure of the MapMode class (javap 实测) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.FileChannel$MapMode");
        check(c.getName().equals("java.nio.channels.FileChannel$MapMode"), "binary name");
        check(c.getSuperclass() == Object.class, "extends Object directly");
        check(!c.isEnum(), "isEnum false");
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
        boolean hasToString = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("toString")) hasToString = true;
        check(hasToString, "declares toString");
        boolean enumStyle = false;
        for (Method m : c.getMethods())
            if (m.getName().equals("values") || m.getName().equals("valueOf")) enumStyle = true;
        check(!enumStyle, "no enum-style values()/valueOf() methods");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    // ---- A5 black-box: constant identity, equals/hashCode, toString, map use ----
    static void testBlackBox() throws Exception {
        check(FileChannel.MapMode.READ_ONLY.toString().equals("READ_ONLY"), "READ_ONLY toString exact");
        check(FileChannel.MapMode.READ_WRITE.toString().equals("READ_WRITE"), "READ_WRITE toString exact");
        check(FileChannel.MapMode.PRIVATE.toString().equals("PRIVATE"), "PRIVATE toString exact");
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
            Path file = dir.resolve("mm.bin");
            Files.write(file, new byte[] {10, 20, 30, 40});
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
                MappedByteBuffer ro = ch.map(FileChannel.MapMode.READ_ONLY, 0, 4);
                check(ro.capacity() == 4, "READ_ONLY map capacity");
                check(ro.isReadOnly(), "READ_ONLY map buffer read-only");
                check(ro.get(0) == 10 && ro.get(3) == 40, "READ_ONLY map content");
                check(ch.map(FileChannel.MapMode.READ_ONLY, 2, 2).capacity() == 2,
                      "subrange map honours position+size");
            }
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                MappedByteBuffer rw = ch.map(FileChannel.MapMode.READ_WRITE, 0, 4);
                check(!rw.isReadOnly(), "READ_WRITE map buffer writable");
                rw.put(0, (byte) 99);
                rw.force();
                check(Files.readAllBytes(file)[0] == 99, "READ_WRITE map write persisted");
                MappedByteBuffer pv = ch.map(FileChannel.MapMode.PRIVATE, 0, 4);
                check(!pv.isReadOnly(), "PRIVATE map buffer writable");
                pv.put(1, (byte) 77);
                pv.force();
                check(pv.get(1) == 77, "PRIVATE map sees its own write");
                check(Files.readAllBytes(file)[1] == 20, "PRIVATE map write not visible in file");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testMapMode();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
