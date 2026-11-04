/*
 * @test
 * @summary java.nio.file.NoSuchFileException conformance: white-box structure + black-box behavior.
 * @run main NoSuchFileExceptionTest
 */

import java.nio.file.NoSuchFileException;

public class NoSuchFileExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(NoSuchFileException.class),
              "hierarchy: extends FileSystemException");

        NoSuchFileException e = new NoSuchFileException("f", "o", "r");
        check("f".equals(e.getFile()) && "o".equals(e.getOtherFile()), "files stored");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path ghost = dir.resolve("ghost.txt");
            boolean read = false;
            try { java.nio.file.Files.readAllBytes(ghost); }
            catch (NoSuchFileException e) {
                read = e.getFile().endsWith("ghost.txt") && e.getOtherFile() == null;
            }
            check(read, "read missing -> NoSuchFileException(file)");
            boolean copy = false;
            try { java.nio.file.Files.copy(ghost, dir.resolve("out.txt")); }
            catch (NoSuchFileException e) {
                copy = e.getFile() != null;
            }
            check(copy, "copy missing source -> NoSuchFileException with file");
            java.nio.file.Files.deleteIfExists(ghost);
            check(true, "deleteIfExists silent");
        } finally {
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
