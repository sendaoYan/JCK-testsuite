/*
 * @test
 * @summary java.nio.file.StandardWatchEventKinds conformance: event kinds.
 * @run main StandardWatchEventKindsTest
 */

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

public class StandardWatchEventKindsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStandardWatchEventKinds() {
        check(StandardWatchEventKinds.ENTRY_CREATE != null, "ENTRY_CREATE");
        check(StandardWatchEventKinds.ENTRY_DELETE != null, "ENTRY_DELETE");
        check(StandardWatchEventKinds.ENTRY_MODIFY != null, "ENTRY_MODIFY");
        check(StandardWatchEventKinds.OVERFLOW != null, "OVERFLOW");
        check("ENTRY_CREATE".equals(StandardWatchEventKinds.ENTRY_CREATE.name()),
              "ENTRY_CREATE name");
        check(StandardWatchEventKinds.ENTRY_CREATE instanceof WatchEvent.Kind,
              "is WatchEvent.Kind");
    }

    // ---- appended: kind identity/type/toString matrix ----
    static void testKindMatrix() {
        WatchEvent.Kind<?>[] kinds = {
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.OVERFLOW };
        check("ENTRY_DELETE".equals(StandardWatchEventKinds.ENTRY_DELETE.name()),
              "ENTRY_DELETE name");
        check("ENTRY_MODIFY".equals(StandardWatchEventKinds.ENTRY_MODIFY.name()),
              "ENTRY_MODIFY name");
        check("OVERFLOW".equals(StandardWatchEventKinds.OVERFLOW.name()), "OVERFLOW name");
        check(StandardWatchEventKinds.ENTRY_CREATE.type() == Path.class, "ENTRY_CREATE type Path");
        check(StandardWatchEventKinds.ENTRY_DELETE.type() == Path.class, "ENTRY_DELETE type Path");
        check(StandardWatchEventKinds.ENTRY_MODIFY.type() == Path.class, "ENTRY_MODIFY type Path");
        check(StandardWatchEventKinds.OVERFLOW.type() == Object.class,
              "OVERFLOW type Object (measured)");
        for (WatchEvent.Kind<?> k : kinds)
            check(k.toString().equals(k.name()), "toString == name for " + k.name());
        // identity: constants are singletons, equality is reference-based
        for (int i = 0; i < kinds.length; i++)
            for (int j = 0; j < kinds.length; j++)
                check(kinds[i].equals(kinds[j]) == (i == j),
                      "identity equality " + kinds[i].name() + " vs " + kinds[j].name());
        for (WatchEvent.Kind<?> k : kinds)
            check(k.hashCode() == k.hashCode(), "hashCode stable for " + k.name());
        java.util.HashSet<WatchEvent.Kind<?>> set =
                new java.util.HashSet<WatchEvent.Kind<?>>();
        for (WatchEvent.Kind<?> k : kinds) set.add(k);
        check(set.size() == 4, "four distinct kinds in a set");
        // a look-alike kind with the same name is NOT the standard constant
        WatchEvent.Kind<Path> fake = new WatchEvent.Kind<Path>() {
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
        check(c.getDeclaredFields().length >= 4, "declares the standard kind fields");
        check(!c.isEnum() && !c.isInterface(), "plain utility class");
    }

    // ---- appended: live events carry the standard kind singletons ----
    // Inotify on ext4 delivers ENTRY_CREATE + ENTRY_MODIFY for a fresh Files.write
    // and ENTRY_DELETE after removal; both behaviours measured stable on this host.
    static void testLiveEvents() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            WatchKey key = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            Path f = dir.resolve("kinds.txt");
            Files.write(f, "v1".getBytes("UTF-8"));
            // CREATE and MODIFY may arrive in separate inotify batches: bounded
            // retry until both kinds have been observed (fail with message, never hang)
            boolean sawCreate = false;
            boolean sawModify = false;
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!(sawCreate && sawModify) && System.nanoTime() < deadline) {
                WatchKey signalled = ws.poll(2, TimeUnit.SECONDS);
                if (signalled == null) break;
                check(signalled == key, "key signalled (must)");
                for (WatchEvent<?> e : signalled.pollEvents()) {
                    if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        sawCreate = true;
                        Path ctx = (Path) e.context();
                        check("kinds.txt".equals(ctx.toString()),
                              "ENTRY_CREATE context is the exact file name");
                        check(!ctx.isAbsolute(), "context is relative to the directory");
                    }
                    if (e.kind() == StandardWatchEventKinds.ENTRY_MODIFY) sawModify = true;
                }
                check(signalled.reset(), "key re-armed");
            }
            check(sawCreate, "ENTRY_CREATE delivered (must)");
            check(sawModify, "ENTRY_MODIFY delivered (ext4/inotify measured stable)");

            Files.delete(f);
            WatchKey delKey = ws.poll(5, TimeUnit.SECONDS);
            check(delKey == key, "delete signalled the same key");
            boolean sawDelete = false;
            for (WatchEvent<?> e : delKey.pollEvents()) {
                if (e.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    sawDelete = true;
                    check("kinds.txt".equals(e.context().toString()),
                          "ENTRY_DELETE context is the exact file name");
                }
            }
            check(sawDelete, "ENTRY_DELETE delivered (must)");
            delKey.reset();
        } finally {
            try { Files.deleteIfExists(dir.resolve("kinds.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testStandardWatchEventKinds();
        testKindMatrix();
        testContainerWhiteBox();
        testLiveEvents();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
