/*
 * @test
 * @summary java.nio.file.NotLinkException conformance: hierarchy and constructor.
 * @run main NotLinkExceptionTest
 */

import java.nio.file.NotLinkException;
import java.nio.file.FileSystemException;

public class NotLinkExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNotLinkException() {
        check(FileSystemException.class.isAssignableFrom(NotLinkException.class), "extends FileSystemException");
        NotLinkException e = new NotLinkException("f");
        check(e instanceof FileSystemException, "instanceof FileSystemException");
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
        check(NotLinkException.class.getSuperclass() == java.nio.file.FileSystemException.class,
                "direct superclass is FileSystemException");
        check(java.io.IOException.class.isAssignableFrom(NotLinkException.class),
                "caught as IOException");
        check(java.io.Serializable.class.isAssignableFrom(NotLinkException.class),
                "Serializable");
        check(NotLinkException.class.getConstructors().length == 2,
                "2 public constructors (String) and (String,String,String)");
        NotLinkException one = new NotLinkException("f");
        check("f".equals(one.getFile()) && one.getOtherFile() == null && one.getReason() == null,
                "1-arg getters");
        check("f".equals(one.getMessage()), "message == file");
        check("java.nio.file.NotLinkException: f".equals(one.toString()), "toString exact");
        NotLinkException three = new NotLinkException("f", "o", "r");
        check("f".equals(three.getFile()) && "o".equals(three.getOtherFile())
                && "r".equals(three.getReason()), "3-arg getters");
        check("f -> o: r".equals(three.getMessage()), "inherited message composition");
        check(new NotLinkException(null).getFile() == null
                && new NotLinkException(null).getMessage() == null,
                "null file accepted with null message");
    }

    static void testContractCauseAndSerialization() throws Exception {
        NotLinkException e = new NotLinkException("f");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(NotLinkException.class).getSerialVersionUID()
                == -388655596416518021L, "serialVersionUID == -388655596416518021");
        Object back = serialRoundTrip(new NotLinkException("f", "o", "r"));
        check(back instanceof NotLinkException, "round-trip preserves subclass identity");
        NotLinkException r = (NotLinkException) back;
        check("f".equals(r.getFile()) && "o".equals(r.getOtherFile())
                && "r".equals(r.getReason()), "round-trip keeps getters");
        check("f -> o: r".equals(r.getMessage()), "round-trip message");
    }

    static void testContractRealTrigger() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.Path plain = dir.resolve("plain.txt");
            java.nio.file.Files.write(plain, "x".getBytes("UTF-8"));
            java.nio.file.Path sub = dir.resolve("sub");
            java.nio.file.Files.createDirectory(sub);
            try {
                java.nio.file.Files.readSymbolicLink(plain);
                check(false, "readSymbolicLink(regular file) must fail");
            } catch (NotLinkException e) {
                check(plain.toString().equals(e.getFile()), "getFile is the non-link path");
                check(e.getOtherFile() == null && e.getReason() == null, "other/reason null");
                check(plain.toString().equals(e.getMessage()), "message == path");
            }
            // a directory that is not a link is rejected the same way (EINVAL -> NotLink)
            checkThrowsExactIO(NotLinkException.class,
                    () -> java.nio.file.Files.readSymbolicLink(sub),
                    "readSymbolicLink(directory) -> NotLinkException");
            // precision negative: MISSING path is NoSuchFileException, not NotLink
            checkThrowsExactIO(java.nio.file.NoSuchFileException.class,
                    () -> java.nio.file.Files.readSymbolicLink(dir.resolve("missing")),
                    "readSymbolicLink(missing) -> NoSuchFileException (not NotLink)");
            java.nio.file.Path link = dir.resolve("link");
            java.nio.file.Files.createSymbolicLink(link, plain);
            check(java.nio.file.Files.readSymbolicLink(link).equals(plain),
                    "a real link reads back its target");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dir.resolve("link")); } catch (Exception ignore) { }
            deleteTree(dir);
        }
    }

    // ==== batch A11: second extra coverage ====
    static void testContractExtra2() throws Exception {
        check("f -> o".equals(new NotLinkException("f", "o", null).getMessage()),
                "(f,o,null) message 'f -> o'");
        check(!java.nio.file.Files.isSymbolicLink(
                java.nio.file.Paths.get(".")), "control: isSymbolicLink(plain) false");
        Object back = serialRoundTrip(new NotLinkException("f", "o", "r"));
        check(back instanceof java.nio.file.FileSystemException,
                "round-trip survives as FileSystemException on the wire");
        check("f -> o: r".equals(((NotLinkException) back).getMessage()),
                "round-trip keeps composed message");
    }

    public static void main(String[] args) throws Exception {
        testNotLinkException();
        testContractHierarchyAndCtors();
        testContractCauseAndSerialization();
        testContractRealTrigger();
        testContractExtra2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
