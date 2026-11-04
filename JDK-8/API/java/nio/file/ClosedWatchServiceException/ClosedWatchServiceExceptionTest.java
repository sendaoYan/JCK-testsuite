/*
 * @test
 * @summary java.nio.file.ClosedWatchServiceException conformance: hierarchy and constructor.
 * @run main ClosedWatchServiceExceptionTest
 */

import java.nio.file.ClosedWatchServiceException;

public class ClosedWatchServiceExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClosedWatchServiceException() {
        check(IllegalStateException.class.isAssignableFrom(ClosedWatchServiceException.class), "extends IllegalStateException");
        ClosedWatchServiceException e = new ClosedWatchServiceException();
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
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

    // ==== batch A11: constructor / serialization / closed-service trigger ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(ClosedWatchServiceException.class.getSuperclass()
                == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException");
        check(RuntimeException.class.isAssignableFrom(ClosedWatchServiceException.class),
                "caught as RuntimeException");
        check(java.io.Serializable.class.isAssignableFrom(ClosedWatchServiceException.class),
                "Serializable");
        check(ClosedWatchServiceException.class.getConstructors().length == 1,
                "single public no-arg constructor");
        ClosedWatchServiceException e = new ClosedWatchServiceException();
        check(e.getMessage() == null, "no-arg message null");
        check("java.nio.file.ClosedWatchServiceException".equals(e.toString()),
                "toString bare (no colon suffix)");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(ClosedWatchServiceException.class)
                .getSerialVersionUID() == 1853336266231677732L,
                "serialVersionUID == 1853336266231677732");
        Object back = serialRoundTrip(new ClosedWatchServiceException());
        check(back instanceof ClosedWatchServiceException, "round-trip preserves class");
        check(((ClosedWatchServiceException) back).getMessage() == null,
                "round-trip keeps null message");
    }

    static void testContractRealTrigger() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        java.nio.file.WatchService ws =
                java.nio.file.FileSystems.getDefault().newWatchService();
        try {
            java.nio.file.WatchKey key = dir.register(ws,
                    java.nio.file.StandardWatchEventKinds.ENTRY_CREATE);
            check(key.isValid(), "key valid while service open");
            ws.close();
            checkThrowsExact(ClosedWatchServiceException.class, () -> ws.poll(),
                    "poll() after close -> ClosedWatchServiceException");
            checkThrowsExact(ClosedWatchServiceException.class,
                    () -> { try { ws.poll(1, java.util.concurrent.TimeUnit.SECONDS); }
                            catch (InterruptedException ie) { throw new RuntimeException(ie); } },
                    "poll(timeout) after close -> ClosedWatchServiceException");
            checkThrowsExact(ClosedWatchServiceException.class,
                    () -> { try { ws.take(); }
                            catch (InterruptedException ie) { throw new RuntimeException(ie); } },
                    "take() after close -> ClosedWatchServiceException");
            checkThrowsExactIO(ClosedWatchServiceException.class,
                    () -> dir.register(ws, java.nio.file.StandardWatchEventKinds.ENTRY_CREATE),
                    "register after close -> ClosedWatchServiceException");
            check(!key.isValid(), "keys die with the service");
        } finally {
            try { ws.close(); } catch (Exception ignore) { }
            deleteTree(dir);
        }
    }

    // ==== batch A11: extra coverage ====
    static void testContractExtra() throws Exception {
        check(new ClosedWatchServiceException().getLocalizedMessage() == null,
                "localizedMessage null when message null");
        check(new ClosedWatchServiceException().getSuppressed().length == 0,
                "no suppressed exceptions by default");
        check(new ClosedWatchServiceException().fillInStackTrace().getClass() == ClosedWatchServiceException.class,
                "fillInStackTrace keeps exact type");
        Object back = serialRoundTrip(new ClosedWatchServiceException());
        check(back.getClass().getName().equals("java.nio.file.ClosedWatchServiceException"),
                "round-trip exact class name");
    }

    // ==== batch A11: second extra coverage ====
    static void testContractRealTrigger2() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        java.nio.file.WatchService ws =
                java.nio.file.FileSystems.getDefault().newWatchService();
        try {
            java.nio.file.WatchKey key = dir.register(ws,
                    java.nio.file.StandardWatchEventKinds.ENTRY_CREATE);
            check(key != null, "register returns a key while open");
            check(key.isValid(), "key valid before any event");
            key.cancel();
            check(!key.isValid(), "cancelled key invalid");
            check(ws.poll() == null, "poll with no events returns null (no exception)");
            check(ws.poll(1, java.util.concurrent.TimeUnit.MILLISECONDS) == null,
                    "bounded poll with no events returns null");
            ws.close();
            ws.close();
            check(true, "double close of WatchService is idempotent");
        } finally {
            try { ws.close(); } catch (Exception ignore) { }
            deleteTree(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testClosedWatchServiceException();
        testContractHierarchyAndCtors();
        testContractRealTrigger();
        testContractExtra();
        testContractRealTrigger2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
