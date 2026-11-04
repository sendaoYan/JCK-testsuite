/*
 * @test
 * @summary java.nio.file.SimpleFileVisitor conformance: white-box structure + black-box behavior.
 * @run main SimpleFileVisitorTest
 */

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class SimpleFileVisitorTest {
    // the default constructor is protected: subclass to reach it
    static final class MyVisitor extends SimpleFileVisitor<Path> { }
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.SimpleFileVisitor");
        check(!java.lang.reflect.Modifier.isInterface(c.getModifiers())
              && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()),
              "concrete class");
        check(java.nio.file.FileVisitor.class.isAssignableFrom(c), "implements FileVisitor");
        check(c.getTypeParameters().length == 1, "generic in path type");
        check(c.getDeclaredConstructors().length == 1
              && !java.lang.reflect.Modifier.isPublic(
                     c.getDeclaredConstructors()[0].getModifiers()),
              "protected default constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SimpleFileVisitor<Path> v = new MyVisitor();

        // defaults continue everywhere and pass attributes through
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("d.txt");
            Files.writeString(f, "d");
            BasicFileAttributes attrs = Files.readAttributes(f,
                    java.nio.file.attribute.BasicFileAttributes.class);
            check(v.preVisitDirectory(dir, attrs) == FileVisitResult.CONTINUE,
                  "preVisitDirectory -> CONTINUE");
            check(v.visitFile(f, attrs) == FileVisitResult.CONTINUE,
                  "visitFile -> CONTINUE");
            check(v.postVisitDirectory(dir, null) == FileVisitResult.CONTINUE,
                  "postVisitDirectory(null error) -> CONTINUE");

            // visitFileFailed rethrows the failure unchanged
            IOException ioe = new IOException("boom");
            boolean rethrown = false;
            try { v.visitFileFailed(f, ioe); }
            catch (IOException e) { rethrown = e == ioe; }
            check(rethrown, "visitFileFailed rethrows the same IOException");

            // usable directly in walkFileTree to traverse everything
            java.util.concurrent.atomic.AtomicInteger files =
                    new java.util.concurrent.atomic.AtomicInteger();
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path p, BasicFileAttributes a)
                        throws IOException {
                    files.incrementAndGet();
                    return super.visitFile(p, a);
                }
            });
            check(files.get() == 1, "walk covers the single file");

            // a failing attribute read surfaces through the default failure hook
            Path lockedDir = dir.resolve("locked");
            Files.createDirectory(lockedDir);
            Files.writeString(lockedDir.resolve("s.txt"), "s");
            if (!System.getProperty("user.name").equals("root")) {
                Files.setPosixFilePermissions(lockedDir,
                        java.nio.file.attribute.PosixFilePermissions.fromString("--x------"));
                try {
                    boolean failed = false;
                    try {
                        Files.walkFileTree(dir, new MyVisitor());
                    } catch (IOException e) { failed = true; }
                    check(failed, "unreadable entry -> IOException through default hook");
                } finally {
                    Files.setPosixFilePermissions(lockedDir,
                            java.nio.file.attribute.PosixFilePermissions.fromString("rwx------"));
                }
            }
        } finally {
            try (var s = Files.walk(dir)) {
                s.sorted(java.util.Comparator.reverseOrder())
                 .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignore) { } });
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
