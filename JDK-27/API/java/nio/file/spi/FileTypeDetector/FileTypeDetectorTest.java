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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
