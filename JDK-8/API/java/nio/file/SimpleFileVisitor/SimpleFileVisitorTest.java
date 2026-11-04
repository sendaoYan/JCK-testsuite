/*
 * @test
 * @summary java.nio.file.SimpleFileVisitor conformance: default visit results.
 * @run main SimpleFileVisitorTest
 */

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class SimpleFileVisitorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class V extends SimpleFileVisitor<Path> { }

    static void testSimpleFileVisitor() throws Exception {
        Path dir = Files.createTempDirectory("jck-sfv");
        Path file = dir.resolve("f");
        Files.write(file, new byte[] {1});
        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);

        V v = new V();
        check(v.preVisitDirectory(dir, attrs) == FileVisitResult.CONTINUE, "preVisitDirectory");
        check(v.visitFile(file, attrs) == FileVisitResult.CONTINUE, "visitFile");
        check(v.postVisitDirectory(dir, null) == FileVisitResult.CONTINUE, "postVisitDirectory");
        try {
            v.visitFileFailed(file, new IOException("boom"));
            check(false, "visitFileFailed should rethrow");
        } catch (IOException expected) {
            check(true, "visitFileFailed rethrows");
        }

        Files.delete(file);
        Files.delete(dir);
    }

    public static void main(String[] args) throws Exception {
        testSimpleFileVisitor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
