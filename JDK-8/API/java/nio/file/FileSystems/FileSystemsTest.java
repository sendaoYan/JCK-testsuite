/*
 * @test
 * @summary java.nio.file.FileSystems conformance: default filesystem.
 * @run main FileSystemsTest
 */

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class FileSystemsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileSystems() {
        FileSystem fs = FileSystems.getDefault();
        check(fs != null, "getDefault");
        check(fs.getSeparator() != null, "separator");
        check(fs.isOpen(), "isOpen");
        check(fs.provider() != null, "provider");
        check(fs.supportedFileAttributeViews() != null, "supportedFileAttributeViews");
    }

    public static void main(String[] args) {
        testFileSystems();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
