/*
 * @test
 * @summary java.nio.file.AtomicMoveNotSupportedException conformance: white-box structure + black-box behavior.
 * @run main AtomicMoveNotSupportedExceptionTest
 */

import java.nio.file.AtomicMoveNotSupportedException;

public class AtomicMoveNotSupportedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(AtomicMoveNotSupportedException.class), "hierarchy: extends FileSystemException");

        AtomicMoveNotSupportedException e =
                new AtomicMoveNotSupportedException("s", "t", "why");
        check("s".equals(e.getFile()) && "t".equals(e.getOtherFile()), "files stored");
        check("why".equals(e.getReason()), "reason stored");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // cross-file-system ATOMIC_MOVE surfaces this exception
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        java.nio.file.Path other = java.nio.file.Path.of("/dev/shm");
        java.nio.file.FileStore s1 = java.nio.file.Files.getFileStore(dir);
        java.nio.file.FileStore s2 = java.nio.file.Files.getFileStore(other);
        if (s1.equals(s2)) {
            System.out.println("NOTE: tmp and /dev/shm share a store; move part skipped");
            return;
        }
        try {
            java.nio.file.Path src = dir.resolve("am.txt");
            java.nio.file.Files.writeString(src, "m");
            java.nio.file.Path dst = other.resolve("jck-am-" + System.nanoTime() + ".txt");
            boolean threw = false;
            try {
                java.nio.file.Files.move(src, dst, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                threw = true;
                check(src.endsWith("am.txt") ? e.getFile() != null : true,
                      "source carried");
            } finally {
                java.nio.file.Files.deleteIfExists(dst);
            }
            check(threw, "cross-store ATOMIC_MOVE -> AtomicMoveNotSupportedException");
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("am.txt"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
