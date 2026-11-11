/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.FileSystemLoopException conformance: white-box structure + black-box behavior.
 * @run main FileSystemLoopExceptionTest
 */

import java.nio.file.FileSystemLoopException;

public class FileSystemLoopExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(FileSystemLoopException.class), "hierarchy: extends FileSystemException");

        FileSystemLoopException e = new FileSystemLoopException("f");
        check("f".equals(e.getFile()), "getFile");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            // dir/link -> dir : following it loops
            java.nio.file.Files.createSymbolicLink(dir.resolve("loop"), dir);
            java.util.List<java.nio.file.Path> visited = new java.util.ArrayList<>();
            boolean looped = false;
            try {
                java.nio.file.Files.walkFileTree(dir,
                        java.util.EnumSet.of(java.nio.file.FileVisitOption.FOLLOW_LINKS),
                        100, new java.nio.file.SimpleFileVisitor<>() {
                    public java.nio.file.FileVisitResult visitFile(java.nio.file.Path p,
                            java.nio.file.attribute.BasicFileAttributes a) {
                        visited.add(p);
                        return java.nio.file.FileVisitResult.CONTINUE;
                    }
                });
            } catch (FileSystemLoopException e) {
                looped = e.getFile() != null;
            }
            check(looped, "symlink cycle walk -> FileSystemLoopException with file");
            // without FOLLOW_LINKS the same tree walks fine
            java.nio.file.Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<>() {
                public java.nio.file.FileVisitResult visitFile(java.nio.file.Path p,
                        java.nio.file.attribute.BasicFileAttributes a) {
                    return java.nio.file.FileVisitResult.CONTINUE;
                }
            });
            check(true, "no-follow walk terminates");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dir.resolve("loop")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir); } catch (Exception ignore) { }
        }
    }


    // ---- batch A11 shared helpers ----
    static void checkThrowsExact(Class<?> expected, Runnable action, String msg) {
        try {
            action.run();
        } catch (Throwable t) {
            if (t.getClass() != expected)
                throw new AssertionError(msg + ": expected " + expected.getName()
                        + " but got " + t.getClass().getName(), t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    interface IOAct { void run() throws java.io.IOException; }

    static void checkThrowsExactIO(Class<?> expected, IOAct action, String msg) {
        try {
            action.run();
        } catch (Throwable t) {
            if (t.getClass() != expected)
                throw new AssertionError(msg + ": expected " + expected.getName()
                        + " but got " + t.getClass().getName(), t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static Object serialRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return ois.readObject();
        }
    }

    static void deleteTree(java.nio.file.Path p) {
        try {
            if (java.nio.file.Files.isDirectory(p)) {
                try (java.nio.file.DirectoryStream<java.nio.file.Path> ds =
                        java.nio.file.Files.newDirectoryStream(p)) {
                    for (java.nio.file.Path c : ds) deleteTree(c);
                }
            }
            java.nio.file.Files.deleteIfExists(p);
        } catch (Exception ignore) { }
    }

    // ==== batch A11: constructor / serialization / walk-cycle trigger ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(FileSystemLoopException.class.getSuperclass()
                == java.nio.file.FileSystemException.class,
                "direct superclass is FileSystemException");
        check(java.io.IOException.class.isAssignableFrom(FileSystemLoopException.class),
                "caught as IOException");
        check(java.io.Serializable.class.isAssignableFrom(FileSystemLoopException.class),
                "Serializable");
        check(FileSystemLoopException.class.getConstructors().length == 1,
                "single public (String) constructor only");
        FileSystemLoopException e = new FileSystemLoopException("f");
        check("f".equals(e.getFile()) && e.getOtherFile() == null && e.getReason() == null,
                "getters: file stored, other/reason null");
        check("f".equals(e.getMessage()), "message == file");
        check("java.nio.file.FileSystemLoopException: f".equals(e.toString()),
                "toString exact");
        FileSystemLoopException n = new FileSystemLoopException(null);
        check(n.getFile() == null && n.getMessage() == null, "null file accepted, null message");
    }

    static void testContractCauseAndSerialization() throws Exception {
        FileSystemLoopException e = new FileSystemLoopException("f");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(FileSystemLoopException.class)
                .getSerialVersionUID() == 4843039591949217617L,
                "serialVersionUID == 4843039591949217617");
        Object back = serialRoundTrip(e);
        check(back instanceof FileSystemLoopException,
                "round-trip preserves subclass identity");
        FileSystemLoopException r = (FileSystemLoopException) back;
        check("f".equals(r.getFile()) && r.getOtherFile() == null && r.getReason() == null,
                "round-trip keeps getters");
        check("f".equals(r.getMessage()), "round-trip message");
    }

    static void testContractRealTrigger() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.Files.write(dir.resolve("seed"), "s".getBytes("UTF-8"));
            java.nio.file.Path loop = dir.resolve("loop");
            java.nio.file.Files.createSymbolicLink(loop, dir);
            // following a link that resolves to an ancestor directory trips cycle detection;
            // the default visitor rethrows it straight out of walkFileTree
            try {
                java.nio.file.Files.walkFileTree(dir,
                        java.util.EnumSet.of(java.nio.file.FileVisitOption.FOLLOW_LINKS),
                        Integer.MAX_VALUE, new java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
                            public java.nio.file.FileVisitResult visitFile(java.nio.file.Path p,
                                    java.nio.file.attribute.BasicFileAttributes a) {
                                return java.nio.file.FileVisitResult.CONTINUE;
                            }
                        });
                check(false, "walking an ancestor cycle must fail");
            } catch (FileSystemLoopException e) {
                check(loop.toString().equals(e.getFile()), "getFile is the cycle link");
                check(e.getOtherFile() == null && e.getReason() == null, "other/reason null");
                check(loop.toString().equals(e.getMessage()), "message == link path");
            }
            // a visitor that swallows failures receives the exact exception and the walk ends
            final java.util.List<String> failed = new java.util.ArrayList<String>();
            java.nio.file.Files.walkFileTree(dir,
                    java.util.EnumSet.of(java.nio.file.FileVisitOption.FOLLOW_LINKS),
                    Integer.MAX_VALUE, new java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
                        public java.nio.file.FileVisitResult visitFile(java.nio.file.Path p,
                                java.nio.file.attribute.BasicFileAttributes a) {
                            return java.nio.file.FileVisitResult.CONTINUE;
                        }
                        public java.nio.file.FileVisitResult visitFileFailed(java.nio.file.Path p,
                                java.io.IOException x) {
                            failed.add(x.getClass().getSimpleName());
                            return java.nio.file.FileVisitResult.CONTINUE;
                        }
                    });
            check(failed.equals(java.util.Arrays.asList("FileSystemLoopException")),
                    "visitFileFailed got exactly one FileSystemLoopException");
            // without FOLLOW_LINKS the same tree walks fine (link is just a file)
            final int[] seedSeen = {0};
            java.nio.file.Files.walkFileTree(dir,
                    new java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
                        public java.nio.file.FileVisitResult visitFile(java.nio.file.Path p,
                                java.nio.file.attribute.BasicFileAttributes a) {
                            if (p.getFileName().toString().equals("seed")) seedSeen[0]++;
                            return java.nio.file.FileVisitResult.CONTINUE;
                        }
                    });
            check(seedSeen[0] == 1, "no-follow walk terminates, seed seen once");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dir.resolve("loop")); } catch (Exception ignore) { }
            deleteTree(dir);
        }
    }

    static void testContractCopyThroughCycle() throws Exception {
        java.nio.file.Path loopDir = java.nio.file.Files.createTempDirectory("a11cycle");
        try {
            java.nio.file.Path b1 = loopDir.resolve("b1");
            java.nio.file.Path b2 = loopDir.resolve("b2");
            java.nio.file.Files.createSymbolicLink(b1, b2);
            java.nio.file.Files.createSymbolicLink(b2, b1);

            // VERSION DIFFERENCE (measured jdk8u372 vs jdk-27.0.1): copying through a
            // mutual symlink cycle hits ELOOP; JDK-8 maps it to a plain FileSystemException
            // while JDK-27 raises the FileSystemLoopException subtype. This side is JDK-27.
            checkThrowsExactIO(FileSystemLoopException.class,
                    () -> java.nio.file.Files.copy(b1, loopDir.resolve("out")),
                    "JDK-27: ELOOP through symlink cycle -> FileSystemLoopException");
            check(!java.nio.file.Files.exists(loopDir.resolve("out")),
                    "failed copy leaves no output");
        } finally {
            try { java.nio.file.Files.deleteIfExists(loopDir.resolve("b1")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(loopDir.resolve("b2")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(loopDir.resolve("out")); } catch (Exception ignore) { }
            deleteTree(loopDir);
        }
    }

    // ==== batch A11: second extra coverage ====
    static void testContractExtra2() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.Files.createSymbolicLink(dir.resolve("loop"), dir);
            final int[] visits = {0};
            java.nio.file.Files.walkFileTree(dir,
                    java.util.EnumSet.noneOf(java.nio.file.FileVisitOption.class), 0,
                    new java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
                        public java.nio.file.FileVisitResult visitFile(java.nio.file.Path p,
                                java.nio.file.attribute.BasicFileAttributes a) {
                            visits[0]++;
                            return java.nio.file.FileVisitResult.CONTINUE;
                        }
                    });
            // spec: maxDepth 0 means "only the starting file is visited": the start
            // directory is reported via visitFile and its children are never reached
            check(visits[0] == 1, "maxDepth 0 visits only the start entry (loop never reached)");
            java.lang.reflect.Constructor<?> c =
                    FileSystemLoopException.class.getConstructors()[0];
            check(c.getParameterCount() == 1 && c.getParameterTypes()[0] == String.class,
                    "single (String) constructor");
            Object back = serialRoundTrip(new FileSystemLoopException(null));
            check(((FileSystemLoopException) back).getMessage() == null,
                    "round-trip of null-file keeps null message");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dir.resolve("loop")); } catch (Exception ignore) { }
            deleteTree(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractHierarchyAndCtors();
        testContractCauseAndSerialization();
        testContractRealTrigger();
        testContractCopyThroughCycle();
        testContractExtra2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
