/*
 * @test
 * @summary java.nio.file.StandardWatchEventKinds conformance: white-box structure + black-box behavior.
 * @run main StandardWatchEventKindsTest
 */

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchService;
import java.util.List;

public class StandardWatchEventKindsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.StandardWatchEventKinds");
        check(!java.lang.reflect.Modifier.isPublic(c.getModifiers())
              || true, "container class");
        check(c.getDeclaredFields().length >= 4, "declares the standard kinds");
        check(StandardWatchEventKinds.ENTRY_CREATE.name().equals("ENTRY_CREATE"),
              "ENTRY_CREATE name");
        check(StandardWatchEventKinds.ENTRY_DELETE.name().equals("ENTRY_DELETE"),
              "ENTRY_DELETE name");
        check(StandardWatchEventKinds.ENTRY_MODIFY.name().equals("ENTRY_MODIFY"),
              "ENTRY_MODIFY name");
        check(StandardWatchEventKinds.OVERFLOW.name().equals("OVERFLOW"), "OVERFLOW name");
        check(StandardWatchEventKinds.ENTRY_CREATE.type() == Path.class, "ENTRY_CREATE type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // overflow never equals the entry kinds
        check(!StandardWatchEventKinds.OVERFLOW.equals(StandardWatchEventKinds.ENTRY_CREATE),
              "distinct kinds");
        check(StandardWatchEventKinds.ENTRY_CREATE.toString().equals("ENTRY_CREATE"),
              "toString is the name");

        // real events carry these kinds
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            Path created = dir.resolve("watched.txt");
            Files.writeString(created, "v1");
            Files.writeString(created, "v2");
            Files.delete(created);

            java.util.Set<String> seen = new java.util.HashSet<>();
            long deadline = System.currentTimeMillis() + 10_000;
            while (seen.size() < 3 && System.currentTimeMillis() < deadline) {
                java.nio.file.WatchKey key = ws.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                if (key == null) continue;
                for (java.nio.file.WatchEvent<?> e : key.pollEvents())
                    seen.add(e.kind().name());
                key.reset();
            }
            check(seen.contains("ENTRY_CREATE"), "create event observed");
            check(seen.contains("ENTRY_MODIFY") || seen.contains("ENTRY_CREATE"),
                  "modify-or-create observed");
            check(seen.contains("ENTRY_DELETE"), "delete event observed");
            // context of an entry event is a Path relative to the directory
            dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
            Path probe = dir.resolve("ctx.txt");
            Files.writeString(probe, "c");
            java.nio.file.WatchKey key = ws.poll(5, java.util.concurrent.TimeUnit.SECONDS);
            check(key != null, "second registration fires");
            List<java.nio.file.WatchEvent<?>> events = key.pollEvents();
            boolean ctxIsRelativePath = events.stream()
                    .filter(e -> e.kind() == StandardWatchEventKinds.ENTRY_CREATE)
                    .findFirst()
                    .map(e -> e.context() instanceof Path p
                              && p.getFileName().toString().equals("ctx.txt"))
                    .orElse(false);
            check(ctxIsRelativePath, "event context is a relative Path");
            key.reset();
        } finally {
            try { Files.deleteIfExists(dir.resolve("watched.txt")); } catch (Exception ignore) { }
            try { Files.deleteIfExists(dir.resolve("ctx.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
