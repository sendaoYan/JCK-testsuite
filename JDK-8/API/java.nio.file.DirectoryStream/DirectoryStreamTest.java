/*
 * @test
 * @summary java.nio.file.DirectoryStream conformance: iterate directory entries.
 * @run main DirectoryStreamTest
 */

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDirectoryStream() throws Exception {
        Path dir = Files.createTempDirectory("jck-ds");
        Files.write(dir.resolve("a.txt"), new byte[] {1});
        Files.write(dir.resolve("b.txt"), new byte[] {2});
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            int count = 0;
            for (Path p : ds) count++;
            check(count == 2, "two entries");
        }
        Files.delete(dir.resolve("a.txt"));
        Files.delete(dir.resolve("b.txt"));
        Files.delete(dir);
    }

    public static void main(String[] args) throws Exception {
        testDirectoryStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
