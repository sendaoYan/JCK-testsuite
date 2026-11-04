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


    // ===================== 契约深化：中文 entry/manifest 分节/entries 序/关闭后 ISE/流视图 =====================
    static void testContracts() throws Exception {
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue("Manifest-Version", "1.0");
        mf.getMainAttributes().putValue("Main-Class", "Main");
        Attributes sec = new Attributes();
        sec.putValue("X-Section", "yes");
        mf.getEntries().put("中 文.txt", sec);
        mf.getEntries().put("plain.txt", new Attributes());
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        JarOutputStream jo = new JarOutputStream(bos, mf);
        for (String name : new String[] {"中 文.txt", "plain.txt", "dir/"}) {
            jo.putNextEntry(new JarEntry(name));
            if (!name.endsWith("/")) jo.write(("body-of-" + name).getBytes("UTF-8"));
            jo.closeEntry();
        }
        jo.close();
        java.nio.file.Path p = java.nio.file.Files.createTempFile("jck-jarfile", ".jar");
        java.nio.file.Files.write(p, bos.toByteArray());
        try (JarFile jf = new JarFile(p.toFile())) {
            check("Main".equals(jf.getManifest().getMainAttributes().getValue("Main-Class")), "manifest main attrs");
            check(jf.size() == 4, "size includes META-INF/MANIFEST.MF: " + jf.size());
            int manifestSeen = 0;
            java.util.List<String> names = new java.util.ArrayList<String>();
            java.util.Enumeration<JarEntry> en = jf.entries();
            while (en.hasMoreElements()) {
                String n = en.nextElement().getName();
                names.add(n);
                if (n.equals("META-INF/MANIFEST.MF")) manifestSeen++;
            }
            check(manifestSeen == 1, "exactly one manifest entry in entries()");
            check(names.contains("中 文.txt") && names.contains("dir/"), "chinese + dir entries listed");
            check(jf.stream().count() == jf.size(), "stream() count == size()");
            JarEntry cn = jf.getJarEntry("中 文.txt");
            check(cn != null && "yes".equals(cn.getAttributes().getValue("X-Section")), "chinese entry section attrs");
            java.io.InputStream is = jf.getInputStream(cn);
            byte[] body = new byte[64];
            int n = is.read(body);
            check(new String(body, 0, n, "UTF-8").equals("body-of-中 文.txt"), "chinese body round-trip");
            is.close();
            check(jf.getEntry("META-INF/MANIFEST.MF") != null, "manifest reachable via getEntry");
            check(jf.getJarEntry("missing") == null, "missing entry null");
        } finally {
            java.nio.file.Files.deleteIfExists(p);
        }
        // 关闭后操作 IllegalStateException（8 与 27 一致）
        java.nio.file.Path p2 = java.nio.file.Files.createTempFile("jck-jarfile2", ".jar");
        java.nio.file.Files.write(p2, bos.toByteArray());
        JarFile closed = new JarFile(p2.toFile());
        closed.close();
        try { closed.getManifest(); check(false, "closed getManifest ISE"); }
        catch (IllegalStateException e) { check(true, "closed getManifest ISE"); }
        try { closed.getJarEntry("plain.txt"); check(false, "closed getJarEntry ISE"); }
        catch (IllegalStateException e) { check(true, "closed getJarEntry ISE"); }
        try { closed.stream().count(); check(false, "closed stream ISE"); }
        catch (IllegalStateException e) { check(true, "closed stream ISE"); }
        check(closed.getName() != null, "getName still works after close");
        java.nio.file.Files.deleteIfExists(p2);
        // 不存在的 jar 文件
        try {
            new JarFile(new java.io.File("/nonexistent/jck-nope.jar"));
            check(false, "missing jar file throws");
        } catch (java.io.IOException e) {
            check(e instanceof java.util.zip.ZipException || e instanceof java.io.FileNotFoundException,
                  "missing jar IOException family: " + e.getClass().getSimpleName());
        }
    }

    public static void main(String[] args) throws Exception {
        testContracts();
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
