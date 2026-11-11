/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.FileVisitOption conformance: white-box structure + black-box behavior
 *          (enum contract, FOLLOW_LINKS semantics incl. dir-symlink following, loop detection).
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
import java.util.List;

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
        check(c.getDeclaringClass() == null, "top-level type");
        check(c.getEnumConstants().length == 1, "exactly 1 constant");
        check(((Enum<?>) c.getEnumConstants()[0]).name().equals("FOLLOW_LINKS"),
              "constant named FOLLOW_LINKS");
        check(!java.nio.file.OpenOption.class.isAssignableFrom(c)
              && !java.nio.file.CopyOption.class.isAssignableFrom(c),
              "no marker interfaces (standalone enum)");
        check(Enum.class.isAssignableFrom(c), "extends java.lang.Enum");
        check(c.getFields().length == 1, "single public constant field");
    }

    // ---- Part 2: enum contract ----
    static void testEnumContract() {
        FileVisitOption[] vals = FileVisitOption.values();
        check(vals.length == 1, "values() has exactly 1 element");
        check(vals[0] == FileVisitOption.FOLLOW_LINKS, "values()[0] is FOLLOW_LINKS");
        check(FileVisitOption.values() != vals, "values() returns a fresh array each call");
        vals[0] = null; // scribble on the returned array
        check(FileVisitOption.values()[0] == FileVisitOption.FOLLOW_LINKS,
              "mutating values() array does not affect the enum");
        check(FileVisitOption.FOLLOW_LINKS.name().equals("FOLLOW_LINKS"), "name()");
        check(FileVisitOption.FOLLOW_LINKS.ordinal() == 0, "ordinal() == 0");
        check(FileVisitOption.FOLLOW_LINKS.toString().equals("FOLLOW_LINKS"),
              "toString() == name() for this enum");
        check(FileVisitOption.FOLLOW_LINKS.compareTo(FileVisitOption.FOLLOW_LINKS) == 0,
              "compareTo self == 0");
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

    // ---- Part 3: FOLLOW_LINKS semantics (probe-confirmed) ----
    static void testBehavior() throws Exception {
        Path root = Files.createTempDirectory("jck-fvo");
        try {
            // directory symlink: NOT followed by default, followed with FOLLOW_LINKS
            Path realDir = Files.createDirectory(root.resolve("real"));
            Files.write(realDir.resolve("f.txt"), new byte[] {1});
            Files.createSymbolicLink(root.resolve("slink"), realDir);
            final List<String> noFollow = new ArrayList<>();
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
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
                    Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
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
                        Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {}),
                  "self-loop + FOLLOW_LINKS -> FileSystemLoopException");
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() { });
            check(true, "self-loop without FOLLOW_LINKS completes normally");
            Files.delete(root.resolve("loop"));

            // broken link: visited via visitFile in BOTH modes (lstat fallback), never failed
            Files.createSymbolicLink(root.resolve("dead"), root.resolve("ghost"));
            final List<String> broken = new ArrayList<>();
            Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                    Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
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
            try (java.util.stream.Stream<Path> s = Files.walk(root)) {
                for (Path p : s.sorted(java.util.Comparator.reverseOrder())
                        .collect(java.util.stream.Collectors.toList()))
                    try { Files.deleteIfExists(p); } catch (Exception ignore) { }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testEnumContract();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
