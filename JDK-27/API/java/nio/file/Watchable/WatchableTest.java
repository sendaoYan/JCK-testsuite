/*
 * @test
 * @summary java.nio.file.Watchable conformance: white-box structure + black-box behavior.
 * @run main WatchableTest
 */

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;

public class WatchableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.Watchable");
        check(c.isInterface(), "Watchable is an interface");
        for (String n : new String[] {"register", "register"})
            check(java.util.Arrays.stream(c.getMethods())
                  .anyMatch(m -> m.getName().equals(n)), "has " + n);
        long registerVariants = java.util.Arrays.stream(c.getMethods())
                .filter(m -> m.getName().equals("register")).count();
        check(registerVariants == 2, "two register overloads");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            // Path implements Watchable: registration returns a usable key
            Watchable watchable = dir;
            WatchKey key = watchable.register(ws,
                    new java.nio.file.WatchEvent.Kind<?>[] {
                            StandardWatchEventKinds.ENTRY_CREATE },
                    new java.nio.file.WatchEvent.Modifier[0]);
            check(key != null, "register returns a key");
            check(key.isValid(), "key valid");
            check(key.watchable() == dir, "key reports its watchable");

            // triggering an event delivers it to the service
            Files.writeString(dir.resolve("w.txt"), "w");
            WatchKey fired = ws.poll(10, java.util.concurrent.TimeUnit.SECONDS);
            check(fired == key, "the registered key fired");
            check(fired.pollEvents().stream()
                    .anyMatch(e -> e.kind() == StandardWatchEventKinds.ENTRY_CREATE),
                  "create event delivered");

            // varargs register (no modifiers) behaves identically
            fired.cancel();
            WatchKey simple = dir.register(ws, StandardWatchEventKinds.ENTRY_DELETE);
            check(simple != null && simple.isValid(), "varargs register works");
            simple.cancel();

            // registering no events at all is rejected
            boolean noEvents = false;
            try { dir.register(ws); }
            catch (IllegalArgumentException e) { noEvents = true; }
            check(noEvents, "register with no events -> IllegalArgumentException");

            // a closed watch service rejects registration
            WatchService doomed = FileSystems.getDefault().newWatchService();
            doomed.close();
            boolean closed = false;
            try { dir.register(doomed, StandardWatchEventKinds.ENTRY_CREATE); }
            catch (java.nio.file.ClosedWatchServiceException e) { closed = true; }
            check(closed, "register on closed service -> ClosedWatchServiceException");
        } finally {
            try { Files.deleteIfExists(dir.resolve("w.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
