/*
 * @test
 * @summary java.nio.file.ClosedWatchServiceException conformance: white-box structure + black-box behavior.
 * @run main ClosedWatchServiceExceptionTest
 */

import java.nio.file.ClosedWatchServiceException;

public class ClosedWatchServiceExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalStateException.class.isAssignableFrom(ClosedWatchServiceException.class), "hierarchy: extends IllegalStateException");

        check(new ClosedWatchServiceException() != null, "no-arg ctor");
        check(new ClosedWatchServiceException().getMessage() == null, "message null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try (java.nio.file.WatchService ws =
                java.nio.file.FileSystems.getDefault().newWatchService()) {
            java.nio.file.WatchKey key =
                    dir.register(ws, java.nio.file.StandardWatchEventKinds.ENTRY_CREATE);
            ws.close();
            boolean threw = false;
            try { ws.poll(); }
            catch (ClosedWatchServiceException e) { threw = true; }
            check(threw, "poll after close -> ClosedWatchServiceException");
            check(!key.isValid(), "keys die with the service");
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
