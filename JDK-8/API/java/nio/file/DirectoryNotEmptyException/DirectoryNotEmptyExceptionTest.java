/*
 * @test
 * @summary java.nio.file.DirectoryNotEmptyException conformance: white-box structure + black-box behavior.
 * @run main DirectoryNotEmptyExceptionTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;

public class DirectoryNotEmptyExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(DirectoryNotEmptyException.class),
              "hierarchy: extends FileSystemException");

        DirectoryNotEmptyException e = new DirectoryNotEmptyException("dir");
        check("dir".equals(e.getFile()), "getFile");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path child = dir.resolve("keep.txt");
            java.nio.file.Files.write(child, "k".getBytes(StandardCharsets.UTF_8));
            boolean threw = false;
            try { java.nio.file.Files.delete(dir); }
            catch (DirectoryNotEmptyException e) {
                threw = e.getFile().endsWith(dir.getFileName().toString());
            }
            check(threw, "deleting non-empty dir -> DirectoryNotEmptyException with file");
            java.nio.file.Files.delete(child);
            java.nio.file.Files.delete(dir);
            check(!java.nio.file.Files.exists(dir), "emptied dir deletes cleanly");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dir.resolve("keep.txt")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir); } catch (Exception ignore) { }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
