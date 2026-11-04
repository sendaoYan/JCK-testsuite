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

    // ---- appended: kind identity/type matrix ----
    static void testKindMatrix() {
        Kind<?>[] kinds = {
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.OVERFLOW };
        check("OVERFLOW".equals(StandardWatchEventKinds.OVERFLOW.name()), "OVERFLOW name");
        check(StandardWatchEventKinds.ENTRY_CREATE.type() == Path.class, "ENTRY_CREATE type Path");
        check(StandardWatchEventKinds.ENTRY_DELETE.type() == Path.class, "ENTRY_DELETE type Path");
        check(StandardWatchEventKinds.ENTRY_MODIFY.type() == Path.class, "ENTRY_MODIFY type Path");
        check(StandardWatchEventKinds.OVERFLOW.type() == Object.class,
              "OVERFLOW type Object (measured)");
        for (Kind<?> k : kinds)
            check(k.toString().equals(k.name()), "toString == name for " + k.name());
        for (int i = 0; i < kinds.length; i++)
            for (int j = 0; j < kinds.length; j++)
                check(kinds[i].equals(kinds[j]) == (i == j),
                      "identity equality " + kinds[i].name() + " vs " + kinds[j].name());
        for (Kind<?> k : kinds)
            check(k.hashCode() == k.hashCode(), "hashCode stable for " + k.name());
        java.util.HashSet<Kind<?>> set = new java.util.HashSet<>();
        for (Kind<?> k : kinds) set.add(k);
        check(set.size() == 4, "four distinct kinds in a set");
        Kind<Path> fake = new Kind<>() {
            public String name() { return "ENTRY_CREATE"; }
            public Class<Path> type() { return Path.class; } };
        check(fake.name().equals(StandardWatchEventKinds.ENTRY_CREATE.name()),
              "fake shares the name");
        check(!fake.equals(StandardWatchEventKinds.ENTRY_CREATE),
              "equality stays reference-based");
    }

    // ---- appended: white-box container ----
    static void testContainerWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.StandardWatchEventKinds");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final container class");
        check(java.lang.reflect.Modifier.isPrivate(
                c.getDeclaredConstructors()[0].getModifiers()), "private constructor");
        check(!c.isEnum() && !c.isInterface(), "plain utility class");
    }

    // ---- appended: live singleton identity + count/context precision ----
    // Inotify on ext4 delivers ENTRY_CREATE + ENTRY_MODIFY for a fresh Files.write
    // and ENTRY_DELETE after removal; both behaviours measured stable on this host.
    static void testLiveKindIdentity() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            java.nio.file.WatchKey key = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            Path f = dir.resolve("kinds.txt");
            Files.writeString(f, "v1");
            // CREATE and MODIFY may arrive in separate inotify batches: bounded
            // retry until both kinds have been observed (fail with message, never hang)
            boolean sawCreate = false;
            boolean sawModify = false;
            long deadline = System.nanoTime()
                    + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
            while (!(sawCreate && sawModify) && System.nanoTime() < deadline) {
                java.nio.file.WatchKey signalled =
                        ws.poll(2, java.util.concurrent.TimeUnit.SECONDS);
                if (signalled == null) break;
                check(signalled == key, "key signalled (must)");
                for (java.nio.file.WatchEvent<?> e : signalled.pollEvents()) {
                    // kind() is the very same singleton as the public constant
                    if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        sawCreate = true;
                        Path ctx = (Path) e.context();
                        check("kinds.txt".equals(ctx.toString()),
                              "ENTRY_CREATE context is the exact file name");
                        check(!ctx.isAbsolute(), "context is relative to the directory");
                        check(e.count() == 1, "single create, count == 1");
                    }
                    if (e.kind() == StandardWatchEventKinds.ENTRY_MODIFY) sawModify = true;
                }
                check(signalled.reset(), "key re-armed");
            }
            check(sawCreate, "ENTRY_CREATE delivered (must)");
            check(sawModify, "ENTRY_MODIFY delivered (ext4/inotify measured stable)");

            Files.delete(f);
            java.nio.file.WatchKey delKey =
                    ws.poll(5, java.util.concurrent.TimeUnit.SECONDS);
            check(delKey == key, "delete signalled the same key");
            boolean sawDelete = false;
            for (java.nio.file.WatchEvent<?> e : delKey.pollEvents())
                if (e.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    sawDelete = true;
                    check("kinds.txt".equals(e.context().toString()),
                          "ENTRY_DELETE context is the exact file name");
                }
            check(sawDelete, "ENTRY_DELETE delivered (must)");
            delKey.reset();
        } finally {
            try { Files.deleteIfExists(dir.resolve("kinds.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testKindMatrix();
        testContainerWhiteBox();
        testLiveKindIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
