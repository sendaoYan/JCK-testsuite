/*
 * @test
 * @summary java.nio.file.WatchKey conformance: white-box structure + black-box behavior (JDK-8).
 * @run main WatchKeyTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

public class WatchKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchKey");
        check(c.isInterface(), "WatchKey is an interface");
        for (String n : new String[] {"isValid", "cancel", "pollEvents", "reset", "watchable"})
            check(java.util.Arrays.stream(c.getMethods())
                  .anyMatch(m -> m.getName().equals(n)), "has method " + n);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            WatchKey key = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            check(key.isValid(), "fresh key valid");
            check(key.watchable() == dir, "watchable is the directory");

            check(key.pollEvents().isEmpty(), "no events queued initially");
            check(key.reset(), "reset before signals -> true");

            Files.write(dir.resolve("a.txt"), "a".getBytes(StandardCharsets.UTF_8));
            WatchKey fired = ws.poll(10, TimeUnit.SECONDS);
            check(fired == key, "key signalled");
            check(!fired.pollEvents().isEmpty(), "events retrieved");
            check(fired.pollEvents().isEmpty(), "events consumed by retrieval");
            check(fired.reset(), "reset after processing -> true");

            key.cancel();
            check(!key.isValid(), "cancelled key invalid");
            check(key.pollEvents().isEmpty(), "cancelled key has no events");
            check(!key.reset(), "reset on cancelled key -> false");

            WatchKey again = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
            check(again != key && again.isValid(), "fresh key after re-register");
            again.cancel();

            WatchService doomed = FileSystems.getDefault().newWatchService();
            WatchKey k2 = dir.register(doomed, StandardWatchEventKinds.ENTRY_CREATE);
            doomed.close();
            check(!k2.isValid(), "service close invalidates keys");
        } finally {
            try { Files.deleteIfExists(dir.resolve("a.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
