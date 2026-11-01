/*
 * @test
 * @summary java.util.zip.ZipOutputStream conformance: write entry and stream options.
 * @run main ZipOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZipOutputStream() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(bout);
        zout.setMethod(ZipEntry.DEFLATED);
        zout.setLevel(Deflater.DEFAULT_COMPRESSION);
        zout.setComment("zip comment");
        zout.putNextEntry(new ZipEntry("e.txt"));
        zout.write("data".getBytes("UTF-8"));
        zout.closeEntry();
        zout.finish();
        zout.close();
        check(bout.size() > 0, "zip output produced");
    }

    public static void main(String[] args) throws Exception {
        testZipOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
