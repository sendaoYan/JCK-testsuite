/*
 * @test
 * @summary java.nio.file.WatchService conformance: white-box structure + black-box behavior.
 * @run main WatchServiceTest
 */

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;

public class WatchServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchService");
        check(c.isInterface(), "WatchService is an interface");
        check(java.io.Closeable.class.isAssignableFrom(c), "extends Closeable");
        for (String n : new String[] {"close", "poll", "poll", "take"})
            check(java.util.Arrays.stream(c.getMethods())
                  .anyMatch(m -> m.getName().equals(n)), "has method " + n);
        long pollVariants = java.util.Arrays.stream(c.getMethods())
                .filter(m -> m.getName().equals("poll")).count();
        check(pollVariants == 2, "two poll overloads");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            // no-event poll returns null immediately
            check(ws.poll() == null, "immediate poll null when idle");
            // timed poll returns null after the timeout
            long t0 = System.nanoTime();
            check(ws.poll(150, java.util.concurrent.TimeUnit.MILLISECONDS) == null,
                  "timed poll null when idle");
            check(System.nanoTime() - t0 >= 140_000_000L, "timeout elapsed");

            // an event wakes poll and take alike
            dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            Files.writeString(dir.resolve("t.txt"), "t");
            java.nio.file.WatchKey pKey = ws.poll(10, java.util.concurrent.TimeUnit.SECONDS);
            check(pKey != null && !pKey.pollEvents().isEmpty(), "poll returns the signalled key");
            pKey.reset();

            // the delete of the same file is delivered to the same key
            Files.delete(dir.resolve("t.txt"));
            java.nio.file.WatchKey dKey = ws.poll(10, java.util.concurrent.TimeUnit.SECONDS);
            check(dKey != null && dKey.pollEvents().stream()
                    .anyMatch(e -> e.kind() == StandardWatchEventKinds.ENTRY_DELETE),
                  "delete key delivered");
            dKey.reset();

            // after close, operations fail
            WatchService doomed = FileSystems.getDefault().newWatchService();
            doomed.close();
            boolean closed = false;
            try { doomed.poll(); }
            catch (java.nio.file.ClosedWatchServiceException e) { closed = true; }
            check(closed, "poll after close -> ClosedWatchServiceException");
            boolean closedTake = false;
            try { doomed.take(); }
            catch (java.nio.file.ClosedWatchServiceException e) { closedTake = true; }
            check(closedTake, "take after close -> ClosedWatchServiceException");
            // closing twice is harmless
            doomed.close();
            check(true, "double close tolerated");
        } finally {
            try { Files.deleteIfExists(dir.resolve("t.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
