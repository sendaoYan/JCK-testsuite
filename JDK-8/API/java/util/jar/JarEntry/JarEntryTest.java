/*
 * @test
 * @summary java.util.jar.JarEntry conformance: white-box structure + black-box behavior.
 * @run main JarEntryTest
 */

import java.util.jar.JarEntry;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
