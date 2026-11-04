/*
 * @test
 * @summary java.nio.file.NotLinkException conformance: white-box structure + black-box behavior.
 * @run main NotLinkExceptionTest
 */

import java.nio.file.NotLinkException;

public class NotLinkExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(NotLinkException.class), "hierarchy: extends FileSystemException");

        NotLinkException e = new NotLinkException("f");
        check("f".equals(e.getFile()), "getFile");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path plain = dir.resolve("plain.txt");
            java.nio.file.Files.writeString(plain, "x");
            boolean threw = false;
            try { java.nio.file.Files.readSymbolicLink(plain); }
            catch (NotLinkException e) { threw = e.getFile() != null; }
            check(threw, "readSymbolicLink on regular file -> NotLinkException");
            // a real link reads back its target instead
            java.nio.file.Path link = dir.resolve("link");
            java.nio.file.Files.createSymbolicLink(link, plain);
            check(java.nio.file.Files.readSymbolicLink(link).equals(plain),
                  "real link resolves");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dir.resolve("link")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir.resolve("plain.txt")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir); } catch (Exception ignore) { }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
