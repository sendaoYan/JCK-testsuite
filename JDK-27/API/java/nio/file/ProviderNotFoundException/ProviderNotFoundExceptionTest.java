/*
 * @test
 * @summary java.nio.file.ProviderNotFoundException conformance: white-box structure + black-box behavior.
 * @run main ProviderNotFoundExceptionTest
 */

import java.nio.file.ProviderNotFoundException;

public class ProviderNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.RuntimeException.class.isAssignableFrom(ProviderNotFoundException.class), "hierarchy: extends UnsupportedOperationException");

        check(new ProviderNotFoundException() != null, "no-arg ctor");
        check(new ProviderNotFoundException("m").getMessage().equals("m"), "message ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        boolean threw = false;
        try {
            java.nio.file.FileSystems.newFileSystem(
                    new java.net.URI("jck-nosuch://host/"), java.util.Map.of());
        } catch (ProviderNotFoundException e) {
            threw = true;
        } catch (java.net.URISyntaxException e) {
            throw new AssertionError("bad test URI", e);
        }
        check(threw, "unknown scheme -> ProviderNotFoundException");
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

    // ==== batch A11: constructors / serialization / unknown-scheme triggers ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(ProviderNotFoundException.class.getSuperclass()
                == java.lang.RuntimeException.class,
                "direct superclass is RuntimeException");
        check(java.io.Serializable.class.isAssignableFrom(ProviderNotFoundException.class),
                "Serializable");
        check(ProviderNotFoundException.class.getConstructors().length == 2,
                "2 public constructors: no-arg and (String)");
        check(new ProviderNotFoundException().getMessage() == null, "no-arg message null");
        check("m".equals(new ProviderNotFoundException("m").getMessage()),
                "message ctor stores message");
        check("java.nio.file.ProviderNotFoundException: m"
                .equals(new ProviderNotFoundException("m").toString()),
                "toString with message");
        check("java.nio.file.ProviderNotFoundException"
                .equals(new ProviderNotFoundException().toString()), "toString bare form");
        ProviderNotFoundException e = new ProviderNotFoundException();
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(ProviderNotFoundException.class)
                .getSerialVersionUID() == -1880012509822920354L,
                "serialVersionUID == -1880012509822920354");
        Object back = serialRoundTrip(new ProviderNotFoundException("m"));
        check(back instanceof ProviderNotFoundException, "round-trip preserves class");
        check("m".equals(((ProviderNotFoundException) back).getMessage()),
                "round-trip keeps message");
    }

    static void testContractRealTrigger() throws Exception {
        java.util.Map<String, Object> env = new java.util.HashMap<String, Object>();
        final java.net.URI unknown = new java.net.URI("jck-nosuch://host/");
        boolean threw = false;
        try {
            java.nio.file.FileSystems.newFileSystem(unknown, env);
            check(false, "unknown scheme must be refused");
        } catch (ProviderNotFoundException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("jck-nosuch"),
                    "message names the missing scheme");
        }
        check(threw, "newFileSystem(unknown scheme) -> ProviderNotFoundException");
        // getFileSystem over an unknown scheme is ProviderNotFound too (scheme lookup
        // precedes the open-fs lookup), distinguishing it from FileSystemNotFound
        checkThrowsExact(ProviderNotFoundException.class,
                () -> { try { java.nio.file.FileSystems.getFileSystem(
                            new java.net.URI("bogus://x")); }
                        catch (java.net.URISyntaxException u) {
                            throw new RuntimeException(u); } },
                "getFileSystem(unknown scheme) -> ProviderNotFoundException");
        boolean hasFile = false;
        for (java.nio.file.spi.FileSystemProvider p :
                java.nio.file.spi.FileSystemProvider.installedProviders()) {
            if (p.getScheme().equals("file")) hasFile = true;
        }
        check(hasFile, "installedProviders always contains the 'file' provider (control)");
    }

    // ==== batch A11: extra coverage ====
    static void testContractExtra() throws Exception {
        check(new ProviderNotFoundException("m").getLocalizedMessage().equals("m"),
                "localizedMessage delegates to message");
        check(new ProviderNotFoundException("m").getSuppressed().length == 0,
                "no suppressed exceptions by default");
        ProviderNotFoundException chained = new ProviderNotFoundException("m");
        chained.initCause(new RuntimeException("why"));
        Object back = serialRoundTrip(chained);
        check(back instanceof ProviderNotFoundException && ((ProviderNotFoundException) back).getCause()
                instanceof RuntimeException && "why".equals(
                ((ProviderNotFoundException) back).getCause().getMessage()),
                "round-trip keeps chained cause");
        check("m".equals(((ProviderNotFoundException) back).getMessage()),
                "round-trip keeps message alongside cause");
    }

    // ==== batch A11: second extra coverage ====
    static void testContractExtra2() throws Exception {
        int zero = 0, one = 0;
        for (java.lang.reflect.Constructor<?> c :
                ProviderNotFoundException.class.getConstructors()) {
            if (c.getParameterCount() == 0) zero++;
            if (c.getParameterCount() == 1 && c.getParameterTypes()[0] == String.class) one++;
        }
        check(zero == 1, "exactly one no-arg constructor");
        check(one == 1, "exactly one (String) constructor");
        Object back = serialRoundTrip(new ProviderNotFoundException());
        check(back instanceof ProviderNotFoundException
                && ((ProviderNotFoundException) back).getMessage() == null,
                "round-trip of no-arg keeps null message");
        boolean hasJar = false;
        for (java.nio.file.spi.FileSystemProvider p :
                java.nio.file.spi.FileSystemProvider.installedProviders()) {
            if (p.getScheme().equals("jar")) hasJar = true;
        }
        check(hasJar, "a zip/jar provider is installed on this JDK (measured)");
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
