/*
 * @test
 * @summary java.io.FileOutputStream conformance: full contract coverage.
 *          File/String constructors, truncate vs append, single/offset writes,
 *          getFD/getChannel, FileNotFoundException for a directory target.
 * @run main FileOutputStreamTest
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;

public class FileOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static File newTempDir() throws Exception {
        File d = File.createTempFile("jck-fos", ".tmp");
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

    static byte[] readAll(File f) throws Exception {
        FileInputStream fis = new FileInputStream(f);
        byte[] all = new byte[(int) f.length()];
        int n = fis.read(all);
        fis.close();
        return Arrays.copyOf(all, n);
    }

    static void testBehavior() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "out.bin");

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(new byte[]{1, 2, 3});
            check(fos.getFD() != null, "getFD non-null");
            check(fos.getChannel() != null, "getChannel non-null");
            fos.close();
            check(f.length() == 3, "wrote 3 bytes");

            // append mode
            FileOutputStream app = new FileOutputStream(f, true);
            app.write(new byte[]{4, 5}, 0, 2);
            app.close();
            check(f.length() == 5, "append adds bytes");
            check(Arrays.equals(readAll(f), new byte[]{1, 2, 3, 4, 5}), "append roundtrip");

            // default mode truncates
            FileOutputStream trunc = new FileOutputStream(f);
            trunc.write(9);
            trunc.close();
            check(f.length() == 1, "default mode truncates");
            check(Arrays.equals(readAll(f), new byte[]{9}), "truncate content");

            // write(byte[],off,len) region
            FileOutputStream reg = new FileOutputStream(f);
            reg.write(new byte[]{10, 20, 30, 40}, 1, 2);
            reg.close();
            check(f.length() == 2, "write(byte[],off,len) length");
            check(Arrays.equals(readAll(f), new byte[]{20, 30}), "write(byte[],off,len) content");

            // String constructor + append
            FileOutputStream sc = new FileOutputStream(f.getPath(), true);
            sc.write(new byte[]{7});
            sc.close();
            check(Arrays.equals(readAll(f), new byte[]{20, 30, 7}), "String ctor append");

            // directory target -> FileNotFoundException
            try {
                new FileOutputStream(dir);
                check(false, "directory target must throw FileNotFoundException");
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
