/*
 * @test
 * @summary java.nio.file.FileSystemAlreadyExistsException conformance: white-box structure + black-box behavior.
 * @run main FileSystemAlreadyExistsExceptionTest
 */

import java.nio.file.FileSystemAlreadyExistsException;

public class FileSystemAlreadyExistsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.RuntimeException.class.isAssignableFrom(FileSystemAlreadyExistsException.class), "hierarchy: extends RuntimeException");

        check(new FileSystemAlreadyExistsException() != null, "no-arg ctor");
        check(new FileSystemAlreadyExistsException("msg").getMessage().equals("msg"),
              "message ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path zip = java.nio.file.Files.createTempFile("jck", ".zip");
        try {
            try (java.util.zip.ZipOutputStream zos =
                    new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("s.txt"));
            }
            java.net.URI uri = new java.net.URI("jar", zip.toUri().toString(), null);
            java.nio.file.FileSystem fs1 = java.nio.file.FileSystems.newFileSystem(uri, java.util.Map.of());
            java.nio.file.FileSystem fs2 = null;
            boolean threw = false;
            try {
                fs2 = java.nio.file.FileSystems.newFileSystem(uri, java.util.Map.of());
            } catch (FileSystemAlreadyExistsException e) {
                threw = true;
            } finally {
                if (fs2 != null) fs2.close();
                fs1.close();
            }
            check(threw, "second open of same URI -> FileSystemAlreadyExistsException");
        } catch (java.net.URISyntaxException e) {
            throw new AssertionError("URI building failed", e);
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

    // ==== batch A11: constructors / serialization / two real triggers ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(FileSystemAlreadyExistsException.class.getSuperclass()
                == java.lang.RuntimeException.class,
                "direct superclass is RuntimeException");
        check(java.io.Serializable.class.isAssignableFrom(FileSystemAlreadyExistsException.class),
                "Serializable");
        check(FileSystemAlreadyExistsException.class.getConstructors().length == 2,
                "2 public constructors: no-arg and (String)");
        check(new FileSystemAlreadyExistsException().getMessage() == null,
                "no-arg message null");
        FileSystemAlreadyExistsException e = new FileSystemAlreadyExistsException("dup");
        check("dup".equals(e.getMessage()), "message ctor stores message");
        check("java.nio.file.FileSystemAlreadyExistsException: dup".equals(e.toString()),
                "toString with message");
        check("java.nio.file.FileSystemAlreadyExistsException"
                .equals(new FileSystemAlreadyExistsException().toString()),
                "toString bare form");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(FileSystemAlreadyExistsException.class)
                .getSerialVersionUID() == -5438419127181131148L,
                "serialVersionUID == -5438419127181131148");
    }

    static void testContractSerialization() throws Exception {
        Object back = serialRoundTrip(new FileSystemAlreadyExistsException("dup"));
        check(back instanceof FileSystemAlreadyExistsException, "round-trip preserves class");
        check("dup".equals(((FileSystemAlreadyExistsException) back).getMessage()),
                "round-trip keeps message");
        Object backN = serialRoundTrip(new FileSystemAlreadyExistsException());
        check(backN instanceof FileSystemAlreadyExistsException
                && ((FileSystemAlreadyExistsException) backN).getMessage() == null,
                "round-trip keeps null message");
    }

    static void testContractRealTrigger() throws Exception {
        // trigger 1 (no zip needed): the default "file" provider already has an open
        // file system, so opening another one over file: URIs is refused
        java.util.Map<String, Object> env = new java.util.HashMap<String, Object>();
        checkThrowsExact(FileSystemAlreadyExistsException.class,
                () -> { try { java.nio.file.FileSystems.newFileSystem(
                        new java.net.URI("file:///"), env); }
                        catch (java.net.URISyntaxException | java.io.IOException i) {
                            throw new RuntimeException(i); } },
                "newFileSystem over the already-open default fs -> FileSystemAlreadyExistsException");
        java.nio.file.Path zip = java.nio.file.Files.createTempFile("a11", ".zip");
        try {
            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
                    java.nio.file.Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("s.txt"));
                zos.write("s".getBytes("UTF-8"));
            }
            java.net.URI jarUri = new java.net.URI("jar", zip.toUri().toString(), null);
            java.nio.file.FileSystem fs =
                    java.nio.file.FileSystems.newFileSystem(jarUri, env);
            checkThrowsExact(FileSystemAlreadyExistsException.class,
                    () -> { try { java.nio.file.FileSystems.newFileSystem(jarUri, env); }
                            catch (java.io.IOException i) { throw new RuntimeException(i); } },
                    "second open of the same jar URI -> FileSystemAlreadyExistsException");
            fs.close();
            // after close the very same URI opens again cleanly
            java.nio.file.FileSystem fs2 =
                    java.nio.file.FileSystems.newFileSystem(jarUri, env);
            check(fs2.isOpen(), "reopen after close succeeds");
            fs2.close();
        } finally {
            java.nio.file.Files.deleteIfExists(zip);
        }
    }

    // ==== batch A11: extra coverage ====
    static void testContractExtra() throws Exception {
        check(new FileSystemAlreadyExistsException("m").getLocalizedMessage().equals("m"),
                "localizedMessage delegates to message");
        check(new FileSystemAlreadyExistsException("m").getSuppressed().length == 0,
                "no suppressed exceptions by default");
        FileSystemAlreadyExistsException chained = new FileSystemAlreadyExistsException("m");
        chained.initCause(new RuntimeException("why"));
        Object back = serialRoundTrip(chained);
        check(back instanceof FileSystemAlreadyExistsException && ((FileSystemAlreadyExistsException) back).getCause()
                instanceof RuntimeException && "why".equals(
                ((FileSystemAlreadyExistsException) back).getCause().getMessage()),
                "round-trip keeps chained cause");
        check("m".equals(((FileSystemAlreadyExistsException) back).getMessage()),
                "round-trip keeps message alongside cause");
    }

    // ==== batch A11: second extra coverage ====
    static void testContractExtra2() throws Exception {
        int zero = 0, one = 0;
        for (java.lang.reflect.Constructor<?> c :
                FileSystemAlreadyExistsException.class.getConstructors()) {
            if (c.getParameterCount() == 0) zero++;
            if (c.getParameterCount() == 1 && c.getParameterTypes()[0] == String.class) one++;
        }
        check(zero == 1, "exactly one no-arg constructor");
        check(one == 1, "exactly one (String) constructor");
        check(new FileSystemAlreadyExistsException("dup").getLocalizedMessage().equals("dup"),
                "localizedMessage delegates to message");
        check(java.io.ObjectStreamClass.lookup(FileSystemAlreadyExistsException.class)
                .getSerialVersionUID() == -5438419127181131148L,
                "serialVersionUID constant re-check");
        Object back = serialRoundTrip(new FileSystemAlreadyExistsException("dup"));
        check(back.getClass().getName().equals("java.nio.file.FileSystemAlreadyExistsException"),
                "round-trip exact class name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractHierarchyAndCtors();
        testContractSerialization();
        testContractRealTrigger();
        testContractExtra();
        testContractExtra2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
