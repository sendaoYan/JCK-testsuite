/*
 * @test
 * @summary java.nio.channels.OverlappingFileLockException conformance: hierarchy and constructor.
 * @run main OverlappingFileLockExceptionTest
 */

import java.nio.channels.OverlappingFileLockException;

public class OverlappingFileLockExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOverlappingFileLockException() {
        check(IllegalStateException.class.isAssignableFrom(OverlappingFileLockException.class), "extends IllegalStateException");
        OverlappingFileLockException e = new OverlappingFileLockException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }



    // =====================================================================
    // Batch A8 deepening: contract-level coverage. Every behavioural fact
    // below was probed on JDK-8 (8u372) and JDK-27 (27.0.1) beforehand;
    // both sides behave identically except where explicitly commented.
    // =====================================================================

    /** Asserts that {@code action} throws exactly {@code expected} (no subclass tolerance). */
    static <T extends Throwable> T checkThrows(Class<T> expected, java.util.concurrent.Callable<?> action, String what) {
        try {
            action.call();
        } catch (Throwable t) {
            if (t.getClass() != expected)
                throw new AssertionError(what + ": expected " + expected.getName()
                        + " but got " + t.getClass().getName()
                        + (t.getMessage() != null ? " (" + t.getMessage() + ")" : ""));
            passed++;
            @SuppressWarnings("unchecked")
            T typed = (T) t;
            return typed;
        }
        throw new AssertionError(what + ": expected " + expected.getName() + " but nothing was thrown");
    }

    /** Serializes {@code obj} into a byte stream and reads it back. */
    static Object serializeRoundTrip(Object obj) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return ois.readObject();
        }
    }

    /** Best-effort recursive removal of a temporary directory tree (used in finally). */
    static void rmTempDir(java.nio.file.Path dir) {
        if (dir == null || !java.nio.file.Files.exists(dir)) return;
        try (java.util.stream.Stream<java.nio.file.Path> walk = java.nio.file.Files.walk(dir)) {
            walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try { java.nio.file.Files.deleteIfExists(p); } catch (java.io.IOException ignore) { }
            });
        } catch (java.io.IOException ignore) { }
    }

    static void testHierarchyContract() {
        check(OverlappingFileLockException.class.getSuperclass() == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalStateException.class.isAssignableFrom(OverlappingFileLockException.class), "assignable to IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(OverlappingFileLockException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(OverlappingFileLockException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(OverlappingFileLockException.class), "Serializable through Throwable");
        check(new OverlappingFileLockException() instanceof java.lang.IllegalStateException, "instanceof IllegalStateException");
        check(OverlappingFileLockException.class.getName().equals("java.nio.channels.OverlappingFileLockException"), "fully-qualified name");
        check(!java.io.IOException.class.isAssignableFrom(OverlappingFileLockException.class), "unchecked: not an IOException");
    }

    static void testConstructorContract() {
        OverlappingFileLockException e = new OverlappingFileLockException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = OverlappingFileLockException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.OverlappingFileLockException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("OverlappingFileLockException"), "toString() contains the simple class name");
        OverlappingFileLockException other = new OverlappingFileLockException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(OverlappingFileLockException.class).getSerialVersionUID() == 2047812138163068433L,
                "serialVersionUID == 2047812138163068433L (identical on JDK-8 and JDK-27, probed)");
        OverlappingFileLockException original = new OverlappingFileLockException();
        OverlappingFileLockException back = (OverlappingFileLockException) serializeRoundTrip(original);
        check(back.getClass() == OverlappingFileLockException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        OverlappingFileLockException e = new OverlappingFileLockException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        OverlappingFileLockException selfCause = new OverlappingFileLockException();
        checkThrows(java.lang.IllegalArgumentException.class, () -> selfCause.initCause(selfCause),
                "initCause(this) -> IllegalArgumentException");
        check(selfCause.getCause() == null, "self-cause attempt left the cause null");
        check(e.fillInStackTrace() == e, "fillInStackTrace() returns this");
        check(e.getStackTrace() != null && e.getStackTrace().length > 0,
                "stack trace captured at construction");
        e.addSuppressed(new IllegalStateException("suppressed-1"));
        check(e.getSuppressed().length == 1, "addSuppressed() is visible");
        check("suppressed-1".equals(e.getSuppressed()[0].getMessage()), "suppressed content preserved");
        check(e.getMessage() == null, "cause/suppressed manipulation does not fabricate a message");
        OverlappingFileLockException thrown = new OverlappingFileLockException();
        try {
            throw thrown;
        } catch (OverlappingFileLockException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: overlapping file locks inside one JVM (temp file) ----
    static void testRealTriggerOverlappingLocks() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a8ovl");
        try {
            java.nio.file.Path f = dir.resolve("lock.bin");
            java.nio.file.Files.write(f, new byte[64]);
            try (java.nio.channels.FileChannel c1 = java.nio.channels.FileChannel.open(f,
                         java.nio.file.StandardOpenOption.READ, java.nio.file.StandardOpenOption.WRITE);
                 java.nio.channels.FileChannel c2 = java.nio.channels.FileChannel.open(f,
                         java.nio.file.StandardOpenOption.READ, java.nio.file.StandardOpenOption.WRITE)) {
                java.nio.channels.FileLock first = c1.tryLock(0, 10, false);
                check(first != null && first.isValid(), "first lock (0,10) acquired");
                check(first.channel() == c1, "lock belongs to the acquiring channel");
                checkThrows(java.nio.channels.OverlappingFileLockException.class, () -> c2.tryLock(5, 10, false),
                        "overlapping tryLock through a second channel of the same file -> OverlappingFileLockException");
                checkThrows(java.nio.channels.OverlappingFileLockException.class, () -> c2.lock(5, 10, false),
                        "overlapping blocking lock() through a second channel -> OverlappingFileLockException");
                checkThrows(java.nio.channels.OverlappingFileLockException.class, () -> c1.tryLock(5, 10, false),
                        "overlapping tryLock on the same channel -> OverlappingFileLockException");
                checkThrows(java.nio.channels.OverlappingFileLockException.class, () -> c1.tryLock(5, 0, false),
                        "zero-length lock strictly inside the held region overlaps");
                java.nio.channels.FileLock zeroLen = c1.tryLock(0, 0, false);
                check(zeroLen != null,
                        "JDK-8 semantics: zero-length lock at the region start (0,0) does not overlap held (0,10)");
                zeroLen.release();
                check(c2.tryLock(10, 10, false) != null,
                        "adjacent non-overlapping region lockable via the other channel");
                check(c1.tryLock(20, 0, false) != null,
                        "zero-length lock right past the held region is fine");
                first.release();
                check(!first.isValid(), "released lock reports invalid");
                check(c1.tryLock(5, 5, false) != null,
                        "released region can be locked again (5,10 stays clear of c2's (10,20))");
            }
        } finally {
            rmTempDir(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testOverlappingFileLockException();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerOverlappingLocks();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
