/*
 * @test
 * @summary java.nio.file.AtomicMoveNotSupportedException conformance: hierarchy and constructor.
 * @run main AtomicMoveNotSupportedExceptionTest
 */

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystemException;

public class AtomicMoveNotSupportedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAtomicMoveNotSupportedException() {
        check(FileSystemException.class.isAssignableFrom(AtomicMoveNotSupportedException.class), "extends FileSystemException");
        AtomicMoveNotSupportedException e = new AtomicMoveNotSupportedException("f", "o", "r");
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

    // ==== batch A11: constructor / getter / serialization / cross-store trigger ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(AtomicMoveNotSupportedException.class.getSuperclass()
                == java.nio.file.FileSystemException.class,
                "direct superclass is FileSystemException");
        check(java.io.IOException.class.isAssignableFrom(AtomicMoveNotSupportedException.class),
                "caught as IOException");
        check(java.io.Serializable.class.isAssignableFrom(AtomicMoveNotSupportedException.class),
                "Serializable");
        check(AtomicMoveNotSupportedException.class.getConstructors().length == 1,
                "single public 3-arg constructor only");
        AtomicMoveNotSupportedException e =
                new AtomicMoveNotSupportedException("s", "t", "why");
        check("s".equals(e.getFile()) && "t".equals(e.getOtherFile())
                && "why".equals(e.getReason()), "3-arg getters round-trip");
        check("s -> t: why".equals(e.getMessage()), "message composition 's -> t: why'");
        check("java.nio.file.AtomicMoveNotSupportedException: s -> t: why".equals(e.toString()),
                "toString exact");
        AtomicMoveNotSupportedException n =
                new AtomicMoveNotSupportedException(null, null, null);
        check(n.getFile() == null && n.getOtherFile() == null && n.getReason() == null,
                "all-null arguments accepted");
        check(n.getMessage() == null, "all-null -> null message");
    }

    static void testContractCauseAndSerialization() throws Exception {
        AtomicMoveNotSupportedException e = new AtomicMoveNotSupportedException("s", "t", "w");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(AtomicMoveNotSupportedException.class)
                .getSerialVersionUID() == 5402760225333135579L,
                "serialVersionUID == 5402760225333135579");
        Object back = serialRoundTrip(e);
        check(back instanceof AtomicMoveNotSupportedException,
                "round-trip preserves subclass identity");
        AtomicMoveNotSupportedException r = (AtomicMoveNotSupportedException) back;
        check("s".equals(r.getFile()) && "t".equals(r.getOtherFile()) && "w".equals(r.getReason()),
                "round-trip keeps getters");
        check("s -> t: w".equals(r.getMessage()), "round-trip message");
    }

    static void testContractRealTrigger() throws Exception {
        // /tmp (ext4) and /dev/shm (tmpfs) are different FileStores on this host (measured);
        // EXDEV surfaces as AtomicMoveNotSupportedException only under ATOMIC_MOVE
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        java.nio.file.Path shm = java.nio.file.Paths.get("/dev/shm");
        java.nio.file.FileStore s1 = java.nio.file.Files.getFileStore(dir);
        java.nio.file.FileStore s2 = java.nio.file.Files.getFileStore(shm);
        if (s1.equals(s2)) {
            System.out.println("NOTE: tmp and /dev/shm share a FileStore; trigger skipped");
            return;
        }
        java.nio.file.Path shmOut = null;
        try {
            java.nio.file.Path src = dir.resolve("am.txt");
            java.nio.file.Files.write(src, "m".getBytes("UTF-8"));
            // control: same-store rename with ATOMIC_MOVE succeeds
            java.nio.file.Path sameStore = dir.resolve("am2.txt");
            java.nio.file.Files.move(src, sameStore, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            check(java.nio.file.Files.exists(sameStore) && !java.nio.file.Files.exists(src),
                    "same-store ATOMIC_MOVE is a plain rename (succeeds)");
            shmOut = shm.resolve("a11-" + System.nanoTime() + ".txt");
            try {
                java.nio.file.Files.move(sameStore, shmOut,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
                check(false, "cross-store ATOMIC_MOVE must fail");
            } catch (AtomicMoveNotSupportedException e) {
                check(sameStore.toString().equals(e.getFile()), "getFile is the source");
                check(shmOut.toString().equals(e.getOtherFile()), "getOtherFile is the target");
                check(e.getReason() != null, "errno reason present (EXDEV)");
                check(e.getMessage().equals(sameStore.toString() + " -> " + shmOut.toString()
                        + ": " + e.getReason()), "message 'src -> dst: reason'");
            }
        } finally {
            if (shmOut != null) java.nio.file.Files.deleteIfExists(shmOut);
            deleteTree(dir);
        }
    }

    // ==== batch A11: second extra coverage ====
    static void testContractExtra2() throws Exception {
        check(java.io.ObjectStreamClass.lookup(AtomicMoveNotSupportedException.class)
                .getSerialVersionUID() == java.io.ObjectStreamClass.lookup(
                        AtomicMoveNotSupportedException.class).getSerialVersionUID(),
                "serialVersionUID stable across lookups");
        AtomicMoveNotSupportedException e = new AtomicMoveNotSupportedException("s", "t", "why");
        check(e.getMessage().endsWith(": " + e.getReason()),
                "message ends with ': reason'");
        java.lang.reflect.Constructor<?> c =
                AtomicMoveNotSupportedException.class.getConstructors()[0];
        check(c.getParameterCount() == 3, "single constructor takes 3 Strings");
        boolean allStrings = true;
        for (Class<?> t : c.getParameterTypes()) if (t != String.class) allStrings = false;
        check(allStrings, "constructor signature is (String,String,String)");
        Object back = serialRoundTrip(e);
        check(back instanceof java.io.IOException,
                "round-trip survives as an IOException on the wire");
        check(((AtomicMoveNotSupportedException) back).getCause() == null,
                "round-trip keeps null cause");
    }

    public static void main(String[] args) throws Exception {
        testAtomicMoveNotSupportedException();
        testContractHierarchyAndCtors();
        testContractCauseAndSerialization();
        testContractRealTrigger();
        testContractExtra2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
