/*
 * @test
 * @summary java.nio.file.FileVisitor conformance: white-box structure + black-box behavior (JDK-8).
 * @run main FileVisitorTest
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileVisitorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.FileVisitor");
        check(c.isInterface(), "FileVisitor is an interface");
        check(c.getTypeParameters().length == 1, "generic in path type");
        for (String n : new String[] {"preVisitDirectory", "visitFile", "visitFileFailed",
                "postVisitDirectory"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path sub = dir.resolve("sub");
            Files.createDirectory(sub);
            Files.write(dir.resolve("f1.txt"), "1".getBytes(StandardCharsets.UTF_8));
            Files.write(sub.resolve("f2.txt"), "2".getBytes(StandardCharsets.UTF_8));

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
            long fileCount = 0;
            for (String t : trace) if (t.startsWith("file:")) fileCount++;
            check(fileCount == 2, "both files visited");
            int subPre = trace.indexOf("pre:sub");
            int subPost = trace.indexOf("post:sub");
            check(subPre >= 0 && subPost >= 0 && subPre < subPost, "sub pre precedes sub post");

            if (!System.getProperty("user.name").equals("root")) {
                Path lockedDir = dir.resolve("locked");
                Files.createDirectory(lockedDir);
                Path secret = lockedDir.resolve("secret.txt");
                Files.write(secret, "s".getBytes(StandardCharsets.UTF_8));
                Files.setPosixFilePermissions(lockedDir,
                        java.nio.file.attribute.PosixFilePermissions.fromString("--x------"));
                try {
                    AtomicInteger failures = new AtomicInteger();
                    Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<Path>() {
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

            boolean propagated = false;
            try {
                Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<Path>() {
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
