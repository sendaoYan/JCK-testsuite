/*
 * @test
 * @summary java.io.FileInputStream API conformance: read bytes back from a temp file.
 * @run main FileInputStreamTest
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileInputStream() throws Exception {
        File f = File.createTempFile("jck", ".tmp");
        f.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(new byte[]{10, 20, 30});
        }
        try (FileInputStream fis = new FileInputStream(f)) {
            check(fis.read() == 10, "read 10");
            check(fis.read() == 20, "read 20");
            check(fis.read() == 30, "read 30");
            check(fis.read() == -1, "EOF");
        }
    }

    public static void main(String[] args) throws Exception {
        testFileInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
