/*
 * @test
 * @summary java.nio.file.Watchable conformance: hierarchy and methods.
 * @run main WatchableTest
 */

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class WatchableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWatchable() {
        check(Watchable.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : Watchable.class.getMethods()) names.add(m.getName());
        String[] required = { "register" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    // ---- appended: overload shapes via reflection ----
    static void testOverloadShapes() throws Exception {
        Class<?> c = Class.forName("java.nio.file.Watchable");
        Class<?> ws = Class.forName("java.nio.file.WatchService");
        Class<?> kindArr = Class.forName("[Ljava.nio.file.WatchEvent$Kind;");
        Class<?> modArr = Class.forName("[Ljava.nio.file.WatchEvent$Modifier;");
        boolean varargsForm = false;
        boolean extendedForm = false;
        for (Method m : c.getMethods()) {
            if (!m.getName().equals("register")) continue;
            Class<?>[] ps = m.getParameterTypes();
            if (ps.length == 2 && ps[0] == ws && ps[1] == kindArr)
                varargsForm = true;
            if (ps.length == 3 && ps[0] == ws && ps[1] == kindArr && ps[2] == modArr)
                extendedForm = true;
        }
        check(varargsForm, "register(WatchService, Kind...) form present");
        check(extendedForm, "register(WatchService, Kind[], Modifier[]) form present");
        check(WatchKey.class.isAssignableFrom(
                c.getMethod("register", ws, kindArr).getReturnType()),
              "register returns WatchKey");
        // Path is the platform Watchable
        check(Watchable.class.isAssignableFrom(Path.class), "Path implements Watchable");
        check(Modifier.isInterface(c.getModifiers()) && c.getInterfaces().length == 0,
              "top-level interface, no parents");
    }

    // ---- appended: behavior through the interface reference ----
    static void testBehavior() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            Watchable watchable = dir;
            // both interface forms register successfully
            WatchKey kVarargs = watchable.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
            check(kVarargs != null && kVarargs.isValid(), "varargs register yields a valid key");
            check(kVarargs.watchable() == dir, "watchable() reports the directory");
            kVarargs.cancel();
            WatchKey kExtended = watchable.register(ws,
                    new WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE },
                    new WatchEvent.Modifier[0]);
            check(kExtended != null && kExtended.isValid(),
                  "extended register yields a valid key");

            // events flow to a key registered through the interface
            Files.write(dir.resolve("w.txt"), "w".getBytes("UTF-8"));
            WatchKey fired = ws.poll(5, TimeUnit.SECONDS);
            check(fired == kExtended, "interface-registered key fires");
            boolean delivered = false;
            for (WatchEvent<?> e : fired.pollEvents())
                if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE
                        && "w.txt".equals(e.context().toString()))
                    delivered = true;
            check(delivered, "ENTRY_CREATE with exact context delivered");
            fired.reset();

            // a custom Watchable is a plain contract: args pass through verbatim
            final WatchEvent.Kind<?>[] captured = new WatchEvent.Kind<?>[1];
            final WatchEvent.Modifier[] capturedMods = new WatchEvent.Modifier[1];
            Watchable custom = new Watchable() {
                public WatchKey register(WatchService s, WatchEvent.Kind<?>... events) {
                    return register(s, events, new WatchEvent.Modifier[0]);
                }
                public WatchKey register(WatchService s, WatchEvent.Kind<?>[] events,
                                         WatchEvent.Modifier... modifiers) {
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
            WatchEvent.Modifier marker = new WatchEvent.Modifier() {
                public String name() { return "JCK"; } };
            custom.register(ws, new WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE },
                    new WatchEvent.Modifier[] { marker });
            check(capturedMods[0] == marker, "modifier array arrives verbatim");

            // negative registrations with exact exception types (measured)
            boolean iae = false;
            try { watchable.register(ws); }
            catch (IllegalArgumentException e) { iae = true; }
            check(iae, "register with no events -> IllegalArgumentException");
            boolean npe = false;
            try { watchable.register(ws, (WatchEvent.Kind<?>[]) null); }
            catch (NullPointerException e) { npe = true; }
            check(npe, "register with null events -> NullPointerException");
            boolean nsfe = false;
            try { dir.resolve("ghost").register(ws, StandardWatchEventKinds.ENTRY_CREATE); }
            catch (NoSuchFileException e) { nsfe = true; }
            check(nsfe, "register missing directory -> NoSuchFileException");
            Path plain = dir.resolve("plain.txt");
            Files.write(plain, "p".getBytes("UTF-8"));
            boolean nde = false;
            try { plain.register(ws, StandardWatchEventKinds.ENTRY_CREATE); }
            catch (NotDirectoryException e) { nde = true; }
            check(nde, "register a regular file -> NotDirectoryException");
        } finally {
            for (String n : new String[] {"w.txt", "plain.txt"})
                try { Files.deleteIfExists(dir.resolve(n)); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWatchable();
        testOverloadShapes();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
