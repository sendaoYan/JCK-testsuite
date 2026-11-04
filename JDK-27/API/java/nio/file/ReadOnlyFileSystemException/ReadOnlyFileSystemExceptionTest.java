/*
 * @test
 * @summary java.nio.file.ReadOnlyFileSystemException conformance: white-box structure + black-box behavior.
 * @run main ReadOnlyFileSystemExceptionTest
 */

import java.nio.file.ReadOnlyFileSystemException;

public class ReadOnlyFileSystemExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.UnsupportedOperationException.class.isAssignableFrom(ReadOnlyFileSystemException.class), "hierarchy: extends UnsupportedOperationException");

        check(new ReadOnlyFileSystemException() != null, "no-arg ctor");
        check(new ReadOnlyFileSystemException().getMessage() == null, "message null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path zip = java.nio.file.Files.createTempFile("jck", ".zip");
        try {
            try (java.util.zip.ZipOutputStream zos =
                    new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("s.txt"));
            }
            // a read-only underlying archive mounts a read-only filesystem
            java.nio.file.Files.setPosixFilePermissions(zip,
                    java.nio.file.attribute.PosixFilePermissions.fromString("r--r--r--"));
            try (java.nio.file.FileSystem zfs = java.nio.file.FileSystems.newFileSystem(zip)) {
                check(zfs.isReadOnly(), "read-only archive mounts read-only fs");
                java.nio.file.Path entry = zfs.getPath("/s.txt");
                boolean threw = false;
                try {
                    java.nio.file.Files.writeString(entry, "nope");
                } catch (ReadOnlyFileSystemException e) {
                    threw = true;
                }
                check(threw, "write on read-only fs -> ReadOnlyFileSystemException");
            }
            java.nio.file.Files.setPosixFilePermissions(zip,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-r--r--"));
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
