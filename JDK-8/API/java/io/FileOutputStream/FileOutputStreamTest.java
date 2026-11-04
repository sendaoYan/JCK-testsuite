/*
 * @test
 * @summary java.io.FileOutputStream API conformance: write bytes to a temp file.
 * @run main FileOutputStreamTest
 */

import java.io.File;
import java.io.FileOutputStream;

public class FileOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileOutputStream() throws Exception {
        File f = File.createTempFile("jck", ".tmp");
        f.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(new byte[]{1, 2, 3});
        }
        check(f.length() == 3, "wrote 3 bytes");
    }

    public static void main(String[] args) throws Exception {
        testFileOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
