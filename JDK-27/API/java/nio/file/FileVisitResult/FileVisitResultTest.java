/*
 * @test
 * @summary java.nio.file.FileVisitResult conformance: white-box structure + black-box behavior.
 * @run main FileVisitResultTest
 */

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileVisitResultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.FileVisitResult");
        check(c.isEnum(), "FileVisitResult is an enum");
        check(c.getEnumConstants().length == 4, "exactly 4 constants");
        for (String n : new String[] {"CONTINUE", "TERMINATE", "SKIP_SUBTREE", "SKIP_SIBLINGS"})
            check(java.util.Arrays.stream(c.getEnumConstants())
                  .anyMatch(k -> ((Enum<?>) k).name().equals(n)), "constant " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(FileVisitResult.values().length == 4, "values length");
        check(FileVisitResult.valueOf("SKIP_SUBTREE") == FileVisitResult.SKIP_SUBTREE,
              "valueOf");
        check(FileVisitResult.values()[0] == FileVisitResult.CONTINUE, "CONTINUE first");

        Path dir = Files.createTempDirectory("jck");
        try {
            // CONTINUE visits everything
            List<String> names = new ArrayList<>();
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
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

            // TERMINATE stops immediately
            Files.writeString(dir.resolve("a.txt"), "a");
            Files.writeString(dir.resolve("b.txt"), "b");
            java.util.concurrent.atomic.AtomicInteger visits =
                    new java.util.concurrent.atomic.AtomicInteger();
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    visits.incrementAndGet();
                    return FileVisitResult.TERMINATE;
                }
            });
            check(visits.get() == 1, "TERMINATE stops after the first file");

            // SKIP_SIBLINGS skips remaining files in the directory
            java.util.concurrent.atomic.AtomicInteger seen =
                    new java.util.concurrent.atomic.AtomicInteger();
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    seen.incrementAndGet();
                    return FileVisitResult.SKIP_SIBLINGS;
                }
            });
            check(seen.get() == 1, "SKIP_SIBLINGS stops the sibling iteration");

            // SKIP_SUBTREE prevents descending into subdirectories
            Path sub = dir.resolve("sub");
            Files.createDirectories(sub);
            Files.writeString(sub.resolve("deep.txt"), "d");
            java.util.concurrent.atomic.AtomicBoolean enteredSub =
                    new java.util.concurrent.atomic.AtomicBoolean(false);
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
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
            Files.deleteIfExists(dir.resolve("sub/deep.txt".replace('/', java.io.File.separatorChar)));
            Files.deleteIfExists(dir.resolve("sub"));
            Files.deleteIfExists(dir.resolve("a.txt"));
            Files.deleteIfExists(dir.resolve("b.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
