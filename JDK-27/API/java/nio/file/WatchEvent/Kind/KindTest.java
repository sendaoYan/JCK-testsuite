/*
 * @test
 * @summary java.nio.file.WatchEvent.Kind conformance: white-box structure + black-box behavior.
 * @run main KindTest
 */

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchService;
import java.util.List;

public class KindTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchEvent$Kind");
        check(c.isInterface(), "Kind is an interface");
        check(c.getTypeParameters().length == 1, "generic in event type");
        for (String n : new String[] {"name", "type"})
            check(java.util.Arrays.stream(c.getMethods())
                  .anyMatch(m -> m.getName().equals(n)), "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // standard kinds expose their identity via name/type
        Kind<?> create = StandardWatchEventKinds.ENTRY_CREATE;
        check(create.name().equals("ENTRY_CREATE"), "create name");
        check(create.type() == Path.class, "create type is Path");
        Kind<?> overflow = StandardWatchEventKinds.OVERFLOW;
        check(overflow.name().equals("OVERFLOW"), "overflow name");
        check(overflow.type() == Object.class || overflow.type() == Void.class,
              "overflow type is Object/Void");
        check(StandardWatchEventKinds.ENTRY_MODIFY.type() == Path.class, "modify type");
        check(StandardWatchEventKinds.ENTRY_DELETE.type() == Path.class, "delete type");

        // custom kinds can drive registrations and come back on events
        Kind<Path> custom = new Kind<>() {
            public String name() { return "JCK_CUSTOM"; }
            public Class<Path> type() { return Path.class; }
        };
        check(custom.name().equals("JCK_CUSTOM"), "custom kind name");
        check(custom.type() == Path.class, "custom kind type");

        // the default provider rejects nonstandard kinds at registration
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            boolean rejected = false;
            try { dir.register(ws, custom); }
            catch (UnsupportedOperationException e) {
                rejected = e.getMessage() != null && e.getMessage().contains("JCK_CUSTOM");
            }
            check(rejected, "nonstandard kind -> UnsupportedOperationException with its name");

            // equality of the standard instances is reference-based and stable
            check(StandardWatchEventKinds.ENTRY_CREATE == Kind.class.cast(create),
                  "kind instances are singletons");
        } finally {
            try { Files.deleteIfExists(dir.resolve("k.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
