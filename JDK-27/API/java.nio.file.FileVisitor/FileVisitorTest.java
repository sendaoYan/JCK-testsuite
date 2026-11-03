/*
 * @test
 * @summary java.nio.file.FileVisitor conformance: white-box structure + black-box behavior.
 * @run main FileVisitorTest
 */

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileVisitorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.FileVisitor");
        check(c.isInterface(), "FileVisitor is an interface");
        check(c.getTypeParameters().length == 1, "generic in path type");
        for (String n : new String[] {"preVisitDirectory", "visitFile", "visitFileFailed",
                "postVisitDirectory"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path sub = dir.resolve("sub");
            Files.createDirectory(sub);
            Files.writeString(dir.resolve("f1.txt"), "1");
            Files.writeString(sub.resolve("f2.txt"), "2");

            // full visitor lifecycle: pre(dir) -> f1 -> pre(sub) -> f2 -> post(sub) -> post(dir)
            List<String> trace = new ArrayList<>();
            Files.walkFileTree(dir, new FileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    trace.add("pre:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    trace.add("file:" + f.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFileFailed(Path f, IOException e) {
                    trace.add("fail:" + f.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult postVisitDirectory(Path d, IOException e) {
                    trace.add("post:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });
            check(trace.size() == 6, "six callbacks for a two-level tree, got " + trace);
            check(trace.get(0).startsWith("pre:"), "starts with preVisitDirectory");
            check(trace.get(trace.size() - 1).startsWith("post:"), "ends with postVisitDirectory");
            check(trace.stream().filter(t -> t.startsWith("file:")).count() == 2,
                  "both files visited");
            int subPre = trace.indexOf(trace.stream()
                    .filter(t -> t.startsWith("pre:sub")).findFirst().orElse(""));
            int subPost = trace.indexOf(trace.stream()
                    .filter(t -> t.startsWith("post:sub")).findFirst().orElse(""));
            check(subPre < subPost, "sub pre precedes sub post");

            // visitFileFailed fires for unopenable entries (missing read permission)
            if (!System.getProperty("user.name").equals("root")) {
                Path lockedDir = dir.resolve("locked");
                Files.createDirectory(lockedDir);
                Path secret = lockedDir.resolve("secret.txt");
                Files.writeString(secret, "s");
                Files.setPosixFilePermissions(lockedDir,
                        java.nio.file.attribute.PosixFilePermissions.fromString("--x------"));
                try {
                    java.util.concurrent.atomic.AtomicInteger failures =
                            new java.util.concurrent.atomic.AtomicInteger();
                    Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<>() {
                        public FileVisitResult visitFileFailed(Path f, IOException e) {
                            failures.incrementAndGet();
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    check(failures.get() >= 1, "unreadable directory reports failure");
                } finally {
                    Files.setPosixFilePermissions(lockedDir,
                            java.nio.file.attribute.PosixFilePermissions.fromString("rwx------"));
                }
            }

            // an exception from the visitor propagates out of walkFileTree
            boolean propagated = false;
            try {
                Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<>() {
                    public FileVisitResult visitFile(Path f, BasicFileAttributes a)
                            throws IOException {
                        throw new IOException("stop");
                    }
                });
            } catch (IOException e) { propagated = true; }
            check(propagated, "visitor exceptions propagate");
        } finally {
            try { Files.setPosixFilePermissions(dir.resolve("locked"),
                    java.nio.file.attribute.PosixFilePermissions.fromString("rwx------")); }
            catch (Exception ignore) { }
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
