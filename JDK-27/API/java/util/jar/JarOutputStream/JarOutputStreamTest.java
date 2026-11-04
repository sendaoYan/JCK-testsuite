/*
 * @test
 * @summary java.util.jar.JarOutputStream conformance: white-box structure + black-box behavior.
 * @run main JarOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JarOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable action) {
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

    static void deleteRecursively(Path dir) {
        try (java.util.stream.Stream<Path> s = Files.walk(dir)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception ignore) { }
            });
        } catch (Exception ignore) { }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.jar.JarOutputStream");
        check(!c.isInterface() && java.lang.reflect.Modifier.isPublic(c.getModifiers()),
              "JarOutputStream is public concrete class");
        check(c.getSuperclass() == ZipOutputStream.class, "extends java.util.zip.ZipOutputStream");
        check(hasConstructor(c, OutputStream.class), "JarOutputStream(OutputStream)");
        check(hasConstructor(c, OutputStream.class, Manifest.class), "JarOutputStream(OutputStream,Manifest)");
        check(hasMethod(c, "putNextEntry", ZipEntry.class), "putNextEntry(ZipEntry)");
    }

    // ---- Part 2: black-box (behavioral) ----

    static void testManifestFirstEntry() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "com.example.Main");
        try (JarOutputStream jout = new JarOutputStream(bout, mf)) {
            jout.putNextEntry(new JarEntry("e.txt"));
            jout.write("data".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();
        }
        byte[] data = bout.toByteArray();
        check(data.length > 0, "jar output produced");

        try (JarInputStream jin = new JarInputStream(new ByteArrayInputStream(data))) {
            check(jin.getManifest() != null, "manifest written as first entry");
            check("1.0".equals(jin.getManifest().getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
                  "manifest version");
            JarEntry e = jin.getNextJarEntry();
            check(e != null && "e.txt".equals(e.getName()), "first data entry after manifest");
            check(Arrays.equals(readAll(jin), "data".getBytes(StandardCharsets.UTF_8)),
                  "data content equality");
        }

        expectThrows(NullPointerException.class,
                () -> new JarOutputStream(new ByteArrayOutputStream(), (Manifest) null));
    }

    static void testNoManifest() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (JarOutputStream jout = new JarOutputStream(bout)) {
            jout.putNextEntry(new JarEntry("a.txt"));
            jout.write("aaa".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();
        }
        try (JarInputStream jin = new JarInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            check(jin.getManifest() == null, "no manifest written");
            JarEntry e = jin.getNextJarEntry();
            check(e != null && "a.txt".equals(e.getName()), "single entry");
            check(Arrays.equals(readAll(jin), "aaa".getBytes(StandardCharsets.UTF_8)), "content");
        }
    }

    static void testStoredEntryAndRoundTrip(Path jar) throws Exception {
        byte[] storedData = "abc".getBytes(StandardCharsets.UTF_8);
        CRC32 crc = new CRC32();
        crc.update(storedData);

        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (OutputStream os = Files.newOutputStream(jar);
             JarOutputStream jout = new JarOutputStream(os, mf)) {
            // deflated entry (default method)
            jout.putNextEntry(new JarEntry("deflated.txt"));
            jout.write("deflated-content".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();

            // STORED entry with precomputed size/crc
            JarEntry stored = new JarEntry("stored.txt");
            stored.setMethod(ZipEntry.STORED);
            stored.setSize(storedData.length);
            stored.setCompressedSize(storedData.length);
            stored.setCrc(crc.getValue());
            jout.putNextEntry(stored);
            jout.write(storedData);
            jout.closeEntry();
        }

        // round-trip verification with JarFile
        try (JarFile jf = new JarFile(jar.toFile())) {
            check(jf.getManifest() != null, "manifest read back");
            JarEntry deflated = jf.getJarEntry("deflated.txt");
            check(Arrays.equals(readAll(jf.getInputStream(deflated)),
                  "deflated-content".getBytes(StandardCharsets.UTF_8)), "deflated content round-trip");

            JarEntry stored = jf.getJarEntry("stored.txt");
            check(stored != null && stored.getMethod() == ZipEntry.STORED, "stored entry method");
            check(stored.getSize() == 3 && stored.getCrc() == crc.getValue(),
                  "stored entry size/crc preserved");
            check(Arrays.equals(readAll(jf.getInputStream(stored)), storedData),
                  "stored content round-trip equality");
        }

        // also verify via JarInputStream, ordered by central directory order
        try (JarInputStream jin = new JarInputStream(Files.newInputStream(jar))) {
            List<String> names = new ArrayList<String>();
            JarEntry e;
            while ((e = jin.getNextJarEntry()) != null) {
                names.add(e.getName());
                readAll(jin); // consume
            }
            check(names.equals(Arrays.asList("deflated.txt", "stored.txt")),
                  "entries ordered deflated then stored: " + names);
        }
    }

    static void testBlackBox() throws Exception {
        testManifestFirstEntry();
        testNoManifest();

        Path dir = Files.createTempDirectory("jck");
        try {
            Path jar = dir.resolve("out.jar");
            testStoredEntryAndRoundTrip(jar);
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
