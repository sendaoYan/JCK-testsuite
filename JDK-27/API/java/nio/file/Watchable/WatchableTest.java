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

    // ---- appended: overload shapes via reflection ----
    static void testOverloadShapes() throws Exception {
        Class<?> c = Class.forName("java.nio.file.Watchable");
        Class<?> ws = Class.forName("java.nio.file.WatchService");
        Class<?> kindArr = Class.forName("[Ljava.nio.file.WatchEvent$Kind;");
        Class<?> modArr = Class.forName("[Ljava.nio.file.WatchEvent$Modifier;");
        boolean varargsForm = false;
        boolean extendedForm = false;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (!m.getName().equals("register")) continue;
            Class<?>[] ps = m.getParameterTypes();
            if (ps.length == 2 && ps[0] == ws && ps[1] == kindArr) varargsForm = true;
            if (ps.length == 3 && ps[0] == ws && ps[1] == kindArr && ps[2] == modArr)
                extendedForm = true;
        }
        check(varargsForm, "register(WatchService, Kind...) form present");
        check(extendedForm, "register(WatchService, Kind[], Modifier[]) form present");
        check(WatchKey.class.isAssignableFrom(
                      c.getMethod("register", ws, kindArr).getReturnType()),
              "register returns WatchKey");
        check(Watchable.class.isAssignableFrom(Path.class), "Path implements Watchable");
    }

    // ---- appended: arg pass-through + missing/non-directory registration ----
    static void testEdges() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            // a custom Watchable is a plain contract: args pass through verbatim
            final java.nio.file.WatchEvent.Kind<?>[] captured =
                    new java.nio.file.WatchEvent.Kind<?>[1];
            final java.nio.file.WatchEvent.Modifier[] capturedMods =
                    new java.nio.file.WatchEvent.Modifier[1];
            Watchable custom = new Watchable() {
                public WatchKey register(WatchService s, java.nio.file.WatchEvent.Kind<?>... events) {
                    return register(s, events, new java.nio.file.WatchEvent.Modifier[0]);
                }
                public WatchKey register(WatchService s, java.nio.file.WatchEvent.Kind<?>[] events,
                                         java.nio.file.WatchEvent.Modifier... modifiers) {
                    captured[0] = events.length > 0 ? events[0] : null;
                    capturedMods[0] = modifiers.length > 0 ? modifiers[0] : null;
                    return null;
                }
            };
            WatchKey none = custom.register(ws, StandardWatchEventKinds.ENTRY_DELETE);
            check(none == null, "custom implementation return value passes through");
            check(captured[0] == StandardWatchEventKinds.ENTRY_DELETE,
                  "varargs arrive as a Kind[]");
            check(capturedMods[0] == null, "default modifier array is empty");

            boolean nsfe = false;
            try { dir.resolve("ghost").register(ws, StandardWatchEventKinds.ENTRY_CREATE); }
            catch (java.nio.file.NoSuchFileException e) { nsfe = true; }
            check(nsfe, "register missing directory -> NoSuchFileException");
            Path plain = dir.resolve("plain.txt");
            Files.writeString(plain, "p");
            boolean nde = false;
            try { plain.register(ws, StandardWatchEventKinds.ENTRY_CREATE); }
            catch (java.nio.file.NotDirectoryException e) { nde = true; }
            check(nde, "register a regular file -> NotDirectoryException");
            boolean npe = false;
            try { dir.register(ws, (java.nio.file.WatchEvent.Kind<?>[]) null); }
            catch (NullPointerException e) { npe = true; }
            check(npe, "register with null events -> NullPointerException");
        } finally {
            try { Files.deleteIfExists(dir.resolve("plain.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOverloadShapes();
        testEdges();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
