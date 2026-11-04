/*
 * @test
 * @summary java.nio.file.SimpleFileVisitor conformance: white-box structure + black-box behavior (JDK-8).
 * @run main SimpleFileVisitorTest
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleFileVisitorTest {
    // the default constructor is protected: subclass to reach it
    static final class MyVisitor extends SimpleFileVisitor<Path> { }
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure via reflection ----
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

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        SimpleFileVisitor<Path> v = new MyVisitor();

        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("d.txt");
            Files.write(f, "d".getBytes(StandardCharsets.UTF_8));
            BasicFileAttributes attrs = Files.readAttributes(f,
                    java.nio.file.attribute.BasicFileAttributes.class);
            check(v.preVisitDirectory(dir, attrs) == FileVisitResult.CONTINUE,
                  "preVisitDirectory -> CONTINUE");
            check(v.visitFile(f, attrs) == FileVisitResult.CONTINUE,
                  "visitFile -> CONTINUE");
            check(v.postVisitDirectory(dir, null) == FileVisitResult.CONTINUE,
                  "postVisitDirectory(null error) -> CONTINUE");

            IOException ioe = new IOException("boom");
            boolean rethrown = false;
            try { v.visitFileFailed(f, ioe); }
            catch (IOException e) { rethrown = e == ioe; }
            check(rethrown, "visitFileFailed rethrows the same IOException");

            AtomicInteger files = new AtomicInteger();
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path p, BasicFileAttributes a)
                        throws IOException {
                    files.incrementAndGet();
                    return super.visitFile(p, a);
                }
            });
            check(files.get() == 1, "walk covers the single file");

            Path lockedDir = dir.resolve("locked");
            Files.createDirectory(lockedDir);
            Files.write(lockedDir.resolve("s.txt"), "s".getBytes(StandardCharsets.UTF_8));
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
            try (Stream<Path> s = Files.walk(dir)) {
                for (Path p : s.sorted(java.util.Comparator.reverseOrder())
                        .collect(Collectors.toList())) {
                    try { Files.deleteIfExists(p); } catch (Exception ignore) { }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
