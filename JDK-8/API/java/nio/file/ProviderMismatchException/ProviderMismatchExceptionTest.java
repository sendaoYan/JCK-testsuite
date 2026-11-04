/*
 * @test
 * @summary java.nio.file.ProviderMismatchException conformance: hierarchy and constructor.
 * @run main ProviderMismatchExceptionTest
 */

import java.nio.file.ProviderMismatchException;

public class ProviderMismatchExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testProviderMismatchException() {
        check(IllegalArgumentException.class.isAssignableFrom(ProviderMismatchException.class), "extends IllegalArgumentException");
        ProviderMismatchException e = new ProviderMismatchException();
        check(e instanceof IllegalArgumentException, "instanceof IllegalArgumentException");
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

    // ==== batch A11: constructors / serialization / three real triggers ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(ProviderMismatchException.class.getSuperclass()
                == java.lang.IllegalArgumentException.class,
                "direct superclass is IllegalArgumentException");
        check(RuntimeException.class.isAssignableFrom(ProviderMismatchException.class),
                "caught as RuntimeException");
        check(java.io.Serializable.class.isAssignableFrom(ProviderMismatchException.class),
                "Serializable");
        check(ProviderMismatchException.class.getConstructors().length == 2,
                "2 public constructors: no-arg and (String)");
        check(new ProviderMismatchException().getMessage() == null, "no-arg message null");
        check("m".equals(new ProviderMismatchException("m").getMessage()),
                "message ctor stores message");
        check("java.nio.file.ProviderMismatchException: m"
                .equals(new ProviderMismatchException("m").toString()), "toString with message");
        check("java.nio.file.ProviderMismatchException"
                .equals(new ProviderMismatchException().toString()), "toString bare form");
        ProviderMismatchException e = new ProviderMismatchException();
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(ProviderMismatchException.class)
                .getSerialVersionUID() == 4990847485741612530L,
                "serialVersionUID == 4990847485741612530");
        Object back = serialRoundTrip(new ProviderMismatchException("m"));
        check(back instanceof ProviderMismatchException, "round-trip preserves class");
        check("m".equals(((ProviderMismatchException) back).getMessage()),
                "round-trip keeps message");
    }

    static void testContractRealTrigger() throws Exception {
        // NOTE (measured): this jdk8u372 build ships com.sun.nio.zipfs and jdk-27 ships
        // jdk.nio.zipfs, so the zip triggers behave identically on both sides
        java.nio.file.Path zip = java.nio.file.Files.createTempFile("a11", ".zip");
        try {
            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
                    java.nio.file.Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("s.txt"));
                zos.write("s".getBytes("UTF-8"));
            }
            java.nio.file.spi.FileSystemProvider def =
                    java.nio.file.FileSystems.getDefault().provider();
            try (java.nio.file.FileSystem zfs =
                    java.nio.file.FileSystems.newFileSystem(zip, (ClassLoader) null)) {
                java.nio.file.Path zpath = zfs.getPath("/s.txt");
                check(zpath.getFileSystem() == zfs, "path bound to zip fs");
                checkThrowsExactIO(ProviderMismatchException.class,
                        () -> def.readAttributes(zpath,
                                java.nio.file.attribute.BasicFileAttributes.class),
                        "default provider readAttributes(zipPath) -> ProviderMismatchException");
                checkThrowsExact(ProviderMismatchException.class,
                        () -> { try { def.newByteChannel(zpath,
                                    java.util.EnumSet.of(
                                            java.nio.file.StandardOpenOption.READ)).close(); }
                                catch (java.io.IOException i) {
                                    throw new RuntimeException(i); } },
                        "default provider newByteChannel(zipPath) -> ProviderMismatchException");
                java.nio.file.Path probeDir =
                        java.nio.file.Files.createTempDirectory("a11sds");
                try {
                    java.nio.file.DirectoryStream<java.nio.file.Path> ds =
                            java.nio.file.Files.newDirectoryStream(probeDir);
                    try {
                        if (ds instanceof java.nio.file.SecureDirectoryStream) {
                            final java.nio.file.SecureDirectoryStream<java.nio.file.Path> sds =
                                    (java.nio.file.SecureDirectoryStream<java.nio.file.Path>) ds;
                            checkThrowsExact(ProviderMismatchException.class,
                                    () -> { try { sds.newByteChannel(zpath,
                                                java.util.EnumSet.of(
                                                        java.nio.file.StandardOpenOption.READ))
                                                .close(); }
                                            catch (java.io.IOException i) {
                                                throw new RuntimeException(i); } },
                                    "SecureDirectoryStream.newByteChannel(foreign path)"
                                    + " -> ProviderMismatchException");
                        } else {
                            check(true, "no SecureDirectoryStream on this platform; skipped");
                        }
                    } finally {
                        ds.close();
                    }
                } finally {
                    deleteTree(probeDir);
                }
                // control: the OWN provider serves the same path without complaint
                java.nio.file.attribute.BasicFileAttributes attrs =
                        zpath.getFileSystem().provider().readAttributes(zpath,
                                java.nio.file.attribute.BasicFileAttributes.class);
                check(attrs != null && !attrs.isDirectory(), "own provider reads zip entry");
            }
        } finally {
            java.nio.file.Files.deleteIfExists(zip);
        }
    }

    // ==== batch A11: extra coverage ====
    static void testContractExtra() throws Exception {
        check(new ProviderMismatchException("m").getLocalizedMessage().equals("m"),
                "localizedMessage delegates to message");
        check(new ProviderMismatchException("m").getSuppressed().length == 0,
                "no suppressed exceptions by default");
        ProviderMismatchException chained = new ProviderMismatchException("m");
        chained.initCause(new RuntimeException("why"));
        Object back = serialRoundTrip(chained);
        check(back instanceof ProviderMismatchException && ((ProviderMismatchException) back).getCause()
                instanceof RuntimeException && "why".equals(
                ((ProviderMismatchException) back).getCause().getMessage()),
                "round-trip keeps chained cause");
        check("m".equals(((ProviderMismatchException) back).getMessage()),
                "round-trip keeps message alongside cause");
    }

    // ==== batch A11: second extra coverage ====
    static void testContractExtra2() throws Exception {
        int zero = 0, one = 0;
        for (java.lang.reflect.Constructor<?> c :
                ProviderMismatchException.class.getConstructors()) {
            if (c.getParameterCount() == 0) zero++;
            if (c.getParameterCount() == 1 && c.getParameterTypes()[0] == String.class) one++;
        }
        check(zero == 1, "exactly one no-arg constructor");
        check(one == 1, "exactly one (String) constructor");
        check(new ProviderMismatchException("m").getLocalizedMessage().equals("m"),
                "localizedMessage delegates to message");
    }

    public static void main(String[] args) throws Exception {
        testProviderMismatchException();
        testContractHierarchyAndCtors();
        testContractRealTrigger();
        testContractExtra();
        testContractExtra2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
