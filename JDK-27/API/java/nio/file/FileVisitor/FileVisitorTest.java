/*
 * @test
 * @requires os.family == "linux"
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

    // ===== deepened contract coverage (batch A9) =====
    static void testSignatures() throws Exception {
        Class<?> c = Class.forName("java.nio.file.FileVisitor");
        check(c.getTypeParameters()[0].getName().equals("T"), "type parameter named T");
        check(!c.isAnnotationPresent(FunctionalInterface.class),
              "not @FunctionalInterface (four abstract methods)");
        check(c.getMethods().length == 4, "exactly four methods, no defaults");
        java.lang.reflect.Method pre = null, visit = null, fail = null, post = null;
        for (java.lang.reflect.Method m : c.getMethods()) {
            switch (m.getName()) {
                case "preVisitDirectory": pre = m; break;
                case "visitFile": visit = m; break;
                case "visitFileFailed": fail = m; break;
                case "postVisitDirectory": post = m; break;
            }
        }
        check(pre != null && pre.getParameterCount() == 2
              && pre.getParameterTypes()[1] == BasicFileAttributes.class
              && pre.getReturnType() == FileVisitResult.class,
              "preVisitDirectory(T, BasicFileAttributes) -> FileVisitResult");
        check(visit != null && visit.getParameterCount() == 2
              && visit.getParameterTypes()[1] == BasicFileAttributes.class
              && visit.getReturnType() == FileVisitResult.class,
              "visitFile(T, BasicFileAttributes) -> FileVisitResult");
        check(fail != null && fail.getParameterCount() == 2
              && fail.getParameterTypes()[1] == IOException.class
              && fail.getReturnType() == FileVisitResult.class,
              "visitFileFailed(T, IOException) -> FileVisitResult");
        check(post != null && post.getParameterCount() == 2
              && post.getParameterTypes()[1] == IOException.class
              && post.getReturnType() == FileVisitResult.class,
              "postVisitDirectory(T, IOException) -> FileVisitResult");
        boolean allThrow = true;
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getExceptionTypes().length == 0) allThrow = false;
        check(allThrow, "every method declares throws IOException");
    }

    static void testDeepened() throws Exception {
        Path dir = Files.createTempDirectory("jck-fvt");
        try {
            Path sub = dir.resolve("sub");
            Files.createDirectory(sub);
            Files.writeString(dir.resolve("f1.txt"), "1");
            Files.writeString(sub.resolve("f2.txt"), "2");

            // exact lifecycle table with attribute objects and null postVisitDirectory error
            final List<String> table = new ArrayList<>();
            final List<String> attrKinds = new ArrayList<>();
            final List<Boolean> postErrNull = new ArrayList<>();
            Files.walkFileTree(dir, new FileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    check(a != null, "preVisitDirectory attributes non-null");
                    attrKinds.add(a.isDirectory() ? "dir" : "other");
                    table.add("pre:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    check(a != null, "visitFile attributes non-null");
                    attrKinds.add(a.isRegularFile() ? "file" : "other");
                    table.add("file:" + f.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFileFailed(Path f, IOException e) {
                    table.add("fail:" + f.getFileName());
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult postVisitDirectory(Path d, IOException e) {
                    postErrNull.add(e == null);
                    table.add("post:" + d.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });
            check(table.size() == 6, "lifecycle table has six entries");
            int subPre = table.indexOf("pre:sub");
            int f2 = table.indexOf("file:f2.txt");
            int subPost = table.indexOf("post:sub");
            check(subPre >= 0 && f2 >= 0 && subPost >= 0 && subPre < f2 && f2 < subPost,
                  "nested ordering pre:sub < file:f2 < post:sub");
            check(!postErrNull.contains(Boolean.FALSE),
                  "postVisitDirectory receives null error on the normal path");
            check(!attrKinds.contains("other"),
                  "attribute kinds match entry kinds (dir/file)");

            // nonexistent start: no preVisitDirectory; visitFileFailed(start, NoSuchFileException)
            final Path ghost = dir.resolve("ghost");
            final List<String[]> failInfo = new ArrayList<>();
            Files.walkFileTree(ghost, new FileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    failInfo.add(new String[] {"pre", d.toString() });
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    failInfo.add(new String[] {"file", f.toString() });
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFileFailed(Path f, IOException e) {
                    failInfo.add(new String[] {e.getClass().getName(), f.toString() });
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult postVisitDirectory(Path d, IOException e) {
                    failInfo.add(new String[] {"post", d.toString() });
                    return FileVisitResult.CONTINUE;
                }
            });
            check(failInfo.size() == 1, "single visitFileFailed callback for missing start");
            check("java.nio.file.NoSuchFileException".equals(failInfo.get(0)[0])
                  && ghost.toString().equals(failInfo.get(0)[1]),
                  "visitFileFailed gets NoSuchFileException and the start path");

            // no failure: full walk when visitFileFailed never fires
            final java.util.concurrent.atomic.AtomicInteger afterFail =
                    new java.util.concurrent.atomic.AtomicInteger();
            Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<>() {
                public FileVisitResult visitFileFailed(Path f, IOException e) {
                    return FileVisitResult.TERMINATE;
                }
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    afterFail.incrementAndGet();
                    return FileVisitResult.CONTINUE;
                }
            });
            check(afterFail.get() == 2, "walk continues normally when no failure occurs");

            // visitor returning null from visitFile -> NullPointerException (probe-confirmed)
            boolean npe = false;
            try {
                Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<>() {
                    public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                        return null;
                    }
                });
            } catch (NullPointerException e) { npe = true; }
            check(npe, "null FileVisitResult -> NullPointerException");

            // RuntimeException from visitFile propagates unwrapped
            final RuntimeException boom = new IllegalStateException("boom");
            RuntimeException seen = null;
            try {
                Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<>() {
                    public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                        throw boom;
                    }
                });
            } catch (RuntimeException e) { seen = e; }
            check(seen == boom, "RuntimeException propagates unwrapped from visitor");

            // IOException from postVisitDirectory also propagates
            boolean ioProp = false;
            try {
                Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<>() {
                    public FileVisitResult postVisitDirectory(Path d, IOException e)
                            throws IOException {
                        throw new IOException("post");
                    }
                });
            } catch (IOException e) { ioProp = true; }
            check(ioProp, "IOException propagates from postVisitDirectory");
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
        testSignatures();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
