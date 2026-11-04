/*
 * @test
 * @summary java.nio.file.WatchEvent.Modifier conformance: white-box structure + black-box behavior.
 * @run main ModifierTest
 */

import com.sun.nio.file.SensitivityWatchEventModifier;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchService;

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchEvent$Modifier");
        check(c.isInterface(), "Modifier is an interface");
        check(java.util.Arrays.stream(c.getMethods())
              .anyMatch(m -> m.getName().equals("name")), "has name()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // custom modifier: name() is its only contract
        Modifier custom = new Modifier() {
            public String name() { return "JCK_MODIFIER"; }
        };
        check(custom.name().equals("JCK_MODIFIER"), "custom modifier name");

        // a known JDK modifier tweaks watch sensitivity and registers fine
        Modifier high = SensitivityWatchEventModifier.HIGH;
        check(high.name().equals("HIGH"), "HIGH sensitivity modifier name");
        check(java.util.Arrays.stream(SensitivityWatchEventModifier.values())
              .anyMatch(m -> m.name().equals("LOW")), "LOW modifier exists");

        // modifiers flow through the extended register overload
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            java.nio.file.WatchKey key = dir.register(ws,
                    new java.nio.file.WatchEvent.Kind<?>[] {
                            StandardWatchEventKinds.ENTRY_CREATE },
                    new Modifier[] { high });
            check(key != null && key.isValid(), "modified registration accepted");
            Files.writeString(dir.resolve("m.txt"), "m");
            java.nio.file.WatchKey fired =
                    ws.poll(10, java.util.concurrent.TimeUnit.SECONDS);
            check(fired != null && fired.pollEvents().stream()
                    .anyMatch(e -> e.kind() == StandardWatchEventKinds.ENTRY_CREATE),
                  "events still delivered with modifier");
            fired.reset();
            key.cancel();
        } finally {
            try { Files.deleteIfExists(dir.resolve("m.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    // ---- appended: interface shape + empty/unknown modifier matrix (measured) ----
    static void testShapeAndEdges() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchEvent$Modifier");
        java.lang.reflect.Method[] declared = c.getDeclaredMethods();
        check(declared.length == 1, "exactly one declared method");
        check(java.lang.reflect.Modifier.isAbstract(declared[0].getModifiers()),
              "name() is abstract");
        check(c.getTypeParameters().length == 0, "not generic");
        check(c.getInterfaces().length == 0, "no parent interfaces");
        check(SensitivityWatchEventModifier.values().length == 3,
              "three sensitivity levels (measured)");
        check("MEDIUM".equals(SensitivityWatchEventModifier.MEDIUM.name()),
              "MEDIUM sensitivity name");

        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            // empty modifier array is always fine
            java.nio.file.WatchKey plain = dir.register(ws,
                    new java.nio.file.WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE },
                    new Modifier[0]);
            check(plain != null && plain.isValid(), "empty modifier array accepted");
            plain.cancel();

            // unknown modifier is rejected with the exact message (measured)
            Modifier bogus = new Modifier() {
                public String name() { return "JCK_BOGUS_MOD"; } };
            boolean uoe = false;
            String msg = null;
            try {
                dir.register(ws,
                        new java.nio.file.WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE },
                        new Modifier[] { bogus });
            } catch (UnsupportedOperationException e) { uoe = true; msg = e.getMessage(); }
            check(uoe, "unknown modifier -> UnsupportedOperationException");
            check("Modifier not supported".equals(msg),
                  "message is 'Modifier not supported' (measured)");
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testShapeAndEdges();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
