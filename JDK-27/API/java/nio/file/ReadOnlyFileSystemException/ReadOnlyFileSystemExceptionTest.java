/*
 * @test
 * @summary java.nio.file.ReadOnlyFileSystemException conformance: white-box structure + black-box behavior.
 * @run main ReadOnlyFileSystemExceptionTest
 */

import java.nio.file.ReadOnlyFileSystemException;

public class ReadOnlyFileSystemExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.UnsupportedOperationException.class.isAssignableFrom(ReadOnlyFileSystemException.class), "hierarchy: extends UnsupportedOperationException");

        check(new ReadOnlyFileSystemException() != null, "no-arg ctor");
        check(new ReadOnlyFileSystemException().getMessage() == null, "message null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path zip = java.nio.file.Files.createTempFile("jck", ".zip");
        try {
            try (java.util.zip.ZipOutputStream zos =
                    new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("s.txt"));
            }
            // a read-only underlying archive mounts a read-only filesystem
            java.nio.file.Files.setPosixFilePermissions(zip,
                    java.nio.file.attribute.PosixFilePermissions.fromString("r--r--r--"));
            try (java.nio.file.FileSystem zfs = java.nio.file.FileSystems.newFileSystem(zip)) {
                check(zfs.isReadOnly(), "read-only archive mounts read-only fs");
                java.nio.file.Path entry = zfs.getPath("/s.txt");
                boolean threw = false;
                try {
                    java.nio.file.Files.writeString(entry, "nope");
                } catch (ReadOnlyFileSystemException e) {
                    threw = true;
                }
                check(threw, "write on read-only fs -> ReadOnlyFileSystemException");
            }
            java.nio.file.Files.setPosixFilePermissions(zip,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-r--r--"));
        } finally {
            java.nio.file.Files.deleteIfExists(zip);
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

    // ==== batch A11: constructor / serialization / read-only zipfs trigger ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(ReadOnlyFileSystemException.class.getSuperclass()
                == java.lang.UnsupportedOperationException.class,
                "direct superclass is UnsupportedOperationException");
        check(RuntimeException.class.isAssignableFrom(ReadOnlyFileSystemException.class),
                "caught as RuntimeException");
        check(java.io.Serializable.class.isAssignableFrom(ReadOnlyFileSystemException.class),
                "Serializable");
        check(ReadOnlyFileSystemException.class.getConstructors().length == 1,
                "single public no-arg constructor");
        ReadOnlyFileSystemException e = new ReadOnlyFileSystemException();
        check(e.getMessage() == null, "no-arg message null");
        check("java.nio.file.ReadOnlyFileSystemException".equals(e.toString()),
                "toString bare (no colon suffix)");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(ReadOnlyFileSystemException.class)
                .getSerialVersionUID() == -6822409595617487197L,
                "serialVersionUID == -6822409595617487197");
        Object back = serialRoundTrip(new ReadOnlyFileSystemException());
        check(back instanceof ReadOnlyFileSystemException, "round-trip preserves class");
        check(((ReadOnlyFileSystemException) back).getMessage() == null,
                "round-trip keeps null message");
    }

    static void testContractRealTrigger() throws Exception {
        java.nio.file.Path zip = java.nio.file.Files.createTempFile("a11", ".zip");
        try {
            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
                    java.nio.file.Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("s.txt"));
                zos.write("s".getBytes("UTF-8"));
            }
            // control: a writable archive mounts a writable fs and accepts writes
            try (java.nio.file.FileSystem rw =
                    java.nio.file.FileSystems.newFileSystem(zip, (ClassLoader) null)) {
                check(!rw.isReadOnly(), "writable archive mounts writable fs");
                java.nio.file.Files.write(rw.getPath("/s.txt"), "n".getBytes("UTF-8"));
                check(new String(java.nio.file.Files.readAllBytes(rw.getPath("/s.txt")),
                        "UTF-8").equals("n"), "zip entry write visible");
            }
            // an unwritable archive (0444) mounts a read-only fs
            java.nio.file.Files.setPosixFilePermissions(zip,
                    java.nio.file.attribute.PosixFilePermissions.fromString("r--r--r--"));
            try (java.nio.file.FileSystem ro =
                    java.nio.file.FileSystems.newFileSystem(zip, (ClassLoader) null)) {
                check(ro.isReadOnly(), "read-only archive mounts read-only fs");
                final java.nio.file.Path entry = ro.getPath("/s.txt");
                checkThrowsExact(ReadOnlyFileSystemException.class,
                        () -> { try { java.nio.file.Files.write(entry, "x".getBytes("UTF-8")); }
                                catch (java.io.IOException i) {
                                    throw new RuntimeException(i); } },
                        "write on read-only fs -> ReadOnlyFileSystemException");
                checkThrowsExact(ReadOnlyFileSystemException.class,
                        () -> { try { java.nio.file.Files.delete(entry); }
                                catch (java.io.IOException i) {
                                    throw new RuntimeException(i); } },
                        "delete on read-only fs -> ReadOnlyFileSystemException");
                checkThrowsExact(ReadOnlyFileSystemException.class,
                        () -> { try { java.nio.file.Files.createFile(ro.getPath("/n.txt")); }
                                catch (java.io.IOException i) {
                                    throw new RuntimeException(i); } },
                        "create on read-only fs -> ReadOnlyFileSystemException");
                // reads keep working on the read-only mount
                check(new String(java.nio.file.Files.readAllBytes(entry), "UTF-8").equals("n"),
                        "read on read-only fs still works");
            }
        } finally {
            try { java.nio.file.Files.setPosixFilePermissions(zip,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-r--r--")); }
            catch (Exception ignore) { }
            java.nio.file.Files.deleteIfExists(zip);
        }
    }

    // ==== batch A11: extra coverage ====
    static void testContractExtra() throws Exception {
        check(new ReadOnlyFileSystemException().getLocalizedMessage() == null,
                "localizedMessage null when message null");
        check(new ReadOnlyFileSystemException().getSuppressed().length == 0,
                "no suppressed exceptions by default");
        check(new ReadOnlyFileSystemException().fillInStackTrace().getClass() == ReadOnlyFileSystemException.class,
                "fillInStackTrace keeps exact type");
        Object back = serialRoundTrip(new ReadOnlyFileSystemException());
        check(back.getClass().getName().equals("java.nio.file.ReadOnlyFileSystemException"),
                "round-trip exact class name");
    }

    // ==== batch A11: second extra coverage ====
    static void testContractExtra2() throws Exception {
        check(ReadOnlyFileSystemException.class.getConstructors().length == 1,
                "single no-arg constructor");
        check(new ReadOnlyFileSystemException() instanceof UnsupportedOperationException,
                "is an UnsupportedOperationException");
        check(new ReadOnlyFileSystemException().getSuppressed().length == 0,
                "no suppressed exceptions by default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractHierarchyAndCtors();
        testContractRealTrigger();
        testContractExtra();
        testContractExtra2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
