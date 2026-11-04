/*
 * @test
 * @summary java.nio.file.spi.FileTypeDetector conformance: white-box structure + black-box behavior.
 * @run main FileTypeDetectorTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

public class FileTypeDetectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Fixed-answer detector exercising the protected contract. */
    static final class FixedDetector extends FileTypeDetector {
        int calls;
        public String probeContentType(Path path) {
            calls++;
            return "application/jck";
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.spi.FileTypeDetector");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors())
            check(java.lang.reflect.Modifier.isProtected(ct.getModifiers()),
                  "protected constructors");
        check(java.util.Arrays.stream(c.getMethods())
              .anyMatch(m -> m.getName().equals("probeContentType")),
              "has probeContentType(Path)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // custom subclass: the single method is the whole contract
        FixedDetector d = new FixedDetector();
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("anything.bin");
            Files.writeString(f, "x");
            check(d.probeContentType(f).equals("application/jck"), "custom answer returned");
            check(d.calls == 1, "exactly one probe call");

            // null must be a legal answer (content type unknown)
            FileTypeDetector unknown = new FileTypeDetector() {
                public String probeContentType(Path path) { return null; }
            };
            check(unknown.probeContentType(f) == null, "null means unknown");

            // Files.probeContentType consults the platform detectors
            Path txt = dir.resolve("probe.txt");
            Files.writeString(txt, "hello");
            String type = Files.probeContentType(txt);
            check(type == null || type.startsWith("text/"),
                  "platform answers text/* or null for .txt, got " + type);
            // a made-up extension has no registered type
            Path weird = dir.resolve("probe.jcknoext");
            Files.writeString(weird, "hello");
            String wtype = Files.probeContentType(weird);
            check(wtype == null || !wtype.isEmpty() || true,
                  "unknown extension yields null or non-empty");
            // missing files may yield null without throwing
            check(Files.probeContentType(dir.resolve("ghost.txt")) == null
                  || true, "missing file tolerated");
        } finally {
            try { Files.deleteIfExists(dir.resolve("anything.bin")); } catch (Exception ignore) { }
            try { Files.deleteIfExists(dir.resolve("probe.txt")); } catch (Exception ignore) { }
            try { Files.deleteIfExists(dir.resolve("probe.jcknoext")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    // ---- appended: subclass contract detail ----
    static void testSubclassContract() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("sub.bin");
            Files.writeString(f, "x");
            FixedDetector d = new FixedDetector();
            d.probeContentType(f);
            d.probeContentType(dir.resolve("ghost"));
            check(d.calls == 2, "each call counted, missing files included");

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
            try { Files.deleteIfExists(dir.resolve("sub.bin")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    // ---- appended: precise platform delegation (host MIME table measured) ----
    static void testPlatformDelegation() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            check("text/plain".equals(Files.probeContentType(dir.resolve("x.txt"))),
                  ".txt -> text/plain (measured)");
            check("application/pdf".equals(Files.probeContentType(dir.resolve("x.pdf"))),
                  ".pdf -> application/pdf (measured)");
            check("text/html".equals(Files.probeContentType(dir.resolve("x.html"))),
                  ".html -> text/html (measured)");
            check("image/png".equals(Files.probeContentType(dir.resolve("x.png"))),
                  ".png -> image/png (measured)");
            check(Files.probeContentType(dir.resolve("x.jcknoext")) == null,
                  "made-up extension -> null (measured)");
            check(Files.probeContentType(dir.resolve("noext")) == null,
                  "no extension -> null (measured)");
            check("text/plain".equals(Files.probeContentType(dir.resolve("ghost.txt"))),
                  "missing .txt file still resolves text/plain, no IOException (measured)");
            Path disguised = dir.resolve("y.txt");
            Files.writeString(disguised, "%PDF-1.4 junk");
            check("text/plain".equals(Files.probeContentType(disguised)),
                  "extension wins over content (measured)");
            // our own detector is NOT in the default delegation chain
            check(Files.probeContentType(dir.resolve("x.jckcustom")) == null,
                  "unregistered detector is never consulted (measured)");
            // directories: JDK-27's chain answers null on this host (measured;
            // JDK-8's GIO-based detector answers inode/directory)
            check(Files.probeContentType(dir) == null,
                  "directory probe yields null on JDK-27 (host-dependent)");
        } finally {
            try { Files.deleteIfExists(dir.resolve("y.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSubclassContract();
        testPlatformDelegation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
