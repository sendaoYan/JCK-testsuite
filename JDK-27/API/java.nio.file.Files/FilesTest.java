/*
 * @test
 * @summary java.nio.file.Files conformance: create/read/write/delete.
 * @run main FilesTest
 */

import java.nio.file.Files;
import java.nio.file.Path;

public class FilesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFiles() throws Exception {
        Path dir = Files.createTempDirectory("jck-files");
        check(Files.isDirectory(dir), "isDirectory");
        Path file = dir.resolve("f.txt");
        Files.write(file, "hello".getBytes("UTF-8"));
        check(Files.exists(file), "exists");
        check(Files.isRegularFile(file), "isRegularFile");
        check(Files.size(file) == 5, "size");
        check(Files.readAllBytes(file).length == 5, "readAllBytes");
        check(Files.getLastModifiedTime(file) != null, "getLastModifiedTime");
        Files.delete(file);
        check(!Files.exists(file), "deleted");
        Files.delete(dir);
        check(!Files.exists(dir), "dir deleted");
    }

    public static void main(String[] args) throws Exception {
        testFiles();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
