/*
 * @test
 * @summary java.nio.file.NotDirectoryException conformance: white-box structure + black-box behavior.
 * @run main NotDirectoryExceptionTest
 */

import java.nio.file.NotDirectoryException;

public class NotDirectoryExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(NotDirectoryException.class), "hierarchy: extends FileSystemException");

        NotDirectoryException e = new NotDirectoryException("f");
        check("f".equals(e.getFile()), "getFile");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path file = dir.resolve("plain.txt");
            java.nio.file.Files.writeString(file, "p");
            boolean threw = false;
            try { java.nio.file.Files.newDirectoryStream(file).close(); }
            catch (NotDirectoryException e) { threw = e.getFile() != null; }
            check(threw, "stream a regular file -> NotDirectoryException");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dir.resolve("plain.txt")); } catch (Exception ignore) { }
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

    // ==== batch A11: constructor / serialization / real trigger ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(NotDirectoryException.class.getSuperclass() == java.nio.file.FileSystemException.class,
                "direct superclass is FileSystemException");
        check(java.io.IOException.class.isAssignableFrom(NotDirectoryException.class),
                "caught as IOException");
        check(java.io.Serializable.class.isAssignableFrom(NotDirectoryException.class),
                "Serializable");
        check(NotDirectoryException.class.getConstructors().length == 1,
                "single public (String) constructor only");
        NotDirectoryException e = new NotDirectoryException("f");
        check("f".equals(e.getFile()) && e.getOtherFile() == null && e.getReason() == null,
                "getters: file stored, other/reason null");
        check("f".equals(e.getMessage()), "message == file");
        check("java.nio.file.NotDirectoryException: f".equals(e.toString()), "toString exact");
        NotDirectoryException n = new NotDirectoryException(null);
        check(n.getFile() == null && n.getMessage() == null, "null file accepted, null message");
    }

    static void testContractCauseAndSerialization() throws Exception {
        NotDirectoryException e = new NotDirectoryException("f");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(NotDirectoryException.class).getSerialVersionUID()
                == -9011457427178200199L, "serialVersionUID == -9011457427178200199");
        Object back = serialRoundTrip(e);
        check(back instanceof NotDirectoryException, "round-trip preserves subclass identity");
        NotDirectoryException r = (NotDirectoryException) back;
        check("f".equals(r.getFile()) && r.getOtherFile() == null && r.getReason() == null,
                "round-trip keeps getters");
        check("f".equals(r.getMessage()), "round-trip message");
    }

    static void testContractRealTrigger() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.Path plain = dir.resolve("plain.txt");
            java.nio.file.Files.write(plain, "p".getBytes("UTF-8"));
            try {
                java.nio.file.Files.newDirectoryStream(plain).close();
                check(false, "newDirectoryStream(regular file) must fail");
            } catch (NotDirectoryException e) {
                check(plain.toString().equals(e.getFile()), "getFile is the file path");
                check(e.getOtherFile() == null && e.getReason() == null, "other/reason null");
                check(plain.toString().equals(e.getMessage()), "message == path");
            }
            check(!java.nio.file.Files.isDirectory(plain),
                    "isDirectory(regular file) false, no exception");
            // precision negative: a MISSING path is NoSuchFileException, not NotDirectory
            checkThrowsExactIO(java.nio.file.NoSuchFileException.class,
                    () -> java.nio.file.Files.newDirectoryStream(dir.resolve("missing")).close(),
                    "newDirectoryStream(missing) -> NoSuchFileException (not NotDirectory)");
            try (java.nio.file.DirectoryStream<java.nio.file.Path> ok =
                    java.nio.file.Files.newDirectoryStream(dir)) {
                check(ok != null, "real directory streams fine");
            }
        } finally {
            deleteTree(dir);
        }
    }

    // ==== batch A11: second extra coverage ====
    static void testContractExtra2() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.Path plain = dir.resolve("plain.txt");
            java.nio.file.Files.write(plain, "p".getBytes("UTF-8"));
            check(java.nio.file.Files.isRegularFile(plain),
                    "control: isRegularFile true without exception");
            final java.util.List<String> seen = new java.util.ArrayList<String>();
            java.nio.file.Files.walkFileTree(plain,
                    new java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
                        public java.nio.file.FileVisitResult visitFile(java.nio.file.Path p,
                                java.nio.file.attribute.BasicFileAttributes a) {
                            seen.add(p.getFileName().toString());
                            return java.nio.file.FileVisitResult.CONTINUE;
                        }
                    });
            check(seen.equals(java.util.Arrays.asList("plain.txt")),
                    "walkFileTree on a plain file visits exactly the file");
            java.lang.reflect.Constructor<?> c =
                    NotDirectoryException.class.getConstructors()[0];
            check(c.getParameterCount() == 1 && c.getParameterTypes()[0] == String.class,
                    "single (String) constructor");
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
        testContractExtra2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
