/*
 * @test
 * @summary java.nio.file.FileAlreadyExistsException conformance: white-box structure + black-box behavior.
 * @run main FileAlreadyExistsExceptionTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;

public class FileAlreadyExistsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(FileAlreadyExistsException.class),
              "hierarchy: extends FileSystemException");

        FileAlreadyExistsException e = new FileAlreadyExistsException("f");
        check("f".equals(e.getFile()), "getFile");
    }

    // ---- black-box: behavior ----
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

            java.nio.file.Path src = dir.resolve("src.txt");
            java.nio.file.Files.write(src, "src".getBytes(StandardCharsets.UTF_8));
            boolean copyNoReplace = false;
            try {
                java.nio.file.Files.copy(src, f);
            } catch (FileAlreadyExistsException e) { copyNoReplace = true; }
            check(copyNoReplace, "copy without REPLACE -> FileAlreadyExistsException");
            java.nio.file.Files.copy(src, f, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            check(new String(java.nio.file.Files.readAllBytes(f), StandardCharsets.UTF_8)
                  .equals("src"), "REPLACE_EXISTING overwrites");
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
