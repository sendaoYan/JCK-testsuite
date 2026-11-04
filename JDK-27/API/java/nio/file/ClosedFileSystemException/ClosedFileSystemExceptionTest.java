/*
 * @test
 * @summary java.nio.file.ClosedFileSystemException conformance: white-box structure + black-box behavior.
 * @run main ClosedFileSystemExceptionTest
 */

import java.nio.file.ClosedFileSystemException;

public class ClosedFileSystemExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalStateException.class.isAssignableFrom(ClosedFileSystemException.class), "hierarchy: extends IllegalStateException");

        check(new ClosedFileSystemException() != null, "no-arg ctor");
        check(new ClosedFileSystemException().getMessage() == null, "message null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path zip = java.nio.file.Files.createTempFile("jck", ".zip");
        try {
            // seed an empty zip then open it as a filesystem
            try (java.util.zip.ZipOutputStream zos =
                    new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("seed.txt"));
                zos.write("s".getBytes());
            }
            java.nio.file.FileSystem fs = java.nio.file.FileSystems.newFileSystem(zip);
            check(fs.isOpen(), "zip fs open");
            fs.close();
            check(!fs.isOpen(), "zip fs closed");
            boolean threw = false;
            try {
                java.nio.file.Files.writeString(fs.getPath("/seed.txt"), "x");
            } catch (ClosedFileSystemException e) {
                threw = true;
            }
            check(threw, "write after close -> ClosedFileSystemException");
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
