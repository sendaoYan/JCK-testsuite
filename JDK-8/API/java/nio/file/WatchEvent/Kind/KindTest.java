/*
 * @test
 * @summary java.nio.file.WatchEvent.Kind conformance: hierarchy and methods.
 * @run main KindTest
 */

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class KindTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testKind() {
        check(Kind.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : Kind.class.getMethods()) names.add(m.getName());
        String[] required = { "name", "type" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    // ---- appended: method signatures ----
    static void testSignatures() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchEvent$Kind");
        check(c.getTypeParameters().length == 1, "generic in the event type");
        check("T".equals(c.getTypeParameters()[0].getName()), "parameter is <T>");
        Method name = c.getMethod("name");
        check(name.getReturnType() == String.class, "name() returns String");
        Method type = c.getMethod("type");
        check(type.getReturnType() == Class.class, "type() returns Class<?>");
        check(Modifier.isInterface(c.getModifiers()), "interface (recheck)");
    }

    // ---- appended: the four standard kinds ----
    static void testStandardKinds() {
        check("ENTRY_CREATE".equals(StandardWatchEventKinds.ENTRY_CREATE.name()),
              "ENTRY_CREATE name");
        check("ENTRY_DELETE".equals(StandardWatchEventKinds.ENTRY_DELETE.name()),
              "ENTRY_DELETE name");
        check("ENTRY_MODIFY".equals(StandardWatchEventKinds.ENTRY_MODIFY.name()),
              "ENTRY_MODIFY name");
        check("OVERFLOW".equals(StandardWatchEventKinds.OVERFLOW.name()), "OVERFLOW name");
        check(StandardWatchEventKinds.ENTRY_CREATE.type() == Path.class, "create type Path");
        check(StandardWatchEventKinds.ENTRY_DELETE.type() == Path.class, "delete type Path");
        check(StandardWatchEventKinds.ENTRY_MODIFY.type() == Path.class, "modify type Path");
        check(StandardWatchEventKinds.OVERFLOW.type() == Object.class,
              "overflow type Object (measured)");
        check(StandardWatchEventKinds.ENTRY_CREATE.toString().equals("ENTRY_CREATE"),
              "toString is the name");
        check(StandardWatchEventKinds.ENTRY_CREATE == Kind.class
                .cast(StandardWatchEventKinds.ENTRY_CREATE), "singleton identity");
        // a look-alike kind is never equal to the standard constants
        Kind<Path> fake = new Kind<Path>() {
            public String name() { return "ENTRY_CREATE"; }
            public Class<Path> type() { return Path.class; } };
        check(fake.name().equals("ENTRY_CREATE") && fake.type() == Path.class,
              "custom kind honors the contract");
        check(!fake.equals(StandardWatchEventKinds.ENTRY_CREATE),
              "custom kind not equal to the standard singleton");
    }

    // ---- appended: registration rejects nonstandard kinds (measured) ----
    static void testRegistrationRejection() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            Kind<Path> bogus = new Kind<Path>() {
                public String name() { return "JCK_BOGUS_KIND"; }
                public Class<Path> type() { return Path.class; } };
            boolean uoe = false;
            String msg = null;
            try { dir.register(ws, bogus); }
            catch (UnsupportedOperationException e) { uoe = true; msg = e.getMessage(); }
            check(uoe, "nonstandard kind -> UnsupportedOperationException");
            check(msg != null && msg.contains("JCK_BOGUS_KIND"),
                  "message carries the kind name");

            // standard kinds register fine, and live events report kind().name()
            WatchKey key = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
            Files.write(dir.resolve("k.txt"), "k".getBytes("UTF-8"));
            WatchKey fired = ws.poll(5, TimeUnit.SECONDS);
            check(fired == key, "standard kind registration fires");
            boolean nameMatches = false;
            for (WatchEvent<?> e : fired.pollEvents())
                if ("ENTRY_CREATE".equals(e.kind().name())) nameMatches = true;
            check(nameMatches, "live event kind().name() is ENTRY_CREATE");
            fired.reset();
        } finally {
            try { Files.deleteIfExists(dir.resolve("k.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testKind();
        testSignatures();
        testStandardKinds();
        testRegistrationRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
