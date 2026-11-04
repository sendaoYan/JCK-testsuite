/*
 * @test
 * @summary java.io.FileReader conformance: full contract coverage.
 *          File/String constructors, character reads (single/bulk/offset),
 *          EOF, FileNotFoundException for a missing file. ASCII content keeps
 *          the default-charset behavior deterministic (JDK 8 has no Charset ctor).
 * @run main FileReaderTest
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

public class FileReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static File newTempDir() throws Exception {
        File d = File.createTempFile("jck-fr", ".tmp");
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

    static void testBehavior() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "text.txt");
            FileWriter fw = new FileWriter(f);
            fw.write("hello-file");
            fw.close();

            FileReader fr = new FileReader(f);
            check(fr.read() == 'h', "read h");
            check(fr.read() == 'e', "read e");
            check(fr.read() == 'l', "read l");
            char[] buf = new char[4];
            check(fr.read(buf, 1, 3) == 3, "read(char[],off,len) count");
            check(new String(buf, 1, 3).equals("lo-"), "read(char[],off,len) content");
            check(fr.read() == 'f', "read f after bulk");
            check(fr.read() != -1, "more chars remain");
            fr.close();

            FileReader fr2 = new FileReader(f.getPath());
            char[] all = new char[100];
            int n = fr2.read(all);
            check("hello-file".equals(new String(all, 0, n)), "String ctor full read");
            check(fr2.read() == -1, "EOF");
            fr2.close();

            try {
                new FileReader(new File(dir, "nope.txt"));
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
