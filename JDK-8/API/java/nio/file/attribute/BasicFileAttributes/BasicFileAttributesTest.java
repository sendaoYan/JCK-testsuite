/*
 * @test
 * @summary java.nio.file.attribute.BasicFileAttributes conformance: via readAttributes.
 * @run main BasicFileAttributesTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class BasicFileAttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasicFileAttributes() throws Exception {
        Path dir = Files.createTempDirectory("jck-bfa");
        Path file = dir.resolve("f");
        Files.write(file, "hello".getBytes("UTF-8"));
        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
        check(attrs.isRegularFile(), "isRegularFile");
        check(!attrs.isDirectory(), "not directory");
        check(!attrs.isSymbolicLink(), "not symbolic link");
        check(!attrs.isOther(), "not other");
        check(attrs.size() == 5, "size");
        check(attrs.creationTime() != null, "creationTime");
        check(attrs.lastModifiedTime() != null, "lastModifiedTime");
        check(attrs.lastAccessTime() != null, "lastAccessTime");
        check(attrs.fileKey() != null || attrs.fileKey() == null, "fileKey callable");
        Files.delete(file);
        Files.delete(dir);
    }

    public static void main(String[] args) throws Exception {
        testBasicFileAttributes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
