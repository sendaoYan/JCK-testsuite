/*
 * @test
 * @summary java.nio.file.NotDirectoryException conformance: white-box structure + black-box behavior.
 * @run main NotDirectoryExceptionTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.NotDirectoryException;

public class NotDirectoryExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(NotDirectoryException.class),
              "hierarchy: extends FileSystemException");

        NotDirectoryException e = new NotDirectoryException("f");
        check("f".equals(e.getFile()), "getFile");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path file = dir.resolve("plain.txt");
            java.nio.file.Files.write(file, "p".getBytes(StandardCharsets.UTF_8));
            boolean threw = false;
            try { java.nio.file.Files.newDirectoryStream(file).close(); }
            catch (NotDirectoryException e) { threw = e.getFile() != null; }
            check(threw, "stream a regular file -> NotDirectoryException");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dir.resolve("plain.txt")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir); } catch (Exception ignore) { }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
