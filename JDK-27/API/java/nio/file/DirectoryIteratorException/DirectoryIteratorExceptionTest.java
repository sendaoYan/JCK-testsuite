/*
 * @test
 * @summary java.nio.file.DirectoryIteratorException conformance: white-box structure + black-box behavior.
 * @run main DirectoryIteratorExceptionTest
 */

import java.nio.file.DirectoryIteratorException;

public class DirectoryIteratorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.RuntimeException.class.isAssignableFrom(DirectoryIteratorException.class), "hierarchy: extends RuntimeException");

        java.io.IOException cause = new java.io.IOException("boom");
        DirectoryIteratorException e = new DirectoryIteratorException(cause);
        check(e.getCause() == cause, "cause stored");
        check(e instanceof java.util.ConcurrentModificationException,
              "hierarchy: extends ConcurrentModificationException");
        check(e.getCause() instanceof java.io.IOException, "cause is an IOException");
        boolean npe = false;
        try { new DirectoryIteratorException(null); }
        catch (NullPointerException x) { npe = true; }
        check(npe, "null cause -> NullPointerException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Files.writeString(dir.resolve("a.txt"), "a");
            // a filter failing with IOException surfaces wrapped at iteration time
            java.io.IOException boom = new java.io.IOException("boom");
            java.nio.file.DirectoryStream<java.nio.file.Path> stream =
                    java.nio.file.Files.newDirectoryStream(dir, entry -> { throw boom; });
            boolean wrapped = false;
            try {
                stream.iterator().hasNext();
            } catch (DirectoryIteratorException e) {
                wrapped = e.getCause() == boom;
            } finally {
                stream.close();
            }
            check(wrapped, "filter IOException -> DirectoryIteratorException wrapping it");
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("a.txt"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
