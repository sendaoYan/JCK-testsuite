/*
 * @test
 * @summary java.util.zip.ZipInputStream conformance: read multiple entries.
 * @run main ZipInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZipInputStream() throws Exception {
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
        check(e1 != null && "a.txt".equals(e1.getName()), "first entry");
        byte[] buf = new byte[10];
        int n1 = zin.read(buf);
        check(n1 == 3, "read first entry length");
        zin.closeEntry();
        ZipEntry e2 = zin.getNextEntry();
        check(e2 != null && "b.txt".equals(e2.getName()), "second entry");
        check(zin.getNextEntry() == null, "no more entries");
        zin.close();
    }

    public static void main(String[] args) throws Exception {
        testZipInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
