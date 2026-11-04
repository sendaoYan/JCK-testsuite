/*
 * @test
 * @summary java.nio.file.NoSuchFileException conformance: white-box structure + black-box behavior.
 * @run main NoSuchFileExceptionTest
 */

import java.nio.file.NoSuchFileException;

public class NoSuchFileExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(NoSuchFileException.class),
              "hierarchy: extends FileSystemException");

        NoSuchFileException e = new NoSuchFileException("f", "o", "r");
        check("f".equals(e.getFile()) && "o".equals(e.getOtherFile()), "files stored");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path ghost = dir.resolve("ghost.txt");
            boolean read = false;
            try { java.nio.file.Files.readAllBytes(ghost); }
            catch (NoSuchFileException e) {
                read = e.getFile().endsWith("ghost.txt") && e.getOtherFile() == null;
            }
            check(read, "read missing -> NoSuchFileException(file)");
            boolean copy = false;
            try { java.nio.file.Files.copy(ghost, dir.resolve("out.txt")); }
            catch (NoSuchFileException e) {
                copy = e.getFile() != null;
            }
            check(copy, "copy missing source -> NoSuchFileException with file");
            java.nio.file.Files.deleteIfExists(ghost);
            check(true, "deleteIfExists silent");
        } finally {
            java.nio.file.Files.deleteIfExists(dir);
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

    // ==== batch A11: constructor / serialization / real triggers ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(NoSuchFileException.class.getSuperclass() == java.nio.file.FileSystemException.class,
                "direct superclass is FileSystemException");
        check(java.io.IOException.class.isAssignableFrom(NoSuchFileException.class),
                "caught as IOException");
        check(java.io.Serializable.class.isAssignableFrom(NoSuchFileException.class),
                "Serializable");
        check(NoSuchFileException.class.getConstructors().length == 2,
                "2 public constructors (String) and (String,String,String)");
        NoSuchFileException one = new NoSuchFileException("f");
        check("f".equals(one.getFile()) && one.getOtherFile() == null && one.getReason() == null,
                "1-arg getters");
        check("f".equals(one.getMessage()), "message == file");
        check("java.nio.file.NoSuchFileException: f".equals(one.toString()), "toString exact");
        NoSuchFileException three = new NoSuchFileException("f", "o", "r");
        check("f".equals(three.getFile()) && "o".equals(three.getOtherFile())
                && "r".equals(three.getReason()), "3-arg getters");
        check("f -> o: r".equals(three.getMessage()), "inherited message composition");
        NoSuchFileException n = new NoSuchFileException(null);
        check(n.getFile() == null && n.getMessage() == null, "null file accepted, null message");
    }

    static void testContractCauseAndSerialization() throws Exception {
        NoSuchFileException e = new NoSuchFileException("f");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(NoSuchFileException.class).getSerialVersionUID()
                == -1390291775875351931L, "serialVersionUID == -1390291775875351931");
        Object back = serialRoundTrip(new NoSuchFileException("f", "o", "r"));
        check(back instanceof NoSuchFileException, "round-trip preserves subclass identity");
        NoSuchFileException r = (NoSuchFileException) back;
        check("f".equals(r.getFile()) && "o".equals(r.getOtherFile())
                && "r".equals(r.getReason()), "round-trip keeps getters");
        check("f -> o: r".equals(r.getMessage()), "round-trip message");
    }

    static void testContractRealTrigger() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.Path ghost = dir.resolve("ghost.txt");
            try {
                java.nio.file.Files.readAttributes(ghost,
                        java.nio.file.attribute.BasicFileAttributes.class);
                check(false, "readAttributes on missing must fail");
            } catch (NoSuchFileException e) {
                check(ghost.toString().equals(e.getFile()), "getFile is the missing path");
                check(e.getOtherFile() == null && e.getReason() == null, "other/reason null");
                check(ghost.toString().equals(e.getMessage()), "message == path");
            }
            checkThrowsExactIO(NoSuchFileException.class,
                    () -> java.nio.file.Files.delete(ghost),
                    "delete(missing) -> NoSuchFileException");
            checkThrowsExactIO(NoSuchFileException.class,
                    () -> java.nio.file.Files.newInputStream(ghost).close(),
                    "newInputStream(missing) -> NoSuchFileException");
            checkThrowsExactIO(NoSuchFileException.class,
                    () -> java.nio.file.Files.newDirectoryStream(ghost).close(),
                    "newDirectoryStream(missing) -> NoSuchFileException (not NotDirectory)");
            checkThrowsExactIO(NoSuchFileException.class,
                    () -> java.nio.file.Files.copy(ghost, dir.resolve("out.txt")),
                    "copy(missing source) -> NoSuchFileException");
            check(!java.nio.file.Files.deleteIfExists(ghost),
                    "deleteIfExists(missing) returns false silently");
            check(!java.nio.file.Files.exists(ghost),
                    "exists(missing) returns false, no exception");
            check(java.nio.file.Files.notExists(ghost), "notExists(missing) true");
        } finally {
            deleteTree(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractHierarchyAndCtors();
        testContractCauseAndSerialization();
        testContractRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
