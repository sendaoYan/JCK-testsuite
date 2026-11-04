/*
 * @test
 * @summary java.util.zip.ZipFile conformance: open/entries/getEntry/getInputStream.
 * @run main ZipFileTest
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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

    static void testZipFile() throws Exception {
        File tmp = File.createTempFile("jcapi", ".zip");
        tmp.deleteOnExit();
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tmp));
        zout.putNextEntry(new ZipEntry("hello.txt"));
        zout.write("world".getBytes("UTF-8"));
        zout.closeEntry();
        zout.close();

        ZipFile zf = new ZipFile(tmp);
        check(zf.size() == 1, "size");
        ZipEntry entry = zf.getEntry("hello.txt");
        check(entry != null, "getEntry returns entry");
        check("hello.txt".equals(entry.getName()), "entry name");
        InputStream in = zf.getInputStream(entry);
        byte[] buf = new byte[100];
        int n = in.read(buf);
        check(n == 5 && "world".equals(new String(buf, 0, n, "UTF-8")), "entry content");
        in.close();
        int count = 0;
        for (Enumeration<? extends ZipEntry> en = zf.entries(); en.hasMoreElements(); en.nextElement()) {
            count++;
        }
        check(count == 1, "entries enumeration");
        zf.close();
    }

    public static void main(String[] args) throws Exception {
        testZipFile();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
