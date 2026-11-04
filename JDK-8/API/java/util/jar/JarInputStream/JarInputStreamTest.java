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


    // ===================== 契约深化：manifest 消费语义/非首位 manifest 忽略/无 manifest/内容 round-trip =====================
    static void testContracts() throws Exception {
        // 带 manifest 的 jar：getManifest 在首个 getNext 前可用；META-INF 不作为 entry 返回
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue("Manifest-Version", "1.0");
        mf.getMainAttributes().putValue("Main-Class", "Main");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        JarOutputStream jo = new JarOutputStream(bos, mf);
        jo.putNextEntry(new JarEntry("a.txt"));
        jo.write("alpha".getBytes("UTF-8"));
        jo.closeEntry();
        jo.putNextEntry(new JarEntry("b.txt"));
        jo.write("beta".getBytes("UTF-8"));
        jo.closeEntry();
        jo.close();
        JarInputStream jin = new JarInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(jin.getManifest() != null
              && "Main".equals(jin.getManifest().getMainAttributes().getValue("Main-Class")),
              "manifest parsed from stream head");
        JarEntry e1 = jin.getNextJarEntry();
        check("a.txt".equals(e1.getName()), "first entry skips META-INF");
        java.io.ByteArrayOutputStream got = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8];
        int n;
        while ((n = jin.read(buf)) > 0) got.write(buf, 0, n);
        check("alpha".equals(new String(got.toByteArray(), "UTF-8")), "entry body round-trip");
        check("b.txt".equals(jin.getNextJarEntry().getName()), "second entry");
        check(jin.getNextJarEntry() == null, "exhausted null");
        jin.close();
        jin.close(); // close 幂等
        // manifest 不在首位 -> 被忽略
        Manifest late = new Manifest();
        late.getMainAttributes().putValue("Manifest-Version", "1.0");
        java.io.ByteArrayOutputStream mb = new java.io.ByteArrayOutputStream();
        late.write(mb);
        java.io.ByteArrayOutputStream b4 = new java.io.ByteArrayOutputStream();
        java.util.zip.ZipOutputStream z4 = new java.util.zip.ZipOutputStream(b4);
        z4.putNextEntry(new java.util.zip.ZipEntry("z.txt"));
        z4.write("z".getBytes("UTF-8"));
        z4.closeEntry();
        z4.putNextEntry(new java.util.zip.ZipEntry("META-INF/MANIFEST.MF"));
        z4.write(mb.toByteArray());
        z4.closeEntry();
        z4.close();
        JarInputStream lateIn = new JarInputStream(new java.io.ByteArrayInputStream(b4.toByteArray()));
        check(lateIn.getManifest() == null, "non-leading manifest ignored");
        check("z.txt".equals(lateIn.getNextJarEntry().getName()), "entries still readable");
        lateIn.close();
        // 无 manifest
        java.io.ByteArrayOutputStream b5 = new java.io.ByteArrayOutputStream();
        java.util.zip.ZipOutputStream z5 = new java.util.zip.ZipOutputStream(b5);
        z5.putNextEntry(new java.util.zip.ZipEntry("n.txt"));
        z5.write("n".getBytes("UTF-8"));
        z5.closeEntry();
        z5.close();
        JarInputStream none = new JarInputStream(new java.io.ByteArrayInputStream(b5.toByteArray()));
        check(none.getManifest() == null, "no manifest null");
        check("n.txt".equals(none.getNextJarEntry().getName()), "plain zip entries flow");
        none.close();
        // null 构造
        try {
            new JarInputStream(null);
            check(false, "JarInputStream(null) NPE");
        } catch (NullPointerException e) { check(true, "JarInputStream(null) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
