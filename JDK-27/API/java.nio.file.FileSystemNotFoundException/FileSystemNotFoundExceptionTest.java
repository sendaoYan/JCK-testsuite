/*
 * @test
 * @summary java.nio.file.FileSystemNotFoundException conformance: white-box structure + black-box behavior.
 * @run main FileSystemNotFoundExceptionTest
 */

import java.nio.file.FileSystemNotFoundException;

public class FileSystemNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.RuntimeException.class.isAssignableFrom(FileSystemNotFoundException.class), "hierarchy: extends RuntimeException");

        check(new FileSystemNotFoundException() != null, "no-arg ctor");
        check(new FileSystemNotFoundException("m").getMessage().equals("m"), "message ctor");
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
            java.nio.file.FileSystem fs = java.nio.file.FileSystems.newFileSystem(uri, java.util.Map.of());
            fs.close();
            boolean threw = false;
            try { java.nio.file.FileSystems.getFileSystem(uri); }
            catch (FileSystemNotFoundException e) { threw = true; }
            check(threw, "getFileSystem after close -> FileSystemNotFoundException");
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
