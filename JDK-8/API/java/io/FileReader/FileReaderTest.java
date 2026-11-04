/*
 * @test
 * @summary java.io.FileReader API conformance: read characters from a temp file.
 * @run main FileReaderTest
 */

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileReader() throws Exception {
        File f = File.createTempFile("jck", ".tmp");
        f.deleteOnExit();
        try (FileWriter fw = new FileWriter(f)) {
            fw.write("hello");
        }
        try (FileReader fr = new FileReader(f)) {
            check(fr.read() == 'h', "read h");
            check(fr.read() == 'e', "read e");
            check(fr.read() == 'l', "read l");
        }
    }

    public static void main(String[] args) throws Exception {
        testFileReader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
