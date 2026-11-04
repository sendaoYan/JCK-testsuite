/*
 * @test
 * @summary java.util.zip.ZipEntry conformance: white-box structure + black-box metadata behavior.
 * @run main ZipEntryTest
 */

import java.lang.reflect.Modifier;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.zip.ZipEntry;

public class ZipEntryTest {
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
        Class<?> c = clazz("java.util.zip.ZipEntry");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(ZipEntry.STORED == 0, "STORED == 0");
        check(ZipEntry.DEFLATED == 8, "DEFLATED == 8");

        c.getConstructor(String.class);
        c.getConstructor(ZipEntry.class);
        c.getMethod("getName");
        c.getMethod("setTime", long.class);
        c.getMethod("getTime");
        c.getMethod("setTimeLocal", LocalDateTime.class);
        c.getMethod("getTimeLocal");
        c.getMethod("setLastModifiedTime", FileTime.class);
        c.getMethod("getLastModifiedTime");
        c.getMethod("setSize", long.class);
        c.getMethod("getSize");
        c.getMethod("setCrc", long.class);
        c.getMethod("getCrc");
        c.getMethod("setMethod", int.class);
        c.getMethod("getMethod");
        c.getMethod("setExtra", byte[].class);
        c.getMethod("getExtra");
        c.getMethod("setComment", String.class);
        c.getMethod("getComment");
        c.getMethod("isDirectory");
        c.getMethod("clone");
        check(true, "constructors and metadata methods present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ZipEntry e = new ZipEntry("dir/file.txt");
        check("dir/file.txt".equals(e.getName()), "getName");
        check(e.getTime() == -1, "initial getTime -1");
        check(e.getLastModifiedTime() == null, "initial getLastModifiedTime null");
        check(!e.isDirectory(), "not directory initially");

        e.setSize(100);
        check(e.getSize() == 100, "setSize/getSize");
        e.setCompressedSize(50);
        check(e.getCompressedSize() == 50, "setCompressedSize/getCompressedSize");
        e.setCrc(12345L);
        check(e.getCrc() == 12345L, "setCrc/getCrc");

        e.setTime(1000L);
        check(e.getTime() == 1000L, "setTime/getTime");
        e.setMethod(ZipEntry.DEFLATED);
        check(e.getMethod() == ZipEntry.DEFLATED, "setMethod/getMethod");

        // fixed LocalDateTime round-trips through the extended timestamp path
        LocalDateTime ldt = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
        e.setTimeLocal(ldt);
        check(ldt.equals(e.getTimeLocal()), "setTimeLocal/getTimeLocal round-trip");

        // exact epoch millis via FileTime accessors
        long fixedMillis = 1700000000000L;
        ZipEntry ft = new ZipEntry("ft");
        check(ft.setLastModifiedTime(FileTime.fromMillis(fixedMillis)) == ft, "setLastModifiedTime returns this");
        check(ft.getLastModifiedTime().toMillis() == fixedMillis, "getLastModifiedTime exact millis");
        ft.setLastAccessTime(FileTime.fromMillis(fixedMillis - 1000));
        check(ft.getLastAccessTime().toMillis() == fixedMillis - 1000, "set/getLastAccessTime");
        ft.setCreationTime(FileTime.fromMillis(fixedMillis - 2000));
        check(ft.getCreationTime().toMillis() == fixedMillis - 2000, "set/getCreationTime");

        byte[] extra = { 1, 2, 3 };
        e.setExtra(extra);
        check(Arrays.equals(extra, e.getExtra()), "setExtra/getExtra");
        e.setComment("a comment");
        check("a comment".equals(e.getComment()), "setComment/getComment");

        check(e.toString() != null, "toString");
        check(new ZipEntry("a").hashCode() == new ZipEntry("a").hashCode(), "hashCode name-based");

        Object clone = e.clone();
        check(clone instanceof ZipEntry && clone != e, "clone returns distinct ZipEntry");
        ZipEntry ec = (ZipEntry) clone;
        check(e.getName().equals(ec.getName()) && e.getSize() == ec.getSize() && e.getCrc() == ec.getCrc(),
                "clone copies name/size/crc");

        // copy constructor
        ZipEntry copy = new ZipEntry(e);
        check("dir/file.txt".equals(copy.getName()), "copy ctor name");
        check(copy.getSize() == 100 && copy.getCrc() == 12345L && copy.getMethod() == ZipEntry.DEFLATED,
                "copy ctor copies size/crc/method");

        ZipEntry dir = new ZipEntry("dir/");
        check(dir.isDirectory(), "isDirectory for trailing slash");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
