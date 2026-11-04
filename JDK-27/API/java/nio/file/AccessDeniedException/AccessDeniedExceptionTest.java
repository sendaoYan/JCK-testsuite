/*
 * @test
 * @summary java.nio.file.AccessDeniedException conformance: white-box structure + black-box behavior.
 * @run main AccessDeniedExceptionTest
 */

import java.nio.file.AccessDeniedException;

public class AccessDeniedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(AccessDeniedException.class), "hierarchy: extends FileSystemException");

        AccessDeniedException e = new AccessDeniedException("file");
        check("file".equals(e.getFile()), "getFile");
        check(e.getReason() == null, "no reason by default");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        if (System.getProperty("user.name").equals("root")) {
            System.out.println("NOTE: running as root; permission denial part skipped");
            return;
        }
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("denied.txt");
            java.nio.file.Files.writeString(f, "x");
            java.nio.file.Files.setPosixFilePermissions(f,
                    java.util.EnumSet.noneOf(java.nio.file.attribute.PosixFilePermission.class));
            boolean denied = false;
            try {
                java.nio.file.Files.newByteChannel(f,
                        java.nio.file.StandardOpenOption.READ).close();
            } catch (AccessDeniedException e) {
                denied = true;
                check(e.getFile() != null, "denied exception carries file");
            }
            check(denied, "000-mode file -> AccessDeniedException");
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("denied.txt"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
