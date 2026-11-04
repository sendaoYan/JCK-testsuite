/*
 * @test
 * @summary java.nio.file.FileSystemException conformance: white-box structure + black-box behavior.
 * @run main FileSystemExceptionTest
 */

import java.nio.file.FileSystemException;

public class FileSystemExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure ----
    static void testWhiteBox() {
        check(java.io.IOException.class.isAssignableFrom(FileSystemException.class),
              "hierarchy: extends IOException");

        FileSystemException e = new FileSystemException("f", "o", "r");
        check("f".equals(e.getFile()), "getFile");
        check("o".equals(e.getOtherFile()), "getOtherFile");
        check("r".equals(e.getReason()), "getReason");
        FileSystemException e2 = new FileSystemException("only");
        check(e2.getOtherFile() == null && e2.getReason() == null, "1-arg nulls");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path missing = dir.resolve("ghost");
            try {
                java.nio.file.Files.readAttributes(missing,
                        java.nio.file.attribute.BasicFileAttributes.class);
                check(false, "readAttributes on missing file should fail");
            } catch (java.nio.file.NoSuchFileException e) {
                check(e.getFile().endsWith("ghost"), "getFile carries path");
                check(e.getOtherFile() == null, "single file involved");
            }
            FileSystemException e = new FileSystemException("a", "b", "why");
            check(e.getMessage() != null && e.getMessage().contains("a")
                  && e.getMessage().contains("b") && e.getMessage().contains("why"),
                  "message composes file/other/reason");
            FileSystemException single = new FileSystemException("solo");
            check(single.getMessage() != null && single.getMessage().contains("solo"),
                  "message composes single file");
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
