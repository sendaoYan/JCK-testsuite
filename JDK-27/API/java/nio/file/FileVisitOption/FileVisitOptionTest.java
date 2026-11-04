/*
 * @test
 * @summary java.nio.file.FileVisitOption conformance: white-box structure + black-box behavior.
 * @run main FileVisitOptionTest
 */

import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class FileVisitOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
