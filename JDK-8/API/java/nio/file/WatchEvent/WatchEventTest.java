/*
 * @test
 * @summary java.nio.file.WatchEvent conformance: hierarchy and methods.
 * @run main WatchEventTest
 */

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class WatchEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWatchEvent() {
        check(WatchEvent.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : WatchEvent.class.getMethods()) names.add(m.getName());
        String[] required = { "kind", "count", "context" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    // ---- appended: structural detail of the three accessors ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchEvent");
        check(c.getTypeParameters().length == 1, "generic in the context parameter");
        check("T".equals(c.getTypeParameters()[0].getName()), "parameter is <T>");
        Method kind = c.getMethod("kind");
        check(kind.getReturnType() == Class.forName("java.nio.file.WatchEvent$Kind"),
              "kind() returns WatchEvent.Kind");
        Method cnt = c.getMethod("count");
        check(cnt.getReturnType() == int.class, "count() returns int");
        Method ctx = c.getMethod("context");
        check(ctx.getReturnType() == Object.class, "context() returns Object");
        check(Class.forName("java.nio.file.WatchEvent$Kind").isInterface(),
              "nested Kind interface");
        check(Class.forName("java.nio.file.WatchEvent$Modifier").isInterface(),
              "nested Modifier interface");
        check(Modifier.isInterface(c.getModifiers()), "still an interface (recheck)");
    }

    // ---- appended: live event contract (inotify measured stable) ----
    static void testLiveContract() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            WatchKey key = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            Path f = dir.resolve("ev.txt");
            Files.write(f, "v1".getBytes("UTF-8"));
            // CREATE and MODIFY may arrive in separate inotify batches: bounded retry
            boolean sawCreate = false;
            boolean sawModify = false;
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!(sawCreate && sawModify) && System.nanoTime() < deadline) {
                WatchKey signalled = ws.poll(2, TimeUnit.SECONDS);
                if (signalled == null) break;
                check(signalled == key, "key signalled for the written file");
                List<WatchEvent<?>> events = signalled.pollEvents();
                check(!events.isEmpty(), "pollEvents returns a non-empty list");
                for (WatchEvent<?> e : events) {
                    check(e.kind() != null, "kind() never null");
                    check(e.count() >= 1, "count() at least one for " + e.kind().name());
                    if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        sawCreate = true;
                        check(e.count() == 1, "fresh create count == 1");
                        Object ctx = e.context();
                        check(ctx instanceof Path, "context() is a Path for entry events");
                        Path p = (Path) ctx;
                        check("ev.txt".equals(p.toString()), "context is the exact file name");
                        check(!p.isAbsolute(), "context is relative to the watched directory");
                        check(dir.resolve(p).equals(f), "dir.resolve(context) locates the file");
                    }
                    if (e.kind() == StandardWatchEventKinds.ENTRY_MODIFY)
                        sawModify = true;
                }
                // pollEvents consumed the queue: a second fetch is empty
                check(signalled.pollEvents().isEmpty(), "events retrieved exactly once");
                check(signalled.reset(), "key re-armed");
            }
            check(sawCreate, "ENTRY_CREATE observed (must)");
            check(sawModify, "ENTRY_MODIFY observed (ext4/inotify measured stable)");

            Files.delete(f);
            WatchKey delKey = ws.poll(5, TimeUnit.SECONDS);
            check(delKey == key, "delete signalled the same key");
            boolean sawDelete = false;
            for (WatchEvent<?> e : delKey.pollEvents()) {
                check(e.count() >= 1, "delete count at least one");
                if (e.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    sawDelete = true;
                    check("ev.txt".equals(e.context().toString()),
                          "delete context is the exact file name");
                }
            }
            check(sawDelete, "ENTRY_DELETE observed (must)");
            delKey.reset();
        } finally {
            try { Files.deleteIfExists(dir.resolve("ev.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWatchEvent();
        testStructure();
        testLiveContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
