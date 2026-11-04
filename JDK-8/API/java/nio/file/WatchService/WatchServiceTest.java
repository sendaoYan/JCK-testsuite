/*
 * @test
 * @summary java.nio.file.WatchService conformance: white-box structure + black-box behavior (JDK-8).
 * @run main WatchServiceTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

public class WatchServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchService");
        check(c.isInterface(), "WatchService is an interface");
        check(java.io.Closeable.class.isAssignableFrom(c), "extends Closeable");
        for (String n : new String[] {"close", "poll", "take"})
            check(java.util.Arrays.stream(c.getMethods())
                  .anyMatch(m -> m.getName().equals(n)), "has method " + n);
        long pollVariants = java.util.Arrays.stream(c.getMethods())
                .filter(m -> m.getName().equals("poll")).count();
        check(pollVariants == 2, "two poll overloads");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            check(ws.poll() == null, "immediate poll null when idle");
            long t0 = System.nanoTime();
            check(ws.poll(150, TimeUnit.MILLISECONDS) == null,
                  "timed poll null when idle");
            check(System.nanoTime() - t0 >= 140_000_000L, "timeout elapsed");

            dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            Files.write(dir.resolve("t.txt"), "t".getBytes(StandardCharsets.UTF_8));
            java.nio.file.WatchKey pKey = ws.poll(10, TimeUnit.SECONDS);
            check(pKey != null && !pKey.pollEvents().isEmpty(), "poll returns the signalled key");
            pKey.reset();

            Files.delete(dir.resolve("t.txt"));
            java.nio.file.WatchKey dKey = ws.poll(10, TimeUnit.SECONDS);
            boolean sawDelete = false;
            for (java.nio.file.WatchEvent<?> e : dKey.pollEvents())
                if (e.kind() == StandardWatchEventKinds.ENTRY_DELETE) sawDelete = true;
            check(dKey != null && sawDelete, "delete key delivered");
            dKey.reset();

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
