/*
 * @test
 * @summary java.util.jar.JarFile contract: white-box structure (extends ZipFile,
 *          MANIFEST_NAME, JDK-8 constructors) + black-box behavior (getManifest,
 *          getJarEntry attributes, getInputStream content, entries/stream,
 *          null/missing/closed handling).
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

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable action, String msg) {
        try {
            action.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but none thrown");
    }

    interface ThrowingRunnable { void run() throws Throwable; }

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

    static Path writeSimpleJar(Path dir) throws Exception {
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "com.example.Main");
        Attributes sec = new Attributes();
        sec.put(Attributes.Name.CONTENT_TYPE, "text/plain");
        mf.getEntries().put("hello.txt", sec);

        Path jar = dir.resolve("simple.jar");
        try (OutputStream os = Files.newOutputStream(jar);
             JarOutputStream jout = new JarOutputStream(os, mf)) {
            jout.putNextEntry(new JarEntry("hello.txt"));
            jout.write("world".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();
            jout.putNextEntry(new JarEntry("nested.txt"));
            jout.write("nested-content".getBytes(StandardCharsets.UTF_8));
            jout.closeEntry();
        }
        return jar;
    }

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

        check(hasConstructor(c, String.class), "JarFile(String)");
        check(hasConstructor(c, String.class, boolean.class), "JarFile(String,boolean)");
        check(hasConstructor(c, java.io.File.class), "JarFile(File)");
        check(hasConstructor(c, java.io.File.class, boolean.class), "JarFile(File,boolean)");
        check(hasConstructor(c, java.io.File.class, boolean.class, int.class),
                "JarFile(File,boolean,int)");
        check(hasMethod(c, "getManifest"), "getManifest()");
        check(hasMethod(c, "getJarEntry", String.class), "getJarEntry(String)");
        check(hasMethod(c, "getEntry", String.class), "getEntry(String)");
        check(hasMethod(c, "entries"), "entries()");
        check(hasMethod(c, "stream"), "stream()");
        check(hasMethod(c, "getInputStream", ZipEntry.class), "getInputStream(ZipEntry)");
    }

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

            expectThrows(NullPointerException.class, () -> jf.getJarEntry(null), "getJarEntry(null) NPE");
            check(jf.getInputStream(new JarEntry("absent.txt")) == null,
                    "getInputStream for absent entry returns null");
        }

        // closed-file behavior
        JarFile closed = new JarFile(jar.toFile());
        JarEntry hello = closed.getJarEntry("hello.txt");
        check(hello != null, "obtained entry before close");
        closed.close();
        expectThrows(IllegalStateException.class, () -> closed.size(), "size after close ISE");
        expectThrows(IllegalStateException.class, () -> closed.getEntry("hello.txt"), "getEntry after close ISE");
        expectThrows(IllegalStateException.class, () -> closed.getInputStream(hello), "getInputStream after close ISE");
    }

    static void testCtor() throws Exception {
        expectThrows(NullPointerException.class, () -> new JarFile((String) null), "null String ctor NPE");
        expectThrows(NullPointerException.class, () -> new JarFile((java.io.File) null), "null File ctor NPE");
        expectThrows(java.io.FileNotFoundException.class,
                () -> new JarFile(new java.io.File("/nonexistent/nope.jar")),
                "missing file FileNotFoundException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        Path dir = Files.createTempDirectory("jck");
        try {
            testSimpleJar(writeSimpleJar(dir));
        } finally {
            deleteRecursively(dir);
        }
        testCtor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
