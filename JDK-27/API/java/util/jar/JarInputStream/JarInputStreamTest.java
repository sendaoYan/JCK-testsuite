/*
 * @test
 * @summary java.util.jar.JarInputStream conformance: white-box structure + black-box behavior.
 * @run main JarInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
            check(false, "expected " + type.getSimpleName());
        } catch (Throwable t) {
            check(type.isInstance(t),
                  "expected " + type.getSimpleName() + ", got " + t.getClass().getSimpleName());
        }
    }

    static boolean hasConstructor(Class<?> c, Class<?>... params) {
        try { c.getConstructor(params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) bout.write(buf, 0, n);
        return bout.toByteArray();
    }

    static byte[] buildJarWithManifest() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "com.example.Main");
        try (JarOutputStream jout = new JarOutputStream(bout, mf)) {
            jout.putNextEntry(new JarEntry("a.txt"));
            jout.write("aaa".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();
            jout.putNextEntry(new JarEntry("b.txt"));
            jout.write("bbbb".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();
        }
        return bout.toByteArray();
    }

    static byte[] buildJarNoManifest() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (JarOutputStream jout = new JarOutputStream(bout)) {
            jout.putNextEntry(new JarEntry("c.txt"));
            jout.write("ccc".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();
        }
        return bout.toByteArray();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.jar.JarInputStream");
        check(!c.isInterface() && java.lang.reflect.Modifier.isPublic(c.getModifiers()),
              "JarInputStream is public concrete class");
        check(c.getSuperclass() == ZipInputStream.class, "extends java.util.zip.ZipInputStream");
        check(hasConstructor(c, InputStream.class), "JarInputStream(InputStream)");
        check(hasConstructor(c, InputStream.class, boolean.class), "JarInputStream(InputStream,boolean)");
        check(hasMethod(c, "getManifest"), "getManifest()");
        check(hasMethod(c, "getNextEntry"), "getNextEntry()");
        check(hasMethod(c, "getNextJarEntry"), "getNextJarEntry()");
        check(hasMethod(c, "read", byte[].class, int.class, int.class), "read(byte[],int,int)");
    }

    // ---- Part 2: black-box (behavioral) ----

    static void testWithManifest() throws Exception {
        byte[] data = buildJarWithManifest();
        try (JarInputStream jin = new JarInputStream(new ByteArrayInputStream(data))) {
            Manifest m = jin.getManifest();
            check(m != null, "manifest present");
            check("1.0".equals(m.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
                  "manifest version");
            check("com.example.Main".equals(m.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS)),
                  "manifest main class");

            List<String> names = new ArrayList<String>();
            List<String> contents = new ArrayList<String>();
            JarEntry e;
            while ((e = jin.getNextJarEntry()) != null) {
                names.add(e.getName());
                contents.add(new String(readAll(jin), StandardCharsets.UTF_8));
            }
            check(names.equals(Arrays.asList("a.txt", "b.txt")), "entry order: " + names);
            check(contents.equals(Arrays.asList("aaa", "bbbb")), "entry contents");
        }
    }

    static void testGetNextEntry() throws Exception {
        byte[] data = buildJarWithManifest();
        try (JarInputStream jin = new JarInputStream(new ByteArrayInputStream(data))) {
            check(jin.getManifest() != null, "manifest present before getNextEntry");
            ZipEntry e = jin.getNextEntry();
            check(e != null && "a.txt".equals(e.getName()), "getNextEntry skips manifest, first is a.txt");
            byte[] buf = new byte[16];
            int n = jin.read(buf, 0, buf.length);
            check(n == 3 && "aaa".equals(new String(buf, 0, n, StandardCharsets.UTF_8)),
                  "read(byte[],int,int) entry content");
        }
    }

    static void testWithoutManifest() throws Exception {
        byte[] data = buildJarNoManifest();
        try (JarInputStream jin = new JarInputStream(new ByteArrayInputStream(data))) {
            check(jin.getManifest() == null, "no manifest");
            JarEntry e1 = jin.getNextJarEntry();
            check(e1 != null && "c.txt".equals(e1.getName()), "first jar entry");
            check(Arrays.equals(readAll(jin), "ccc".getBytes(StandardCharsets.UTF_8)),
                  "read content");
            check(jin.getNextJarEntry() == null, "no more entries");
        }
    }

    static void testEmptyJar() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (JarOutputStream jout = new JarOutputStream(bout)) { /* empty */ }
        try (JarInputStream jin = new JarInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            check(jin.getManifest() == null, "empty jar no manifest");
            check(jin.getNextJarEntry() == null, "empty jar no entries");
        }
    }

    static void testBlackBox() throws Exception {
        testWithManifest();
        testGetNextEntry();
        testWithoutManifest();
        testEmptyJar();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
