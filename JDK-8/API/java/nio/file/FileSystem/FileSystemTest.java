/*
 * @test
 * @summary java.nio.file.FileSystem conformance: separator/roots/stores.
 * @run main FileSystemTest
 */

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

public class FileSystemTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileSystem() {
        FileSystem fs = FileSystems.getDefault();
        check(fs.isOpen(), "isOpen");
        check(fs.getSeparator() != null, "getSeparator");
        check(fs.getRootDirectories().iterator().hasNext(), "has root directories");
        check(fs.getFileStores().iterator().hasNext(), "has file stores");
        check(fs.provider() != null, "provider");
        check(fs.getPath("a", "b").getNameCount() == 2, "getPath");
        PathMatcher matcher = fs.getPathMatcher("glob:*.java");
        check(matcher.matches(fs.getPath("Foo.java")), "glob matches Foo.java");
        check(!matcher.matches(fs.getPath("Foo.txt")), "glob not Foo.txt");
        check(fs.getUserPrincipalLookupService() != null, "getUserPrincipalLookupService");
    }

    public static void main(String[] args) {
        testFileSystem();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
