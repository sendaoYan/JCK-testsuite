/*
 * @test
 * @summary java.nio.file.DirectoryNotEmptyException conformance: white-box structure + black-box behavior.
 * @run main DirectoryNotEmptyExceptionTest
 */

import java.nio.file.DirectoryNotEmptyException;

public class DirectoryNotEmptyExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(DirectoryNotEmptyException.class), "hierarchy: extends FileSystemException");

        DirectoryNotEmptyException e = new DirectoryNotEmptyException("dir");
        check("dir".equals(e.getFile()), "getFile");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path child = dir.resolve("keep.txt");
            java.nio.file.Files.writeString(child, "k");
            boolean threw = false;
            try { java.nio.file.Files.delete(dir); }
            catch (DirectoryNotEmptyException e) {
                threw = e.getFile().endsWith(dir.getFileName().toString());
            }
            check(threw, "deleting non-empty dir -> DirectoryNotEmptyException with file");
            // an empty dir deletes fine afterwards
            java.nio.file.Files.delete(child);
            java.nio.file.Files.delete(dir);
            check(!java.nio.file.Files.exists(dir), "emptied dir deletes cleanly");
        } finally {
            // best-effort cleanup if an assertion fired
            try { java.nio.file.Files.deleteIfExists(dir.resolve("keep.txt")); } catch (Exception ignore) { }
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
        check(DirectoryNotEmptyException.class.getSuperclass()
                == java.nio.file.FileSystemException.class,
                "direct superclass is FileSystemException");
        check(java.io.IOException.class.isAssignableFrom(DirectoryNotEmptyException.class),
                "caught as IOException");
        check(java.io.Serializable.class.isAssignableFrom(DirectoryNotEmptyException.class),
                "Serializable");
        check(DirectoryNotEmptyException.class.getConstructors().length == 1,
                "single public (String) constructor only");
        DirectoryNotEmptyException e = new DirectoryNotEmptyException("dir");
        check("dir".equals(e.getFile()) && e.getOtherFile() == null && e.getReason() == null,
                "getters: file stored, other/reason null");
        check("dir".equals(e.getMessage()), "message == file");
        check("java.nio.file.DirectoryNotEmptyException: dir".equals(e.toString()),
                "toString exact");
        DirectoryNotEmptyException n = new DirectoryNotEmptyException(null);
        check(n.getFile() == null && n.getMessage() == null, "null file accepted, null message");
        check(n.getOtherFile() == null && n.getReason() == null, "null file: others null");
    }

    static void testContractCauseAndSerialization() throws Exception {
        DirectoryNotEmptyException e = new DirectoryNotEmptyException("dir");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(DirectoryNotEmptyException.class)
                .getSerialVersionUID() == 3056667871802779003L,
                "serialVersionUID == 3056667871802779003");
        Object back = serialRoundTrip(e);
        check(back instanceof DirectoryNotEmptyException,
                "round-trip preserves subclass identity");
        DirectoryNotEmptyException r = (DirectoryNotEmptyException) back;
        check("dir".equals(r.getFile()) && r.getOtherFile() == null && r.getReason() == null,
                "round-trip keeps getters");
        check("dir".equals(r.getMessage()), "round-trip message");
    }

    static void testContractRealTrigger() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.Path child = dir.resolve("keep.txt");
            java.nio.file.Files.write(child, "k".getBytes("UTF-8"));
            try {
                java.nio.file.Files.delete(dir);
                check(false, "delete of non-empty dir must fail");
            } catch (DirectoryNotEmptyException e) {
                check(dir.toString().equals(e.getFile()), "getFile is the directory");
                check(e.getOtherFile() == null && e.getReason() == null, "other/reason null");
                check(dir.toString().equals(e.getMessage()), "message == path");
            }
            try {
                java.nio.file.Files.deleteIfExists(dir);
                check(false, "deleteIfExists of non-empty dir must fail too");
            } catch (DirectoryNotEmptyException e) {
                check(dir.toString().equals(e.getFile()),
                        "deleteIfExists propagates with the directory");
            }
            // precision negative: an existing directory reused as createDirectory target
            // fails with FileAlreadyExistsException, not DirectoryNotEmptyException
            checkThrowsExactIO(java.nio.file.FileAlreadyExistsException.class,
                    () -> java.nio.file.Files.createDirectory(dir),
                    "createDirectory(existing) -> FileAlreadyExistsException (not DNE)");
            java.nio.file.Files.delete(child);
            java.nio.file.Files.delete(dir);
            check(!java.nio.file.Files.exists(dir), "emptied dir now deletes cleanly");
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
