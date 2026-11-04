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

    // ---- appended: key identity across registrations (LinuxWatchService measured) ----
    static void testKeyIdentity() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            WatchKey first = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            WatchKey second = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
            // the default provider returns the SAME key for the same watchable
            check(second == first,
                  "re-registering the same directory returns the same key (measured)");
            check(second.watchable() == dir, "watchable still the directory");

            // a second service watching the same directory yields an independent key
            try (WatchService other = FileSystems.getDefault().newWatchService()) {
                WatchKey otherKey = dir.register(other, StandardWatchEventKinds.ENTRY_CREATE);
                check(otherKey != first, "different service -> different key");
                check(otherKey.isValid() && first.isValid(), "both keys valid");
                Files.write(dir.resolve("id.txt"), "i".getBytes(StandardCharsets.UTF_8));
                WatchKey a = ws.poll(5, TimeUnit.SECONDS);
                WatchKey b = other.poll(5, TimeUnit.SECONDS);
                check(a == first, "first service signalled its own key");
                check(b == otherKey, "second service signalled its own key");
                a.reset();
                b.reset();
                otherKey.cancel();
            }
        } finally {
            try { Files.deleteIfExists(dir.resolve("id.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    // ---- appended: cancel semantics and closed-service keys ----
    static void testCancelAndCloseSemantics() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            WatchKey key = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
            // cancel is idempotent
            key.cancel();
            key.cancel();
            check(!key.isValid(), "double cancel still invalid, no exception");
            // watchable() survives cancellation
            check(key.watchable() == dir, "watchable preserved after cancel");
            // cancelled keys drain their queues and refuse re-arming
            check(key.pollEvents().isEmpty(), "cancelled key queue empty");
            check(!key.reset(), "cancelled key reset -> false");
            // a cancelled watchable no longer receives events (inotify measured)
            Files.write(dir.resolve("dead.txt"), "d".getBytes(StandardCharsets.UTF_8));
            WatchKey none = ws.poll(2, TimeUnit.SECONDS);
            check(none == null, "cancelled key receives nothing (must)");

            // re-registration produces a fresh, working key
            WatchKey fresh = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
            check(fresh != key && fresh.isValid(), "fresh key after cancel");
            Files.write(dir.resolve("live.txt"), "l".getBytes(StandardCharsets.UTF_8));
            WatchKey fired = ws.poll(5, TimeUnit.SECONDS);
            check(fired == fresh, "fresh key receives events");
            boolean delivered = false;
            for (java.nio.file.WatchEvent<?> e : fired.pollEvents())
                if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE
                        && "live.txt".equals(e.context().toString()))
                    delivered = true;
            check(delivered, "fresh key sees its own event, not the cancelled one");
            check(fired.reset(), "fresh key re-arms");

            // keys of a closed service stay queryable but dead (measured)
            WatchService doomed = FileSystems.getDefault().newWatchService();
            WatchKey k = dir.register(doomed, StandardWatchEventKinds.ENTRY_CREATE);
            doomed.close();
            check(!k.isValid(), "closed service invalidates keys");
            check(k.pollEvents().isEmpty(), "closed-service key pollEvents empty, no throw");
            check(!k.reset(), "closed-service key reset -> false");
            k.cancel();
            check(true, "closed-service key cancel tolerated");
            check(k.watchable() == dir, "watchable preserved after service close");
        } finally {
            for (String n : new String[] {"dead.txt", "live.txt"})
                try { Files.deleteIfExists(dir.resolve(n)); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testKeyIdentity();
        testCancelAndCloseSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
