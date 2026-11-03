/*
 * @test
 * @summary java.util.jar.JarFile conformance: white-box structure + black-box behavior.
 * @run main JarFileTest
 */

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarFileTest {
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

    static List<String> entryNames(JarFile jf) {
        List<String> names = new ArrayList<String>();
        Enumeration<JarEntry> e = jf.entries();
        while (e.hasMoreElements()) names.add(e.nextElement().getName());
        Collections.sort(names);
        return names;
    }

    static void deleteRecursively(Path dir) {
        try (java.util.stream.Stream<Path> s = Files.walk(dir)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception ignore) { }
            });
        } catch (Exception ignore) { }
    }

    static void writeSimpleJar(Path path) throws Exception {
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "com.example.Main");
        Attributes sec = new Attributes();
        sec.put(Attributes.Name.CONTENT_TYPE, "text/plain");
        mf.getEntries().put("hello.txt", sec);

        try (OutputStream os = Files.newOutputStream(path);
             JarOutputStream jout = new JarOutputStream(os, mf)) {
            jout.putNextEntry(new JarEntry("hello.txt"));
            jout.write("world".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();
            jout.putNextEntry(new JarEntry("nested.txt"));
            jout.write("nested-content".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();
        }
    }

    static void writeMultiReleaseJar(Path path) throws Exception {
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mf.getMainAttributes().put(Attributes.Name.MULTI_RELEASE, "true");
        try (OutputStream os = Files.newOutputStream(path);
             JarOutputStream jout = new JarOutputStream(os, mf)) {
            jout.putNextEntry(new JarEntry("version.txt"));
            jout.write("base".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();
            jout.putNextEntry(new JarEntry("META-INF/versions/9/version.txt"));
            jout.write("v9".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.jar.JarFile");
        check(!c.isInterface() && java.lang.reflect.Modifier.isPublic(c.getModifiers()),
              "JarFile is public concrete class");
        check(c.getSuperclass() == ZipFile.class, "extends java.util.zip.ZipFile");

        java.lang.reflect.Field f = c.getField("MANIFEST_NAME");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers())
              && java.lang.reflect.Modifier.isStatic(f.getModifiers())
              && java.lang.reflect.Modifier.isFinal(f.getModifiers()),
              "MANIFEST_NAME is public static final");
        check("META-INF/MANIFEST.MF".equals(f.get(null)), "MANIFEST_NAME value");

        check(hasMethod(c, "baseVersion"), "baseVersion()");
        check(hasMethod(c, "runtimeVersion"), "runtimeVersion()");
        check(hasConstructor(c, String.class), "JarFile(String)");
        check(hasConstructor(c, String.class, boolean.class), "JarFile(String,boolean)");
        check(hasConstructor(c, java.io.File.class), "JarFile(File)");
        check(hasConstructor(c, java.io.File.class, boolean.class), "JarFile(File,boolean)");
        check(hasConstructor(c, java.io.File.class, boolean.class, int.class),
              "JarFile(File,boolean,int)");
        check(hasConstructor(c, java.io.File.class, boolean.class, int.class,
              Runtime.Version.class), "JarFile(File,boolean,int,Version)");
        check(hasMethod(c, "getVersion"), "getVersion()");
        check(hasMethod(c, "isMultiRelease"), "isMultiRelease()");
        check(hasMethod(c, "getManifest"), "getManifest()");
        check(hasMethod(c, "getJarEntry", String.class), "getJarEntry(String)");
        check(hasMethod(c, "getEntry", String.class), "getEntry(String)");
        check(hasMethod(c, "entries"), "entries()");
        check(hasMethod(c, "stream"), "stream()");
        check(hasMethod(c, "versionedStream"), "versionedStream()");
        check(hasMethod(c, "getInputStream", ZipEntry.class), "getInputStream(ZipEntry)");
    }

    // ---- Part 2: black-box (behavioral) ----

    static void testSimpleJar(Path jar) throws Exception {
        try (JarFile jf = new JarFile(jar.toFile())) {
            check(jf.size() == 3, "simple jar has 3 entries (manifest + 2 files)");

            Manifest manifest = jf.getManifest();
            check(manifest != null, "getManifest present");
            check("1.0".equals(manifest.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
                  "manifest version");
            check("com.example.Main".equals(manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS)),
                  "manifest main class");
            check("text/plain".equals(manifest.getAttributes("hello.txt").getValue(Attributes.Name.CONTENT_TYPE)),
                  "manifest per-entry attribute");

            JarEntry hello = jf.getJarEntry("hello.txt");
            check(hello != null && "hello.txt".equals(hello.getName()), "getJarEntry");
            check(jf.getEntry("hello.txt") != null, "getEntry returns ZipEntry");
            check("text/plain".equals(hello.getAttributes().getValue(Attributes.Name.CONTENT_TYPE)),
                  "JarEntry.getAttributes from manifest section");
            check(jf.getJarEntry("nonexistent") == null, "getJarEntry unknown -> null");

            byte[] content = readAll(jf.getInputStream(hello));
            check(Arrays.equals(content, "world".getBytes(StandardCharsets.UTF_8)),
                  "entry content equality");

            List<String> names = entryNames(jf);
            check(names.equals(Arrays.asList("META-INF/MANIFEST.MF", "hello.txt", "nested.txt")),
                  "entries() enumeration sorted: " + names);

            List<String> streamNames = new ArrayList<String>();
            jf.stream().map(e -> e.getName()).forEach(streamNames::add);
            Collections.sort(streamNames);
            check(streamNames.equals(names), "stream() names match entries()");

            List<String> versionedNames = new ArrayList<String>();
            jf.versionedStream().map(e -> e.getName()).forEach(versionedNames::add);
            Collections.sort(versionedNames);
            check(versionedNames.equals(names), "versionedStream() equals stream() when not multi-release");

            check(!jf.isMultiRelease(), "simple jar is not multi-release");
            check(jf.getVersion().equals(JarFile.baseVersion()),
                  "getVersion returns baseVersion when not multi-release");
            check(JarFile.baseVersion().feature() == 8, "baseVersion feature is 8");
            check(JarFile.runtimeVersion().feature() == Runtime.version().feature(),
                  "runtimeVersion feature equals Runtime.version().feature()");

            expectThrows(NullPointerException.class, () -> jf.getJarEntry(null));
            check(jf.getInputStream(new JarEntry("absent.txt")) == null,
                  "getInputStream for absent entry returns null");
        }

        // closed-file behavior
        JarFile closed = new JarFile(jar.toFile());
        JarEntry hello = closed.getJarEntry("hello.txt");
        check(hello != null, "obtained entry before close");
        closed.close();
        expectThrows(IllegalStateException.class, () -> closed.size());
        expectThrows(IllegalStateException.class, () -> closed.getEntry("hello.txt"));
        expectThrows(IllegalStateException.class, new Runnable() {
            public void run() {
                try { closed.getInputStream(hello); }
                catch (java.io.IOException e) { throw new RuntimeException(e); }
            }
        });
    }

    static void testMultiReleaseJar(Path jar) throws Exception {
        // default constructor processes at the base version (feature 8)
        try (JarFile jf = new JarFile(jar.toFile())) {
            check(jf.isMultiRelease(), "multi-release jar detected");
            check(jf.getVersion().feature() == JarFile.baseVersion().feature(),
                  "default constructor uses base version");
            JarEntry base = jf.getJarEntry("version.txt");
            check(base != null && "version.txt".equals(base.getName()), "base entry via default");
            check("version.txt".equals(base.getRealName()), "base entry real name equals name");
            check(Arrays.equals(readAll(jf.getInputStream(base)), "base".getBytes(StandardCharsets.UTF_8)),
                  "default constructor reads base content");
        }

        // runtime-version constructor resolves versioned entries
        try (JarFile jf = new JarFile(jar.toFile(), false, ZipFile.OPEN_READ, JarFile.runtimeVersion())) {
            check(jf.isMultiRelease(), "runtime-version jar is multi-release");
            check(jf.getVersion().feature() == Runtime.version().feature(),
                  "getVersion feature equals runtime feature");

            JarEntry ve = jf.getJarEntry("version.txt");
            check(ve != null, "versioned entry resolvable via base name");
            check("version.txt".equals(ve.getName()), "versioned entry getName is base name");
            check("META-INF/versions/9/version.txt".equals(ve.getRealName()),
                  "versioned entry getRealName is raw path");
            check(Arrays.equals(readAll(jf.getInputStream(ve)), "v9".getBytes(StandardCharsets.UTF_8)),
                  "versioned entry content is v9");

            List<String> versioned = new ArrayList<String>();
            jf.versionedStream().map(e -> e.getName()).forEach(versioned::add);
            Collections.sort(versioned);
            check(versioned.equals(Arrays.asList("META-INF/MANIFEST.MF", "version.txt")),
                  "versionedStream hides raw versioned path: " + versioned);
        }

        // explicitly configured for the base version -> base entry wins
        try (JarFile jf = new JarFile(jar.toFile(), false, ZipFile.OPEN_READ, JarFile.baseVersion())) {
            JarEntry base = jf.getJarEntry("version.txt");
            check(base != null, "base entry resolvable via baseVersion()");
            check("version.txt".equals(base.getRealName()), "base entry getRealName equals name");
            check(Arrays.equals(readAll(jf.getInputStream(base)), "base".getBytes(StandardCharsets.UTF_8)),
                  "base entry content");
        }
    }

    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path simple = dir.resolve("simple.jar");
            writeSimpleJar(simple);
            testSimpleJar(simple);

            Path mr = dir.resolve("mr.jar");
            writeMultiReleaseJar(mr);
            testMultiReleaseJar(mr);
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
