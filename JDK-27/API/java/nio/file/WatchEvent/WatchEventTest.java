/*
 * @test
 * @summary java.nio.file.WatchEvent conformance: white-box structure + black-box behavior.
 * @run main WatchEventTest
 */

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.util.List;

public class WatchEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchEvent");
        check(c.isInterface(), "WatchEvent is an interface");
        check(c.getTypeParameters().length == 1, "generic in kind parameter");
        for (String n : new String[] {"kind", "context", "count"})
            check(java.util.Arrays.stream(c.getMethods())
                  .anyMatch(m -> m.getName().equals(n)), "has method " + n);
        check(Class.forName("java.nio.file.WatchEvent$Kind").isInterface(),
              "nested Kind interface");
        check(Class.forName("java.nio.file.WatchEvent$Modifier").isInterface(),
              "nested Modifier interface");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            Path f = dir.resolve("ev.txt");
            Files.writeString(f, "1");
            Files.writeString(f, "2");

            java.nio.file.WatchKey key = ws.poll(10, java.util.concurrent.TimeUnit.SECONDS);
            check(key != null, "events arrived");
            List<WatchEvent<?>> events = key.pollEvents();
            check(!events.isEmpty(), "non-empty event list");

            boolean sawCreate = false;
            boolean sawContext = false;
            for (WatchEvent<?> e : events) {
                check(e.kind() != null, "kind non-null");
                check(e.count() >= 1, "count at least one");
                if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    sawCreate = true;
                    Object ctx = e.context();
                    if (ctx instanceof Path p
                            && p.getFileName().toString().equals("ev.txt"))
                        sawContext = true;
                }
            }
            check(sawCreate, "ENTRY_CREATE observed");
            check(sawContext, "create context is the relative path of the file");
            key.reset();
        } finally {
            try { Files.deleteIfExists(dir.resolve("ev.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    // ---- appended: accessor structural detail ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchEvent");
        check("T".equals(c.getTypeParameters()[0].getName()), "parameter is <T>");
        check(c.getMethod("kind").getReturnType()
                      == Class.forName("java.nio.file.WatchEvent$Kind"),
              "kind() returns WatchEvent.Kind");
        check(c.getMethod("count").getReturnType() == int.class, "count() returns int");
        check(c.getMethod("context").getReturnType() == Object.class,
              "context() returns Object");
    }

    // ---- appended: live precision (context resolution / poll-once / delete) ----
    static void testLivePrecision() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            java.nio.file.WatchKey key = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            Path f = dir.resolve("prec.txt");
            Files.writeString(f, "v");
            // CREATE and MODIFY may arrive in separate inotify batches: bounded retry
            boolean sawCreate = false;
            boolean sawModify = false;
            long deadline = System.currentTimeMillis() + 5_000;
            while (!(sawCreate && sawModify) && System.currentTimeMillis() < deadline) {
                java.nio.file.WatchKey signalled =
                        ws.poll(2, java.util.concurrent.TimeUnit.SECONDS);
                if (signalled == null) break;
                check(signalled == key, "key signalled");
                List<WatchEvent<?>> events = signalled.pollEvents();
                for (WatchEvent<?> e : events) {
                    check(e.kind() != null, "kind() never null");
                    check(e.count() >= 1, "count() at least one");
                    if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        sawCreate = true;
                        check(e.count() == 1, "fresh create count == 1");
                        Path p = (Path) e.context();
                        check("prec.txt".equals(p.toString()), "context exact file name");
                        check(!p.isAbsolute(), "context relative to the directory");
                        check(dir.resolve(p).equals(f), "dir.resolve(context) locates the file");
                    }
                    if (e.kind() == StandardWatchEventKinds.ENTRY_MODIFY) sawModify = true;
                }
                check(signalled.pollEvents().isEmpty(), "events retrieved exactly once");
                check(signalled.reset(), "key re-armed");
            }
            check(sawCreate, "ENTRY_CREATE observed (must)");
            check(sawModify, "ENTRY_MODIFY observed (ext4/inotify measured stable)");

            Files.delete(f);
            java.nio.file.WatchKey delKey =
                    ws.poll(5, java.util.concurrent.TimeUnit.SECONDS);
            check(delKey == key, "delete signalled the same key");
            boolean sawDelete = false;
            for (WatchEvent<?> e : delKey.pollEvents())
                if (e.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    sawDelete = true;
                    check("prec.txt".equals(e.context().toString()),
                          "delete context exact file name");
                }
            check(sawDelete, "ENTRY_DELETE observed (must)");
            delKey.reset();
        } finally {
            try { Files.deleteIfExists(dir.resolve("prec.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testStructure();
        testLivePrecision();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
