/*
 * @test
 * @summary java.util.zip.ZipFile contract: ctor null/NPE + missing-file ZipException,
 *          size/entries/getEntry/getComment/getName/getInputStream, getEntry null
 *          name NPE, getInputStream null entry NPE, close idempotence and
 *          IllegalStateException after close (getName still works), OPEN_DELETE
 *          removes file, stream() matches entries.
 * @run main ZipFileTest
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipFileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static File makeZip() throws Exception {
        File tmp = File.createTempFile("jcapi-zf", ".zip");
        tmp.deleteOnExit();
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tmp));
        zout.setComment("zf comment");
        zout.putNextEntry(new ZipEntry("a.txt"));
        zout.write("alpha".getBytes(StandardCharsets.UTF_8));
        zout.closeEntry();
        zout.putNextEntry(new ZipEntry("b.txt"));
        zout.write("beta".getBytes(StandardCharsets.UTF_8));
        zout.closeEntry();
        zout.close();
        return tmp;
    }

    static void testCtor() throws Exception {
        checkThrows(NullPointerException.class, () -> new ZipFile((String) null), "null String ctor NPE");
        checkThrows(NullPointerException.class, () -> new ZipFile((File) null), "null File ctor NPE");
        File missing = new File("/nonexistent/nope.zip");
        checkThrows(java.nio.file.NoSuchFileException.class, () -> new ZipFile(missing),
                "missing file NoSuchFileException (JDK-7u40+/NIO 路径与 JDK-8 FileNotFoundException 均为 IOException，版本差异锁定)");
    }

    static void testRead() throws Exception {
        File tmp = makeZip();
        ZipFile zf = new ZipFile(tmp);
        check(zf.size() == 2, "size");
        check("zf comment".equals(zf.getComment()), "comment");
        check(tmp.getPath().equals(zf.getName()), "getName returns path");

        ZipEntry a = zf.getEntry("a.txt");
        check(a != null && "a.txt".equals(a.getName()), "getEntry found");
        check(zf.getEntry("missing.txt") == null, "getEntry missing null");

        InputStream in = zf.getInputStream(a);
        byte[] buf = new byte[16];
        int n = in.read(buf);
        check("alpha".equals(new String(buf, 0, n, StandardCharsets.UTF_8)), "entry content");
        in.close();

        int count = 0;
        for (Enumeration<? extends ZipEntry> en = zf.entries(); en.hasMoreElements(); ) {
            en.nextElement();
            count++;
        }
        check(count == 2, "entries enumeration count");
        check(zf.stream().count() == 2, "stream count");

        checkThrows(NullPointerException.class, () -> zf.getEntry(null), "getEntry null NPE");
        checkThrows(NullPointerException.class, () -> zf.getInputStream(null), "getInputStream null NPE");
        zf.close();
    }

    static void testClose() throws Exception {
        ZipFile zf = new ZipFile(makeZip());
        zf.close();
        zf.close(); // idempotent
        checkThrows(IllegalStateException.class, () -> zf.size(), "size after close ISE");
        checkThrows(IllegalStateException.class, () -> zf.getComment(), "getComment after close ISE");
        checkThrows(IllegalStateException.class, () -> zf.getEntry("a.txt"), "getEntry after close ISE");
        checkThrows(IllegalStateException.class, () -> zf.getInputStream(new ZipEntry("a.txt")), "getInputStream after close ISE");
        checkThrows(IllegalStateException.class, () -> zf.entries().hasMoreElements(), "entries after close ISE");
        check(zf.getName() != null, "getName still works after close");
    }

    static void testOpenDelete() throws Exception {
        File tmp = makeZip();
        check(tmp.exists(), "file exists before OPEN_DELETE");
        ZipFile zf = new ZipFile(tmp, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE);
        check(zf.size() == 2, "OPEN_DELETE readable");
        zf.close();
        check(!tmp.exists(), "OPEN_DELETE removes file on close");
    }


    // ===================== JDK-9+ 增量：toString()（JDK-27 侧锁定） =====================
    static void testJdk27Additions() throws Exception {
        File tmp = File.createTempFile("jck-zf-tostring", ".zip");
        tmp.deleteOnExit();
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tmp));
        zout.putNextEntry(new ZipEntry("inner.txt"));
        zout.write("x".getBytes("UTF-8"));
        zout.closeEntry();
        zout.close();
        try (ZipFile zf = new ZipFile(tmp)) {
            String s = zf.toString();
            check(s != null && s.contains(tmp.getName()), "toString contains file name: " + s);
        }
    }

    public static void main(String[] args) throws Exception {
        testCtor();
        testRead();
        testClose();
        testOpenDelete();        testJdk27Additions();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
