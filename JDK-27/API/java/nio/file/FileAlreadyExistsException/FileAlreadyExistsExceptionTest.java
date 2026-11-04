/*
 * @test
 * @summary java.nio.file.FileAlreadyExistsException conformance: white-box structure + black-box behavior.
 * @run main FileAlreadyExistsExceptionTest
 */

import java.nio.file.FileAlreadyExistsException;

public class FileAlreadyExistsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(FileAlreadyExistsException.class), "hierarchy: extends FileSystemException");

        FileAlreadyExistsException e = new FileAlreadyExistsException("f");
        check("f".equals(e.getFile()), "getFile");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("exists.txt");
            java.nio.file.Files.createFile(f);
            boolean create = false;
            try { java.nio.file.Files.createFile(f); }
            catch (FileAlreadyExistsException e) { create = true; }
            check(create, "createFile twice -> FileAlreadyExistsException");

            java.nio.file.Path sub = dir.resolve("sub");
            java.nio.file.Files.createDirectory(sub);
            boolean mkdir = false;
            try { java.nio.file.Files.createDirectory(sub); }
            catch (FileAlreadyExistsException e) { mkdir = true; }
            check(mkdir, "createDirectory twice -> FileAlreadyExistsException");

            java.nio.file.Files.writeString(dir.resolve("src.txt"), "src");
            boolean copyNoReplace = false;
            try {
                java.nio.file.Files.copy(dir.resolve("src.txt"), f);
            } catch (FileAlreadyExistsException e) { copyNoReplace = true; }
            check(copyNoReplace, "copy without REPLACE -> FileAlreadyExistsException");
            // REPLACE_EXISTING overwrites without complaint
            java.nio.file.Files.copy(dir.resolve("src.txt"), f,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            check(java.nio.file.Files.readString(f).equals("src"),
                  "REPLACE_EXISTING overwrites");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dir.resolve("exists.txt")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir.resolve("src.txt")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir.resolve("sub")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir); } catch (Exception ignore) { }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
