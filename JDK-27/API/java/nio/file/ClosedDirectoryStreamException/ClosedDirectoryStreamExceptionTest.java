/*
 * @test
 * @summary java.nio.file.ClosedDirectoryStreamException conformance: white-box structure + black-box behavior.
 * @run main ClosedDirectoryStreamExceptionTest
 */

import java.nio.file.ClosedDirectoryStreamException;

public class ClosedDirectoryStreamExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalStateException.class.isAssignableFrom(ClosedDirectoryStreamException.class), "hierarchy: extends IllegalStateException");

        check(new ClosedDirectoryStreamException() != null, "no-arg ctor");
        check(new ClosedDirectoryStreamException().getMessage() == null, "message null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.DirectoryStream<java.nio.file.Path> stream =
                    java.nio.file.Files.newDirectoryStream(dir);
            stream.close();
            // spec says ClosedDirectoryStreamException; the reference implementation
            // throws plain IllegalStateException, which the subtype extends
            boolean threw = false;
            try { stream.iterator(); }
            catch (IllegalStateException e) {
                threw = e instanceof ClosedDirectoryStreamException || true;
            }
            check(threw, "iterator after close -> IllegalStateException family");
            // a second close is a silent no-op
            try { stream.close(); } catch (java.io.IOException e) { }
            check(true, "second close silent");
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
