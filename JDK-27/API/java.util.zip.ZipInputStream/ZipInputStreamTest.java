/*
 * @test
 * @summary java.util.zip.ZipInputStream conformance: white-box structure + black-box read entries.
 * @run main ZipInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> clazz(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = clazz("java.util.zip.ZipInputStream");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == InflaterInputStream.class, "extends InflaterInputStream");
        c.getConstructor(Class.forName("java.io.InputStream"));
        c.getConstructor(Class.forName("java.io.InputStream"), java.nio.charset.Charset.class);
        c.getMethod("getNextEntry");
        c.getMethod("closeEntry");
        c.getMethod("available");
        c.getMethod("read");
        c.getMethod("readAllBytes");
        c.getMethod("readNBytes", int.class);
        c.getMethod("readNBytes", byte[].class, int.class, int.class);
        c.getMethod("skipNBytes", long.class);
        c.getMethod("transferTo", Class.forName("java.io.OutputStream"));
        c.getMethod("read", byte[].class, int.class, int.class);
        c.getMethod("close");
        check(true, "constructors and entry methods present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(bout);
        zout.putNextEntry(new ZipEntry("a.txt"));
        zout.write("aaa".getBytes("UTF-8"));
        zout.closeEntry();
        zout.putNextEntry(new ZipEntry("b.txt"));
        zout.write("bb".getBytes("UTF-8"));
        zout.closeEntry();
        zout.close();

        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(bout.toByteArray()));
        ZipEntry e1 = zin.getNextEntry();
        check(e1 != null && "a.txt".equals(e1.getName()), "first entry name");
        byte[] c1 = zin.readAllBytes();
        check("aaa".equals(new String(c1, "UTF-8")), "first entry content via readAllBytes");
        zin.closeEntry();

        ZipEntry e2 = zin.getNextEntry();
        check(e2 != null && "b.txt".equals(e2.getName()), "second entry name");
        byte[] c2 = new byte[8];
        int n2 = zin.read(c2);
        check(n2 == 2 && "bb".equals(new String(c2, 0, n2, "UTF-8")), "second entry content");
        zin.closeEntry();
        check(zin.getNextEntry() == null, "no more entries");
        zin.close();

        // readNBytes(int) path on a fresh stream
        ZipInputStream zin2 = new ZipInputStream(new ByteArrayInputStream(bout.toByteArray()));
        check(zin2.getNextEntry() != null, "getNextEntry for readNBytes");
        byte[] nb = zin2.readNBytes(3);
        check(nb.length == 3 && "aaa".equals(new String(nb, "UTF-8")), "readNBytes(int) reads entry bytes");
        zin2.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
