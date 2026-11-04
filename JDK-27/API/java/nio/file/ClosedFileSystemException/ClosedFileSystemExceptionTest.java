/*
 * @test
 * @summary java.nio.file.ClosedFileSystemException conformance: white-box structure + black-box behavior.
 * @run main ClosedFileSystemExceptionTest
 */

import java.nio.file.ClosedFileSystemException;

public class ClosedFileSystemExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalStateException.class.isAssignableFrom(ClosedFileSystemException.class), "hierarchy: extends IllegalStateException");

        check(new ClosedFileSystemException() != null, "no-arg ctor");
        check(new ClosedFileSystemException().getMessage() == null, "message null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path zip = java.nio.file.Files.createTempFile("jck", ".zip");
        try {
            // seed an empty zip then open it as a filesystem
            try (java.util.zip.ZipOutputStream zos =
                    new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("seed.txt"));
                zos.write("s".getBytes());
            }
            java.nio.file.FileSystem fs = java.nio.file.FileSystems.newFileSystem(zip);
            check(fs.isOpen(), "zip fs open");
            fs.close();
            check(!fs.isOpen(), "zip fs closed");
            boolean threw = false;
            try {
                java.nio.file.Files.writeString(fs.getPath("/seed.txt"), "x");
            } catch (ClosedFileSystemException e) {
                threw = true;
            }
            check(threw, "write after close -> ClosedFileSystemException");
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

    // ==== batch A11: constructor / serialization / closed-zipfs trigger ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(ClosedFileSystemException.class.getSuperclass()
                == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException");
        check(RuntimeException.class.isAssignableFrom(ClosedFileSystemException.class),
                "caught as RuntimeException");
        check(java.io.Serializable.class.isAssignableFrom(ClosedFileSystemException.class),
                "Serializable");
        check(ClosedFileSystemException.class.getConstructors().length == 1,
                "single public no-arg constructor");
        ClosedFileSystemException e = new ClosedFileSystemException();
        check(e.getMessage() == null, "no-arg message null");
        check("java.nio.file.ClosedFileSystemException".equals(e.toString()),
                "toString bare (no colon suffix)");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(ClosedFileSystemException.class)
                .getSerialVersionUID() == -8158336077256193488L,
                "serialVersionUID == -8158336077256193488");
        Object back = serialRoundTrip(new ClosedFileSystemException());
        check(back instanceof ClosedFileSystemException, "round-trip preserves class");
        check(((ClosedFileSystemException) back).getMessage() == null,
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
            java.nio.file.FileSystem zfs =
                    java.nio.file.FileSystems.newFileSystem(zip, (ClassLoader) null);
            check(zfs.isOpen(), "zip fs open before close");
            check(!zfs.isReadOnly(), "writable archive mounts writable fs");
            java.nio.file.Path entry = zfs.getPath("/s.txt");
            check(entry != null, "entry path resolves while open");
            zfs.close();
            check(!zfs.isOpen(), "isOpen false after close");
            checkThrowsExactIO(ClosedFileSystemException.class,
                    () -> java.nio.file.Files.readAllBytes(zfs.getPath("/s.txt")),
                    "provider op after close -> ClosedFileSystemException");
            checkThrowsExact(ClosedFileSystemException.class,
                    () -> java.nio.file.Files.exists(zfs.getPath("/s.txt")),
                    "exists() after close -> ClosedFileSystemException");
            // path OBJECT methods stay usable; only provider operations check open state
            check(zfs.getPath("/x") != null, "fs.getPath after close still returns an object");
            check(zfs.getPath("/s.txt").resolve("y") != null,
                    "Path.resolve after close still returns an object");
        } finally {
            java.nio.file.Files.deleteIfExists(zip);
        }
    }

    // ==== batch A11: extra coverage ====
    static void testContractExtra() throws Exception {
        check(new ClosedFileSystemException().getLocalizedMessage() == null,
                "localizedMessage null when message null");
        check(new ClosedFileSystemException().getSuppressed().length == 0,
                "no suppressed exceptions by default");
        check(new ClosedFileSystemException().fillInStackTrace().getClass() == ClosedFileSystemException.class,
                "fillInStackTrace keeps exact type");
        Object back = serialRoundTrip(new ClosedFileSystemException());
        check(back.getClass().getName().equals("java.nio.file.ClosedFileSystemException"),
                "round-trip exact class name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractHierarchyAndCtors();
        testContractRealTrigger();
        testContractExtra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
