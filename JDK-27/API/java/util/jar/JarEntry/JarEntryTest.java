/*
 * @test
 * @summary java.util.jar.JarEntry conformance: white-box structure + black-box behavior.
 * @run main JarEntryTest
 */

import java.util.jar.JarEntry;
import java.util.jar.*;
import java.util.zip.ZipEntry;

public class JarEntryTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.jar.JarEntry");
        check(!c.isInterface() && java.lang.reflect.Modifier.isPublic(c.getModifiers()),
              "JarEntry is public concrete class");
        check(ZipEntry.class.isAssignableFrom(c) && c.getSuperclass() == ZipEntry.class,
              "extends java.util.zip.ZipEntry");
        check(hasConstructor(c, String.class), "JarEntry(String)");
        check(hasConstructor(c, ZipEntry.class), "JarEntry(ZipEntry)");
        check(hasConstructor(c, JarEntry.class), "JarEntry(JarEntry)");
        check(hasMethod(c, "getAttributes"), "getAttributes()");
        check(hasMethod(c, "getCertificates"), "getCertificates()");
        check(hasMethod(c, "getCodeSigners"), "getCodeSigners()");
        check(hasMethod(c, "getName"), "getName() inherited");
    }

    // ---- Part 2: black-box (behavioral) ----

    static void testBasics() throws Exception {
        JarEntry e = new JarEntry("file.txt");
        check("file.txt".equals(e.getName()), "getName");
        check(!e.isDirectory(), "not a directory");
        check(e.getCertificates() == null, "getCertificates null");
        check(e.getCodeSigners() == null, "getCodeSigners null");
        check(e.getAttributes() == null, "getAttributes null initially");

        JarEntry dir = new JarEntry("dir/");
        check(dir.isDirectory(), "directory entry detected");
    }

    static void testAttributes() throws Exception {
        JarEntry e = new JarEntry("file.txt");
        check(e.getAttributes() == null, "standalone entry has no manifest attributes");
    }

    static void testZipEntryCopy() {
        ZipEntry ze = new ZipEntry("base.txt");
        ze.setComment("a comment");
        ze.setTime(1234567890000L); // even-second epoch, DOS-time stable
        ze.setSize(5);
        JarEntry je = new JarEntry(ze);
        check("base.txt".equals(je.getName()), "name inherited from ZipEntry");
        check("a comment".equals(je.getComment()), "comment inherited");
        check(je.getTime() == 1234567890000L, "time inherited (fixed epoch)");
        check(je.getSize() == 5, "size inherited");
    }

    static void testJarEntryCopy() throws Exception {
        JarEntry src = new JarEntry("orig.txt");
        src.setSize(7);
        src.setComment("c");

        JarEntry copy = new JarEntry(src);
        check("orig.txt".equals(copy.getName()), "copy name");
        check(copy.getSize() == 7, "copy size");
        check("c".equals(copy.getComment()), "copy comment");
        check(copy.getAttributes() == null, "copy attributes null (no manifest)");
        check(copy.getCertificates() == null && copy.getCodeSigners() == null,
              "copy has no signers/certs");
    }

    static void testInheritedSetters() {
        JarEntry e = new JarEntry("x.txt");
        e.setSize(100);
        check(e.getSize() == 100, "setSize/getSize");
        e.setCompressedSize(50);
        check(e.getCompressedSize() == 50, "setCompressedSize/getCompressedSize");
        e.setMethod(ZipEntry.STORED);
        check(e.getMethod() == ZipEntry.STORED, "setMethod/getMethod");
        e.setComment("hello");
        check("hello".equals(e.getComment()), "setComment/getComment");
        check(e.getTime() == -1 || e.getTime() > 0, "default time valid");
    }

    static void testNullName() {
        expectThrows(NullPointerException.class, new Runnable() {
            public void run() { new JarEntry((String) null); }
        });
    }

    static void testBlackBox() throws Exception {
        testBasics();
        testAttributes();
        testZipEntryCopy();
        testJarEntryCopy();
        testInheritedSetters();
        testNullName();
    }


    // ===================== 契约深化：构造器默认值/ZipEntry 拷贝/来自 JarFile 的属性/签名面 null =====================
    static void testContracts() throws Exception {
        JarEntry plain = new JarEntry("e.txt");
        check(plain.getSize() == -1 && plain.getCompressedSize() == -1 && plain.getCrc() == -1,
              "fresh entry sizes -1");
        check(plain.getTime() == -1, "fresh time -1");
        check(plain.getMethod() == -1, "fresh method -1");
        check(plain.getAttributes() == null, "standalone entry attrs null");
        check(plain.getCodeSigners() == null && plain.getCertificates() == null, "unsigned entry null certs");
        check(plain.getName().equals("e.txt"), "name kept");
        check(plain.toString().contains("e.txt"), "toString contains name");
        check(!plain.isDirectory(), "plain not directory");
        check(new JarEntry("dir/").isDirectory(), "trailing slash is directory");
        // JarEntry(ZipEntry) 拷贝时间/注释
        ZipEntry ze = new ZipEntry("z.txt");
        ze.setTime(1234567890000L);
        ze.setComment("zc");
        JarEntry from = new JarEntry(ze);
        check(from.getTime() == 1234567890000L, "copied time");
        check("zc".equals(from.getComment()), "copied comment");
        // null 构造
        try {
            new JarEntry((String) null);
            check(false, "JarEntry(null String) NPE");
        } catch (NullPointerException e) { check(true, "JarEntry(null String) NPE"); }
        try {
            new JarEntry((ZipEntry) null);
            check(false, "JarEntry(null ZipEntry) NPE");
        } catch (NullPointerException e) { check(true, "JarEntry(null ZipEntry) NPE"); }
        // 造 jar：分节属性可读回
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue("Manifest-Version", "1.0");
        Attributes sec = new Attributes();
        sec.putValue("Content-Type", "text/plain");
        mf.getEntries().put("sec.txt", sec);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        JarOutputStream jo = new JarOutputStream(bos, mf);
        jo.putNextEntry(new JarEntry("sec.txt"));
        jo.write("s".getBytes("UTF-8"));
        jo.closeEntry();
        jo.close();
        java.nio.file.Path jarPath = java.nio.file.Files.createTempFile("jck-jarentry", ".jar");
        java.nio.file.Files.write(jarPath, bos.toByteArray());
        try (JarFile jf = new JarFile(jarPath.toFile())) {
            JarEntry e = jf.getJarEntry("sec.txt");
            check(e != null && e.getAttributes() != null
                  && "text/plain".equals(e.getAttributes().getValue("Content-Type")),
                  "section attrs readable via JarFile");
            check(e.getCertificates() == null && e.getCodeSigners() == null, "unsigned null certs");
            check(e.getSize() == 1, "size from central directory");
            check(e.getMethod() == java.util.zip.ZipEntry.DEFLATED, "deflated by default");
        } finally {
            java.nio.file.Files.deleteIfExists(jarPath);
        }
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
