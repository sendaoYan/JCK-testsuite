/*
 * @test
 * @summary java.io.FileWriter API conformance: write characters to a temp file.
 * @run main FileWriterTest
 */

import java.io.File;
import java.io.FileWriter;

public class FileWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileWriter() throws Exception {
        File f = File.createTempFile("jck", ".tmp");
        f.deleteOnExit();
        try (FileWriter fw = new FileWriter(f)) {
            fw.write("abc");
        }
        check(f.length() == 3, "wrote 3 chars");
    }

    public static void main(String[] args) throws Exception {
        testFileWriter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
