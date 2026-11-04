/*
 * @test
 * @summary java.io.FileInputStream conformance: full contract coverage.
 *          File/String constructors, single/offset reads, skip, available,
 *          EOF, getFD/getChannel, FileNotFoundException for a missing file.
 * @run main FileInputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;

public class FileInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static File newTempDir() throws Exception {
        File d = File.createTempFile("jck-fis", ".tmp");
        if (!d.delete() || !d.mkdir()) throw new AssertionError("temp dir");
        return d;
    }

    static void deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) for (File k : kids) deleteRecursively(k);
        }
        f.delete();
    }

    // read all remaining bytes without JDK9+ readAllBytes
    static byte[] drain(FileInputStream fis) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[16];
        int n;
        while ((n = fis.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    static void testBehavior() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "data.bin");
            byte[] data = {10, 20, 30, 40, 50, 60, 70};
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(data);
            fos.close();

            FileInputStream fis = new FileInputStream(f);
            check(fis.getFD() != null, "getFD non-null");
            check(fis.getChannel() != null, "getChannel non-null");
            check(fis.available() == data.length, "available == remaining length");
            check(fis.read() == 10, "read() == 10");
            byte[] buf = new byte[3];
            check(fis.read(buf, 1, 2) == 2 && buf[1] == 20 && buf[2] == 30,
                  "read(byte[],off,len)");
            check(fis.skip(1) == 1, "skip returns count");
            check(fis.read() == 50, "read after skip");
            check(fis.read() == 60 && fis.read() == 70, "remaining bytes consumed");
            check(fis.read() == -1, "EOF read() == -1");
            check(fis.available() == 0, "available 0 at EOF");
            fis.close();

            // String constructor
            FileInputStream fis2 = new FileInputStream(f.getPath());
            check(Arrays.equals(drain(fis2), data), "String ctor + drain equals content");
            fis2.close();

            // read(byte[]) full
            FileInputStream fis3 = new FileInputStream(f);
            byte[] all = new byte[data.length];
            check(fis3.read(all) == data.length && Arrays.equals(all, data), "read(byte[]) full");
            check(fis3.read() == -1, "EOF after full read");
            fis3.close();

            // missing file
            try {
                new FileInputStream(new File(dir, "missing.bin"));
                check(false, "missing file must throw FileNotFoundException");
            } catch (FileNotFoundException expected) {
                passed++;
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
