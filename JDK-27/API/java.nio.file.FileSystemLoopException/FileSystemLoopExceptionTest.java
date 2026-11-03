/*
 * @test
 * @summary java.nio.file.FileSystemLoopException conformance: white-box structure + black-box behavior.
 * @run main FileSystemLoopExceptionTest
 */

import java.nio.file.FileSystemLoopException;

public class FileSystemLoopExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(FileSystemLoopException.class), "hierarchy: extends FileSystemException");

        FileSystemLoopException e = new FileSystemLoopException("f");
        check("f".equals(e.getFile()), "getFile");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            // dir/link -> dir : following it loops
            java.nio.file.Files.createSymbolicLink(dir.resolve("loop"), dir);
            java.util.List<java.nio.file.Path> visited = new java.util.ArrayList<>();
            boolean looped = false;
            try {
                java.nio.file.Files.walkFileTree(dir,
                        java.util.EnumSet.of(java.nio.file.FileVisitOption.FOLLOW_LINKS),
                        100, new java.nio.file.SimpleFileVisitor<>() {
                    public java.nio.file.FileVisitResult visitFile(java.nio.file.Path p,
                            java.nio.file.attribute.BasicFileAttributes a) {
                        visited.add(p);
                        return java.nio.file.FileVisitResult.CONTINUE;
                    }
                });
            } catch (FileSystemLoopException e) {
                looped = e.getFile() != null;
            }
            check(looped, "symlink cycle walk -> FileSystemLoopException with file");
            // without FOLLOW_LINKS the same tree walks fine
            java.nio.file.Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<>() {
                public java.nio.file.FileVisitResult visitFile(java.nio.file.Path p,
                        java.nio.file.attribute.BasicFileAttributes a) {
                    return java.nio.file.FileVisitResult.CONTINUE;
                }
            });
            check(true, "no-follow walk terminates");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dir.resolve("loop")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir); } catch (Exception ignore) { }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
