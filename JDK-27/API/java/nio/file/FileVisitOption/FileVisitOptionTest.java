/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.FileVisitOption conformance: white-box structure + black-box behavior.
 * @run main FileVisitOptionTest
 */

import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileSystemLoopException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileVisitOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Op { void run() throws Exception; }

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
    static void checkThrows(Class<? extends Throwable> ex, Op r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (t.getClass() != ex)
                throw new AssertionError(msg + ": expected " + ex.getName() + " got " + t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + ex.getName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.FileVisitOption");
        check(c.isEnum(), "FileVisitOption is an enum");
        check(c.getEnumConstants().length == 1, "exactly 1 constant");
        check(((Enum<?>) c.getEnumConstants()[0]).name().equals("FOLLOW_LINKS"), "constant FOLLOW_LINKS");
        check(!java.nio.file.OpenOption.class.isAssignableFrom(c)
              && !java.nio.file.CopyOption.class.isAssignableFrom(c),
              "no marker interfaces (standalone enum)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(FileVisitOption.values().length == 1, "single option");
        check(FileVisitOption.valueOf("FOLLOW_LINKS") == FileVisitOption.FOLLOW_LINKS,
              "valueOf");

        Path dir = Files.createTempDirectory("jck");
        try {
            Path target = dir.resolve("target.txt");
            Files.writeString(target, "t");
            Path link = dir.resolve("link");
            Files.createSymbolicLink(link, target);

            // default walk reports the link with link attributes
            java.util.concurrent.atomic.AtomicBoolean sawLinkAsLink =
                    new java.util.concurrent.atomic.AtomicBoolean(false);
            java.util.concurrent.atomic.AtomicBoolean sawLinkAsFile =
                    new java.util.concurrent.atomic.AtomicBoolean(false);
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                    if (p.equals(link) && a.isSymbolicLink())
                        sawLinkAsLink.set(true);
                    return FileVisitResult.CONTINUE;
                }
            });
            check(sawLinkAsLink.get(), "default walk reports the link as a link");

            // FOLLOW_LINKS reports the link's target attributes instead
            Files.walkFileTree(dir, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                    Integer.MAX_VALUE, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                    if (p.equals(link) && a.isRegularFile())
                        sawLinkAsFile.set(true);
                    return FileVisitResult.CONTINUE;
                }
            });
            check(sawLinkAsFile.get(), "FOLLOW_LINKS walks through to the target");
        } finally {
            Files.deleteIfExists(dir.resolve("link"));
            Files.deleteIfExists(dir.resolve("target.txt"));
            Files.deleteIfExists(dir);
        }
    }

    // ===== deepened contract coverage (batch A9): probe-confirmed =====
    static void testEnumContract() {
        FileVisitOption[] vals = FileVisitOption.values();
        check(vals.length == 1, "values() has exactly 1 element");
        check(vals[0] == FileVisitOption.FOLLOW_LINKS, "values()[0] is FOLLOW_LINKS");
        check(FileVisitOption.values() != vals, "values() returns a fresh array each call");
        vals[0] = null; // scribble on the returned array
        check(FileVisitOption.values()[0] == FileVisitOption.FOLLOW_LINKS,
              "mutating values() array does not affect the enum");
        check(FileVisitOption.FOLLOW_LINKS.ordinal() == 0, "ordinal() == 0");
        check(FileVisitOption.FOLLOW_LINKS.toString().equals("FOLLOW_LINKS"),
              "toString() == name() for this enum");
        check(FileVisitOption.valueOf("FOLLOW_LINKS") == FileVisitOption.FOLLOW_LINKS,
              "valueOf round-trip identity");
        checkThrows(IllegalArgumentException.class,
              () -> FileVisitOption.valueOf("follow_links"),
              "valueOf(lowercase) -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class,
              () -> FileVisitOption.valueOf("NOPE"),
              "valueOf(unknown) -> IllegalArgumentException");
        checkThrows(NullPointerException.class, () -> FileVisitOption.valueOf(null),
              "valueOf(null) -> NullPointerException");
        check(EnumSet.of(FileVisitOption.FOLLOW_LINKS).contains(FileVisitOption.FOLLOW_LINKS),
              "EnumSet.of contains the option");
        check(EnumSet.noneOf(FileVisitOption.class).isEmpty(),
              "EnumSet.noneOf starts empty");
        check(EnumSet.allOf(FileVisitOption.class).size() == 1,
              "EnumSet.allOf mirrors values()");
    }

    static void testDeepened() throws Exception {
        Path root = Files.createTempDirectory("jck-fvo");
        try {
            // dir-symlink is a link by default, followed with FOLLOW_LINKS
            Path realDir = Files.createDirectory(root.resolve("real"));
            Files.writeString(realDir.resolve("f.txt"), "x");
            Files.createSymbolicLink(root.resolve("slink"), realDir);
            final List<String> noFollow = new ArrayList<>();
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    noFollow.add("pre:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    noFollow.add((a.isSymbolicLink() ? "link:" : "file:") + f.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });
            check(noFollow.contains("link:slink") && !noFollow.contains("pre:slink"),
                  "default walk visits dir-symlink as a link, no preVisitDirectory");
            final List<String> follow = new ArrayList<>();
            Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                    Integer.MAX_VALUE, new SimpleFileVisitor<>() {
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    follow.add("pre:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    follow.add((a.isSymbolicLink() ? "link:" : "file:") + f.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });
            check(follow.contains("pre:slink"), "FOLLOW_LINKS follows dir-symlink (preVisitDirectory)");
            check(follow.contains("file:f.txt"),
                  "FOLLOW_LINKS re-visits target contents through the link");
            Files.delete(root.resolve("slink"));

            // self-referential dir symlink + FOLLOW_LINKS -> FileSystemLoopException (exact)
            Files.createSymbolicLink(root.resolve("loop"), root);
            checkThrows(FileSystemLoopException.class, () ->
                  Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                        Integer.MAX_VALUE, new SimpleFileVisitor<>() { }),
                  "self-loop + FOLLOW_LINKS -> FileSystemLoopException");
            Files.walkFileTree(root, new SimpleFileVisitor<>() { });
            check(true, "self-loop without FOLLOW_LINKS completes normally");
            Files.delete(root.resolve("loop"));

            // broken link: handled by visitFile with link attrs even under FOLLOW_LINKS
            Files.createSymbolicLink(root.resolve("dead"), root.resolve("ghost"));
            final List<String> broken = new ArrayList<>();
            Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                    Integer.MAX_VALUE, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    broken.add("visit:" + f.getFileName() + ":sym=" + a.isSymbolicLink());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFileFailed(Path f, java.io.IOException e) {
                    broken.add("failed:" + f.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });
            boolean sawDeadAsLink = false, sawFailed = false;
            for (String e : broken) {
                if (e.equals("visit:dead:sym=true")) sawDeadAsLink = true;
                if (e.startsWith("failed:")) sawFailed = true;
            }
            check(sawDeadAsLink,
                  "broken link + FOLLOW_LINKS still handled by visitFile with link attrs");
            check(!sawFailed, "broken link never routed to visitFileFailed");
        } finally {
            try (var s = Files.walk(root)) {
                s.sorted(java.util.Comparator.reverseOrder())
                 .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignore) { } });
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumContract();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
