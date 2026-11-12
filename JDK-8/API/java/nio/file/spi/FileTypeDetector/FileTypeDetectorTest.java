/*
 * @test
 * @summary java.nio.file.spi.FileTypeDetector conformance: hierarchy and methods.
 * @run main FileTypeDetectorTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FileTypeDetectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Fixed-answer detector exercising the protected constructor contract. */
    static final class FixedDetector extends FileTypeDetector {
        int calls;
        public String probeContentType(Path path) {
            calls++;
            return "application/jck";
        }
    }

    static void testFileTypeDetector() {
        check(Modifier.isAbstract(FileTypeDetector.class.getModifiers()), "is abstract");

        Set<String> names = new HashSet<String>();
        for (Method m : FileTypeDetector.class.getMethods()) names.add(m.getName());
        String[] required = { "probeContentType" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    // ---- appended: abstract shape ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("java.nio.file.spi.FileTypeDetector");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface(), "class, not interface");
        check(c.getSuperclass() == Object.class, "extends Object directly");
        // JDK-8 declares an extra private (Void) security-gate constructor; the
        // public contract is the protected no-arg constructor for subclasses
        boolean hasProtected = false;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors())
            if (Modifier.isProtected(ct.getModifiers())) hasProtected = true;
        check(hasProtected, "protected constructor for subclasses");
        check(java.util.Arrays.stream(c.getDeclaredConstructors())
                .anyMatch(ct -> ct.getParameterTypes().length == 0
                      && Modifier.isProtected(ct.getModifiers())),
              "protected no-arg constructor");
        Method probe = c.getMethod("probeContentType", Path.class);
        check(probe.getReturnType() == String.class, "probeContentType returns String");
        check(Modifier.isAbstract(probe.getModifiers()), "probeContentType is abstract");
        check(Modifier.isPublic(probe.getModifiers()), "probeContentType is public");
    }

    // ---- appended: subclass contract ----
    static void testSubclassContract() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("anything.bin");
            Files.write(f, "x".getBytes(StandardCharsets.UTF_8));

            // fixed answer, call counting, repeated calls
            FixedDetector d = new FixedDetector();
            check("application/jck".equals(d.probeContentType(f)), "custom answer returned");
            check(d.calls == 1, "exactly one probe call");
            d.probeContentType(f);
            d.probeContentType(dir.resolve("ghost"));
            check(d.calls == 3, "each call counted, missing files included");

            // null is a legal answer (content type unknown)
            FileTypeDetector unknown = new FileTypeDetector() {
                public String probeContentType(Path path) { return null; } };
            check(unknown.probeContentType(f) == null, "null means unknown");

            // IOException from a detector propagates verbatim to its caller
            final java.io.IOException boom = new java.io.IOException("jck-probe-failure");
            FileTypeDetector failing = new FileTypeDetector() {
                public String probeContentType(Path path) throws java.io.IOException {
                    throw boom;
                } };
            boolean propagated = false;
            try { failing.probeContentType(f); }
            catch (java.io.IOException e) { propagated = (e == boom); }
            check(propagated, "detector IOException propagates as the same object");

            // null-path handling is subclass-defined (the interface imposes no
            // check), so a trivial detector simply answers for null as well
            FixedDetector counting = new FixedDetector();
            check("application/jck".equals(counting.probeContentType(null)),
                  "null-path handling is subclass-defined (no platform check)");
            check(counting.calls == 1, "body entered once for null path");
        } finally {
            try { Files.deleteIfExists(dir.resolve("anything.bin")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    // ---- appended: Files.probeContentType delegation (platform MIME table) ----
    static void testPlatformDelegation() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            // The MIME answers come from the platform's detector and differ across
            // OSes. Lock each actual result and assert repeat calls are deterministic
            // (and never throw), rather than hardcoding Linux MIME strings.
            String[] exts = {"x.txt", "x.pdf", "x.html", "x.png"};
            for (String ext : exts) {
                String first = Files.probeContentType(dir.resolve(ext));
                String second = Files.probeContentType(dir.resolve(ext));
                check(first == null ? second == null : first.equals(second),
                      "probeContentType " + ext + " deterministic");
            }
            // unknown and absent extensions yield null on every platform detector
            check(Files.probeContentType(dir.resolve("x.jcknoext")) == null,
                  "made-up extension -> null");
            check(Files.probeContentType(dir.resolve("noext")) == null,
                  "no extension -> null");

            // the file need not exist: lookup is name-based and never throws here
            String ghost = Files.probeContentType(dir.resolve("ghost.txt"));
            check(ghost == null || Files.probeContentType(dir.resolve("ghost.txt")).equals(ghost),
                  "missing file probe is deterministic and never throws");

            // content does not override a known extension: a .txt file with PDF
            // content returns the same value as any other .txt file.
            Path disguised = dir.resolve("y.txt");
            Files.write(disguised, "%PDF-1.4 junk".getBytes(StandardCharsets.UTF_8));
            String plain = Files.probeContentType(dir.resolve("x.txt"));
            check(plain == null || plain.equals(Files.probeContentType(disguised)),
                  "extension wins over content (deterministic)");

            // our own detector is NOT in the default delegation chain (no ServiceLoader
            // registration), so its answers never leak into Files.probeContentType
            check(Files.probeContentType(dir.resolve("x.jckcustom")) == null,
                  "unregistered detector is never consulted");

            // a directory: result is host-dependent (null or inode/directory)
            String dirType = Files.probeContentType(dir);
            check(dirType == null || "inode/directory".equals(dirType),
                  "directory probe yields null or inode/directory (host-dependent)");
        } finally {
            try { Files.deleteIfExists(dir.resolve("y.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testFileTypeDetector();
        testShape();
        testSubclassContract();
        testPlatformDelegation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
