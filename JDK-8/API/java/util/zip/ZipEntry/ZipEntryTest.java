/*
 * @test
 * @summary java.util.zip.ZipEntry conformance: name/size/crc/time/method/extra/comment.
 * @run main ZipEntryTest
 */

import java.util.Arrays;
import java.util.zip.ZipEntry;

public class ZipEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZipEntry() {
        check(ZipEntry.STORED == 0 && ZipEntry.DEFLATED == 8, "method constants");
        ZipEntry e = new ZipEntry("dir/file.txt");
        check("dir/file.txt".equals(e.getName()), "getName");
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
        byte[] extra = { 1, 2, 3 };
        e.setExtra(extra);
        check(Arrays.equals(extra, e.getExtra()), "setExtra/getExtra");
        e.setComment("a comment");
        check("a comment".equals(e.getComment()), "setComment/getComment");
        check(e.toString() != null, "toString");
        check(e.hashCode() == e.hashCode(), "hashCode stable");
        check(e.clone() instanceof ZipEntry, "clone");

        ZipEntry dir = new ZipEntry("dir/");
        check(dir.isDirectory(), "isDirectory for trailing slash");
    }

    public static void main(String[] args) {
        testZipEntry();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
