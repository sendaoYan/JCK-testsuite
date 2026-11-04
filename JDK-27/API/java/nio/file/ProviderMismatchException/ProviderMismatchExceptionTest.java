/*
 * @test
 * @summary java.nio.file.ProviderMismatchException conformance: white-box structure + black-box behavior.
 * @run main ProviderMismatchExceptionTest
 */

import java.nio.file.ProviderMismatchException;

public class ProviderMismatchExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(ProviderMismatchException.class), "hierarchy: extends IllegalArgumentException");

        check(new ProviderMismatchException() != null, "no-arg ctor");
        check(new ProviderMismatchException("m").getMessage().equals("m"), "message ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // default-provider operations reject paths from a foreign provider
        java.nio.file.Path zip = java.nio.file.Files.createTempFile("jck", ".zip");
        try {
            try (java.util.zip.ZipOutputStream zos =
                    new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("s.txt"));
            }
            try (java.nio.file.FileSystem zfs = java.nio.file.FileSystems.newFileSystem(zip)) {
                java.nio.file.Path zpath = zfs.getPath("/s.txt");
                java.nio.file.spi.FileSystemProvider def =
                        java.nio.file.FileSystems.getDefault().provider();
                boolean threw = false;
                try { def.readAttributes(zpath,
                        java.nio.file.attribute.BasicFileAttributes.class); }
                catch (ProviderMismatchException e) { threw = true; }
                check(threw, "default provider + zip path -> ProviderMismatchException");
            }
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
