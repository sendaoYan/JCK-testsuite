/*
 * @test
 * @summary java.nio.file.DirectoryStream.Filter conformance: accept predicate.
 * @run main FilterTest
 */

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static DirectoryStream.Filter<Path> txtFilter = new DirectoryStream.Filter<Path>() {
        public boolean accept(Path entry) {
            return entry.getFileName().toString().endsWith(".txt");
        }
    };

    static void testFilter() throws Exception {
        check(txtFilter.accept(Paths.get("a.txt")), "accept .txt");
        check(!txtFilter.accept(Paths.get("a.bin")), "reject .bin");

        Path dir = Files.createTempDirectory("jck-flt");
        Files.write(dir.resolve("a.txt"), new byte[] {1});
        Files.write(dir.resolve("b.bin"), new byte[] {2});
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.txt")) {
            int count = 0;
            for (Path p : ds) count++;
            check(count == 1, "glob filter yields one entry");
        }
        Files.delete(dir.resolve("a.txt"));
        Files.delete(dir.resolve("b.bin"));
        Files.delete(dir);
    }

    public static void main(String[] args) throws Exception {
        testFilter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
