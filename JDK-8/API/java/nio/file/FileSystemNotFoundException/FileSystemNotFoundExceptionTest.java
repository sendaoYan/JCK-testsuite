/*
 * @test
 * @summary java.nio.file.FileSystemNotFoundException conformance: hierarchy and constructor.
 * @run main FileSystemNotFoundExceptionTest
 */

import java.nio.file.FileSystemNotFoundException;

public class FileSystemNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileSystemNotFoundException() {
        check(RuntimeException.class.isAssignableFrom(FileSystemNotFoundException.class), "extends RuntimeException");
        FileSystemNotFoundException e = new FileSystemNotFoundException();
        check(e instanceof RuntimeException, "instanceof RuntimeException");
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

    // ==== batch A11: constructors / serialization / real triggers ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(FileSystemNotFoundException.class.getSuperclass()
                == java.lang.RuntimeException.class,
                "direct superclass is RuntimeException");
        check(java.io.Serializable.class.isAssignableFrom(FileSystemNotFoundException.class),
                "Serializable");
        check(FileSystemNotFoundException.class.getConstructors().length == 2,
                "2 public constructors: no-arg and (String)");
        check(new FileSystemNotFoundException().getMessage() == null, "no-arg message null");
        FileSystemNotFoundException e = new FileSystemNotFoundException("gone");
        check("gone".equals(e.getMessage()), "message ctor stores message");
        check("java.nio.file.FileSystemNotFoundException: gone".equals(e.toString()),
                "toString with message");
        check("java.nio.file.FileSystemNotFoundException"
                .equals(new FileSystemNotFoundException().toString()), "toString bare form");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(FileSystemNotFoundException.class)
                .getSerialVersionUID() == 7999581764446402397L,
                "serialVersionUID == 7999581764446402397");
        Object back = serialRoundTrip(new FileSystemNotFoundException("gone"));
        check(back instanceof FileSystemNotFoundException, "round-trip preserves class");
        check("gone".equals(((FileSystemNotFoundException) back).getMessage()),
                "round-trip keeps message");
    }

    static void testContractRealTrigger() throws Exception {
        // jar scheme has an installed provider but nothing open -> FileSystemNotFoundException
        java.nio.file.Path probeZip = java.nio.file.Files.createTempFile("a11probe", ".zip");
        java.net.URI neverOpened = new java.net.URI("jar", probeZip.toUri().toString(), null);
        checkThrowsExact(FileSystemNotFoundException.class,
                () -> java.nio.file.FileSystems.getFileSystem(neverOpened),
                "getFileSystem(unopened jar) -> FileSystemNotFoundException");
        java.nio.file.Files.deleteIfExists(probeZip);
        java.nio.file.Path zip = java.nio.file.Files.createTempFile("a11", ".zip");
        try {
            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
                    java.nio.file.Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("s.txt"));
                zos.write("s".getBytes("UTF-8"));
            }
            java.net.URI jarUri = new java.net.URI("jar", zip.toUri().toString(), null);
            java.nio.file.FileSystem fs =
                    java.nio.file.FileSystems.newFileSystem(jarUri,
                            new java.util.HashMap<String, Object>());
            check(java.nio.file.FileSystems.getFileSystem(jarUri) == fs,
                    "getFileSystem(URI) returns the open instance");
            fs.close();
            checkThrowsExact(FileSystemNotFoundException.class,
                    () -> java.nio.file.FileSystems.getFileSystem(jarUri),
                    "getFileSystem(jar) after close -> FileSystemNotFoundException");
            // precision negative: an UNKNOWN scheme is ProviderNotFound, not FileSystemNotFound
            checkThrowsExact(java.nio.file.ProviderNotFoundException.class,
                    () -> { try { java.nio.file.FileSystems.getFileSystem(
                                new java.net.URI("bogus://x")); }
                            catch (java.net.URISyntaxException u) {
                                throw new RuntimeException(u); } },
                    "getFileSystem(unknown scheme) -> ProviderNotFoundException (not FSNF)");
            // the default provider answers for file:/// and never throws here
            check(java.nio.file.FileSystems.getFileSystem(new java.net.URI("file:///")) != null,
                    "getFileSystem(file:///) returns the default fs");
        } finally {
            java.nio.file.Files.deleteIfExists(zip);
        }
    }

    // ==== batch A11: extra coverage ====
    static void testContractExtra() throws Exception {
        check(new FileSystemNotFoundException("m").getLocalizedMessage().equals("m"),
                "localizedMessage delegates to message");
        check(new FileSystemNotFoundException("m").getSuppressed().length == 0,
                "no suppressed exceptions by default");
        FileSystemNotFoundException chained = new FileSystemNotFoundException("m");
        chained.initCause(new RuntimeException("why"));
        Object back = serialRoundTrip(chained);
        check(back instanceof FileSystemNotFoundException && ((FileSystemNotFoundException) back).getCause()
                instanceof RuntimeException && "why".equals(
                ((FileSystemNotFoundException) back).getCause().getMessage()),
                "round-trip keeps chained cause");
        check("m".equals(((FileSystemNotFoundException) back).getMessage()),
                "round-trip keeps message alongside cause");
    }

    // ==== batch A11: second extra coverage ====
    static void testContractExtra2() throws Exception {
        int zero = 0, one = 0;
        for (java.lang.reflect.Constructor<?> c :
                FileSystemNotFoundException.class.getConstructors()) {
            if (c.getParameterCount() == 0) zero++;
            if (c.getParameterCount() == 1 && c.getParameterTypes()[0] == String.class) one++;
        }
        check(zero == 1, "exactly one no-arg constructor");
        check(one == 1, "exactly one (String) constructor");
        Object back = serialRoundTrip(new FileSystemNotFoundException());
        check(back instanceof FileSystemNotFoundException
                && ((FileSystemNotFoundException) back).getMessage() == null,
                "round-trip of no-arg keeps null message");
        check(new FileSystemNotFoundException("gone").fillInStackTrace()
                .getClass() == FileSystemNotFoundException.class,
                "fillInStackTrace keeps exact type");
    }

    public static void main(String[] args) throws Exception {
        testFileSystemNotFoundException();
        testContractHierarchyAndCtors();
        testContractRealTrigger();
        testContractExtra();
        testContractExtra2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
