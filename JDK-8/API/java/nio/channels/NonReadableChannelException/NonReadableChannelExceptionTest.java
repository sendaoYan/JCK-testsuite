/*
 * @test
 * @summary java.nio.channels.NonReadableChannelException conformance: hierarchy and constructor.
 * @run main NonReadableChannelExceptionTest
 */

import java.nio.channels.NonReadableChannelException;

public class NonReadableChannelExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNonReadableChannelException() {
        check(IllegalStateException.class.isAssignableFrom(NonReadableChannelException.class), "extends IllegalStateException");
        NonReadableChannelException e = new NonReadableChannelException();
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
        check(NonReadableChannelException.class.getSuperclass() == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalStateException.class.isAssignableFrom(NonReadableChannelException.class), "assignable to IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(NonReadableChannelException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(NonReadableChannelException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(NonReadableChannelException.class), "Serializable through Throwable");
        check(new NonReadableChannelException() instanceof java.lang.IllegalStateException, "instanceof IllegalStateException");
        check(NonReadableChannelException.class.getName().equals("java.nio.channels.NonReadableChannelException"), "fully-qualified name");
        check(!java.io.IOException.class.isAssignableFrom(NonReadableChannelException.class), "unchecked: not an IOException");
    }

    static void testConstructorContract() {
        NonReadableChannelException e = new NonReadableChannelException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = NonReadableChannelException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.NonReadableChannelException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("NonReadableChannelException"), "toString() contains the simple class name");
        NonReadableChannelException other = new NonReadableChannelException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(NonReadableChannelException.class).getSerialVersionUID() == -3200915679294993514L,
                "serialVersionUID == -3200915679294993514L (identical on JDK-8 and JDK-27, probed)");
        NonReadableChannelException original = new NonReadableChannelException();
        NonReadableChannelException back = (NonReadableChannelException) serializeRoundTrip(original);
        check(back.getClass() == NonReadableChannelException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        NonReadableChannelException e = new NonReadableChannelException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        NonReadableChannelException selfCause = new NonReadableChannelException();
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
        NonReadableChannelException thrown = new NonReadableChannelException();
        try {
            throw thrown;
        } catch (NonReadableChannelException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: read-side operations on a write-only FileChannel (temp file) ----
    static void testRealTriggerWriteOnlyChannel() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a8nre");
        try {
            java.nio.file.Path f = dir.resolve("w.bin");
            java.nio.file.Files.write(f, new byte[]{1, 2, 3, 4});
            try (java.nio.channels.FileChannel ch = java.nio.channels.FileChannel.open(
                    f, java.nio.file.StandardOpenOption.WRITE)) {
                checkThrows(java.nio.channels.NonReadableChannelException.class,
                        () -> ch.read(java.nio.ByteBuffer.allocate(1)),
                        "relative read() on write-only channel -> NonReadableChannelException");
                checkThrows(java.nio.channels.NonReadableChannelException.class,
                        () -> ch.read(java.nio.ByteBuffer.allocate(1), 0L),
                        "absolute read(buf, pos) on write-only channel -> NonReadableChannelException");
                checkThrows(java.nio.channels.NonReadableChannelException.class,
                        () -> ch.read(new java.nio.ByteBuffer[]{java.nio.ByteBuffer.allocate(1)}, 0, 1),
                        "scattering read(bufs, off, len) on write-only channel -> NonReadableChannelException");
                checkThrows(java.nio.channels.NonReadableChannelException.class,
                        () -> ch.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, 1),
                        "READ_ONLY map on write-only channel -> NonReadableChannelException");
                checkThrows(java.nio.channels.NonReadableChannelException.class,
                        () -> ch.map(java.nio.channels.FileChannel.MapMode.PRIVATE, 0, 1),
                        "PRIVATE map on write-only channel -> NonReadableChannelException");
                checkThrows(java.nio.channels.NonReadableChannelException.class,
                        () -> ch.tryLock(0, 2, true),
                        "shared tryLock on write-only channel -> NonReadableChannelException (needs read access)");
                check(ch.tryLock(0, 2, false) != null, "exclusive tryLock on write-only channel works");
                check(ch.write(java.nio.ByteBuffer.wrap(new byte[]{9, 9})) == 2,
                        "write() on write-only channel works normally");
            }
        } finally {
            rmTempDir(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testNonReadableChannelException();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerWriteOnlyChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
