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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
