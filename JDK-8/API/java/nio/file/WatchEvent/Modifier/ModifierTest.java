/*
 * @test
 * @summary java.nio.file.WatchEvent.Modifier conformance: name() method.
 * @run main ModifierTest
 */

import com.sun.nio.file.SensitivityWatchEventModifier;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testModifier() throws Exception {
        check(java.nio.file.WatchEvent.Modifier.class.isInterface(), "is interface");
        java.lang.reflect.Method m = java.nio.file.WatchEvent.Modifier.class.getMethod("name");
        check(m.getReturnType() == String.class, "name() returns String");
    }

    // ---- appended: the interface declares exactly one method ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchEvent$Modifier");
        java.lang.reflect.Method[] declared = c.getDeclaredMethods();
        check(declared.length == 1, "exactly one declared method (marker with a name)");
        check("name".equals(declared[0].getName()), "the method is name()");
        check(java.lang.reflect.Modifier.isAbstract(declared[0].getModifiers()),
              "name() is abstract");
        check(c.getTypeParameters().length == 0, "not generic");
        check(c.getInterfaces().length == 0, "no parent interfaces");
    }

    // ---- appended: custom and JDK-provided modifiers ----
    static void testImplementations() throws Exception {
        Modifier custom = new Modifier() {
            public String name() { return "JCK_MODIFIER"; } };
        check("JCK_MODIFIER".equals(custom.name()), "custom modifier name");
        check(!custom.equals(null), "equals(null) false");

        // the JDK ships sensitivity modifiers (com.sun.nio.file)
        Modifier high = SensitivityWatchEventModifier.HIGH;
        check("HIGH".equals(high.name()), "HIGH sensitivity name");
        check(high instanceof Modifier, "sensitivity enum implements Modifier");
        SensitivityWatchEventModifier[] sensitivities =
                SensitivityWatchEventModifier.values();
        check(sensitivities.length == 3, "three sensitivity levels (measured)");
        StringBuilder names = new StringBuilder();
        for (SensitivityWatchEventModifier s : sensitivities) names.append(s.name()).append(" ");
        check("HIGH MEDIUM LOW ".equals(names.toString()), "level names (measured)");
        check(SensitivityWatchEventModifier.HIGH
                != SensitivityWatchEventModifier.LOW, "distinct levels");
    }

    // ---- appended: modifiers flow through registration (measured) ----
    static void testRegistrationBehavior() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            // empty modifier array is always fine
            WatchKey plain = dir.register(ws,
                    new WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE },
                    new Modifier[0]);
            check(plain != null && plain.isValid(), "empty modifier array accepted");
            plain.cancel();

            // a real modifier takes effect and events still flow
            WatchKey key = dir.register(ws,
                    new WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE },
                    new Modifier[] { SensitivityWatchEventModifier.HIGH });
            check(key != null && key.isValid(), "HIGH-modified registration accepted");
            Files.write(dir.resolve("m.txt"), "m".getBytes("UTF-8"));
            WatchKey fired = ws.poll(5, TimeUnit.SECONDS);
            check(fired == key, "modified key fires");
            boolean delivered = false;
            for (WatchEvent<?> e : fired.pollEvents())
                if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE
                        && "m.txt".equals(e.context().toString()))
                    delivered = true;
            check(delivered, "event delivered with modifier present");
            fired.reset();

            // unknown modifier is rejected with the exact exception and message
            Modifier bogus = new Modifier() {
                public String name() { return "JCK_BOGUS_MOD"; } };
            boolean uoe = false;
            String msg = null;
            try {
                dir.register(ws,
                        new WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE },
                        new Modifier[] { bogus });
            } catch (UnsupportedOperationException e) { uoe = true; msg = e.getMessage(); }
            check(uoe, "unknown modifier -> UnsupportedOperationException");
            check("Modifier not supported".equals(msg),
                  "message is 'Modifier not supported' (measured)");
        } finally {
            try { Files.deleteIfExists(dir.resolve("m.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testModifier();
        testShape();
        testImplementations();
        testRegistrationBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
