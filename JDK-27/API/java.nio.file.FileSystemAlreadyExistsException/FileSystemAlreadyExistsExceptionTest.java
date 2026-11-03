/*
 * @test
 * @summary java.nio.file.FileSystemAlreadyExistsException conformance: white-box structure + black-box behavior.
 * @run main FileSystemAlreadyExistsExceptionTest
 */

import java.nio.file.FileSystemAlreadyExistsException;

public class FileSystemAlreadyExistsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.RuntimeException.class.isAssignableFrom(FileSystemAlreadyExistsException.class), "hierarchy: extends RuntimeException");

        check(new FileSystemAlreadyExistsException() != null, "no-arg ctor");
        check(new FileSystemAlreadyExistsException("msg").getMessage().equals("msg"),
              "message ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path zip = java.nio.file.Files.createTempFile("jck", ".zip");
        try {
            try (java.util.zip.ZipOutputStream zos =
                    new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("s.txt"));
            }
            java.net.URI uri = new java.net.URI("jar", zip.toUri().toString(), null);
            java.nio.file.FileSystem fs1 = java.nio.file.FileSystems.newFileSystem(uri, java.util.Map.of());
            java.nio.file.FileSystem fs2 = null;
            boolean threw = false;
            try {
                fs2 = java.nio.file.FileSystems.newFileSystem(uri, java.util.Map.of());
            } catch (FileSystemAlreadyExistsException e) {
                threw = true;
            } finally {
                if (fs2 != null) fs2.close();
                fs1.close();
            }
            check(threw, "second open of same URI -> FileSystemAlreadyExistsException");
        } catch (java.net.URISyntaxException e) {
            throw new AssertionError("URI building failed", e);
        } finally {
            java.nio.file.Files.deleteIfExists(zip);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
