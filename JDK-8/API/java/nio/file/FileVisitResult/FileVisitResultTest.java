/*
 * @test
 * @summary java.nio.file.FileVisitResult conformance: white-box structure + black-box behavior (JDK-8).
 * @run main FileVisitResultTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FileVisitResultTest {
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

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.FileVisitResult");
        check(c.isEnum(), "FileVisitResult is an enum");
        check(c.getEnumConstants().length == 4, "exactly 4 constants");
        for (String n : new String[] {"CONTINUE", "TERMINATE", "SKIP_SUBTREE", "SKIP_SIBLINGS"})
            check(java.util.Arrays.stream(c.getEnumConstants())
                  .anyMatch(k -> ((Enum<?>) k).name().equals(n)), "constant " + n);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        check(FileVisitResult.values().length == 4, "values length");
        check(FileVisitResult.valueOf("SKIP_SUBTREE") == FileVisitResult.SKIP_SUBTREE,
              "valueOf");
        check(FileVisitResult.values()[0] == FileVisitResult.CONTINUE, "CONTINUE first");

        Path dir = Files.createTempDirectory("jck");
        try {
            List<String> names = new ArrayList<>();
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    names.add("pre:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    names.add("file:" + f.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });
            check(names.size() == 1 && names.get(0).startsWith("pre:"),
                  "empty tree visited with CONTINUE");

            Files.write(dir.resolve("a.txt"), "a".getBytes(StandardCharsets.UTF_8));
            Files.write(dir.resolve("b.txt"), "b".getBytes(StandardCharsets.UTF_8));
            AtomicInteger visits = new AtomicInteger();
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    visits.incrementAndGet();
                    return FileVisitResult.TERMINATE;
                }
            });
            check(visits.get() == 1, "TERMINATE stops after the first file");

            AtomicInteger seen = new AtomicInteger();
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    seen.incrementAndGet();
                    return FileVisitResult.SKIP_SIBLINGS;
                }
            });
            check(seen.get() == 1, "SKIP_SIBLINGS stops the sibling iteration");

            Path sub = dir.resolve("sub");
            Files.createDirectories(sub);
            Files.write(sub.resolve("deep.txt"), "d".getBytes(StandardCharsets.UTF_8));
            AtomicBoolean enteredSub = new AtomicBoolean(false);
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    if (d.getFileName() != null && d.getFileName().toString().equals("sub"))
                        return FileVisitResult.SKIP_SUBTREE;
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    if (f.getFileName().toString().equals("deep.txt"))
                        enteredSub.set(true);
                    return FileVisitResult.CONTINUE;
                }
            });
            check(!enteredSub.get(), "SKIP_SUBTREE skips descending");
        } finally {
            Files.deleteIfExists(dir.resolve("sub").resolve("deep.txt"));
            Files.deleteIfExists(dir.resolve("sub"));
            Files.deleteIfExists(dir.resolve("a.txt"));
            Files.deleteIfExists(dir.resolve("b.txt"));
            Files.deleteIfExists(dir);
        }
    }

    // ===== deepened contract coverage (batch A9): probe-confirmed =====
    static void testEnumContract() {
        FileVisitResult[] vals = FileVisitResult.values();
        check(FileVisitResult.values() != vals, "values() returns a fresh array each call");
        // declaration order: CONTINUE, TERMINATE, SKIP_SUBTREE, SKIP_SIBLINGS
        check(vals[0].ordinal() == 0 && vals[0] == FileVisitResult.CONTINUE, "CONTINUE ordinal 0");
        check(vals[1].ordinal() == 1 && vals[1] == FileVisitResult.TERMINATE, "TERMINATE ordinal 1");
        check(vals[2].ordinal() == 2 && vals[2] == FileVisitResult.SKIP_SUBTREE, "SKIP_SUBTREE ordinal 2");
        check(vals[3].ordinal() == 3 && vals[3] == FileVisitResult.SKIP_SIBLINGS, "SKIP_SIBLINGS ordinal 3");
        vals[0] = null;
        check(FileVisitResult.values()[0] == FileVisitResult.CONTINUE,
              "mutating values() array does not affect the enum");
        boolean allNames = true;
        for (FileVisitResult r : FileVisitResult.values())
            if (!r.toString().equals(r.name())) allNames = false;
        check(allNames, "toString() == name() for every constant");
        check(FileVisitResult.CONTINUE.compareTo(FileVisitResult.TERMINATE) < 0
              && FileVisitResult.TERMINATE.compareTo(FileVisitResult.SKIP_SUBTREE) < 0
              && FileVisitResult.SKIP_SUBTREE.compareTo(FileVisitResult.SKIP_SIBLINGS) < 0,
              "compareTo follows declaration order");
        boolean roundTrip = true;
        for (FileVisitResult r : FileVisitResult.values())
            if (FileVisitResult.valueOf(r.name()) != r) roundTrip = false;
        check(roundTrip, "valueOf(name) round-trip identity for all constants");
        checkThrows(IllegalArgumentException.class,
              () -> FileVisitResult.valueOf("continue"),
              "valueOf(lowercase) -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class,
              () -> FileVisitResult.valueOf("NOPE"),
              "valueOf(unknown) -> IllegalArgumentException");
        checkThrows(NullPointerException.class, () -> FileVisitResult.valueOf(null),
              "valueOf(null) -> NullPointerException");
        check(FileVisitResult.CONTINUE.getDeclaringClass() == FileVisitResult.class,
              "getDeclaringClass is FileVisitResult");
        java.util.EnumSet<FileVisitResult> two =
              java.util.EnumSet.of(FileVisitResult.TERMINATE, FileVisitResult.SKIP_SUBTREE);
        check(two.size() == 2 && two.contains(FileVisitResult.TERMINATE)
              && two.contains(FileVisitResult.SKIP_SUBTREE), "EnumSet.of pair");
        check(java.util.EnumSet.allOf(FileVisitResult.class).size() == 4,
              "EnumSet.allOf mirrors values()");
        check(java.util.EnumSet.range(FileVisitResult.CONTINUE, FileVisitResult.SKIP_SUBTREE).size() == 3,
              "EnumSet.range follows ordinals");
    }

    static void testDeepened() throws Exception {
        Path dir = Files.createTempDirectory("jck-fvr");
        try {
            Files.write(dir.resolve("a.txt"), new byte[] {1});
            Files.write(dir.resolve("b.txt"), new byte[] {2});
            final List<String> ev = new ArrayList<>();

            // SKIP_SIBLINGS from preVisitDirectory: no entries, no postVisitDirectory
            ev.clear();
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    ev.add("pre:" + d.getFileName());
                    return FileVisitResult.SKIP_SIBLINGS;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    ev.add("f:" + f.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult postVisitDirectory(Path d, java.io.IOException e) {
                    ev.add("post:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });
            check(ev.size() == 1 && ev.get(0).startsWith("pre:"),
                  "SKIP_SIBLINGS from preVisitDirectory: [pre] only (probe-confirmed)");

            // SKIP_SUBTREE from visitFile behaves as CONTINUE
            ev.clear();
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    ev.add("f:" + f.getFileName());
                    return FileVisitResult.SKIP_SUBTREE;
                }
                public FileVisitResult postVisitDirectory(Path d, java.io.IOException e) {
                    ev.add("post:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });
            check(ev.size() == 3 && ev.get(0).startsWith("f:") && ev.get(1).startsWith("f:")
                  && ev.get(2).startsWith("post:"),
                  "SKIP_SUBTREE from visitFile treated as CONTINUE (probe-confirmed)");

            // TERMINATE from preVisitDirectory ends immediately
            ev.clear();
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    ev.add("pre:" + d.getFileName());
                    return FileVisitResult.TERMINATE;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    ev.add("f:" + f.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });
            check(ev.size() == 1 && ev.get(0).startsWith("pre:"),
                  "TERMINATE from preVisitDirectory stops before any entry");

            // SKIP_SIBLINGS from visitFile on a flat dir: [pre, file, post]
            ev.clear();
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    ev.add("pre:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    ev.add("f:" + f.getFileName());
                    return FileVisitResult.SKIP_SIBLINGS;
                }
                public FileVisitResult postVisitDirectory(Path d, java.io.IOException e) {
                    ev.add("post:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });
            check(ev.size() == 3 && ev.get(0).startsWith("pre:") && ev.get(1).startsWith("f:")
                  && ev.get(2).startsWith("post:"),
                  "SKIP_SIBLINGS from visitFile: [pre, file, post] (probe-confirmed)");

            // TERMINATE from visitFile: [pre, file] with no post
            ev.clear();
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    ev.add("pre:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    ev.add("f:" + f.getFileName());
                    return FileVisitResult.TERMINATE;
                }
                public FileVisitResult postVisitDirectory(Path d, java.io.IOException e) {
                    ev.add("post:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });
            check(ev.size() == 2 && ev.get(0).startsWith("pre:") && ev.get(1).startsWith("f:"),
                  "TERMINATE from visitFile: [pre, file], no post (probe-confirmed)");
        } finally {
            try (java.util.stream.Stream<Path> s = Files.walk(dir)) {
                for (Path p : s.sorted(java.util.Comparator.reverseOrder())
                        .collect(java.util.stream.Collectors.toList()))
                    try { Files.deleteIfExists(p); } catch (Exception ignore) { }
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
