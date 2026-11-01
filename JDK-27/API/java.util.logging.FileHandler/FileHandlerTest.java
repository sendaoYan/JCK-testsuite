/*
 * @test
 * @summary java.util.logging.FileHandler conformance: writes log records to a file.
 * @run main FileHandlerTest
 */

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FileHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileHandler() throws Exception {
        File f = new File(System.getProperty("java.io.tmpdir"), "jck_fh_test.log");
        f.delete();
        FileHandler fh = new FileHandler(f.getPath(), true);
        fh.publish(new LogRecord(Level.INFO, "hello file"));
        fh.flush();
        fh.close();
        check(f.exists(), "file created");
        check(f.length() > 0, "file written");
        f.delete();
    }

    public static void main(String[] args) throws Exception {
        testFileHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
