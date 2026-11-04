/*
 * @test
 * @summary java.io.FileWriter conformance: full contract coverage.
 *          File/String constructors, truncate vs append, write(String)/append(char),
 *          write(String,off,len) region, flush. ASCII content keeps the
 *          default-charset behavior deterministic (JDK 8 has no Charset ctor).
 * @run main FileWriterTest
 */

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static File newTempDir() throws Exception {
        File d = File.createTempFile("jck-fw", ".tmp");
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

    static String readBack(File f) throws Exception {
        FileReader fr = new FileReader(f);
        char[] buf = new char[100];
        int n = fr.read(buf);
        fr.close();
        return new String(buf, 0, n);
    }

    static void testBehavior() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "w.txt");

            FileWriter fw = new FileWriter(f);
            fw.write("ab");
            fw.close();
            check(f.length() == 2, "ASCII length 2");

            FileWriter app = new FileWriter(f, true);
            app.append("cd");
            app.close();
            check(f.length() == 4, "append adds chars");
            check("abcd".equals(readBack(f)), "append roundtrip");

            FileWriter trunc = new FileWriter(f);
            trunc.write("xy");
            trunc.close();
            check(f.length() == 2, "default mode truncates");
            check("xy".equals(readBack(f)), "truncate content");

            FileWriter region = new FileWriter(f);
            region.write("abcdef", 1, 3);
            region.close();
            check("bcd".equals(readBack(f)), "write(String,off,len)");

            FileWriter sc = new FileWriter(f.getPath(), true);
            sc.write("z");
            sc.flush();
            sc.close();
            check("bcdz".equals(readBack(f)), "String ctor append + flush");

            char[] chars = {'P', 'Q', 'R'};
            FileWriter cw = new FileWriter(f);
            cw.write(chars, 1, 2);
            cw.close();
            check("QR".equals(readBack(f)), "write(char[],off,len)");
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
