/*
 * @test
 * @summary java.nio.channels.NonWritableChannelException conformance: white-box structure + black-box behavior.
 * @run main NonWritableChannelExceptionTest
 */

import java.nio.channels.NonWritableChannelException;

public class NonWritableChannelExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(NonWritableChannelException.class), "hierarchy: extends IllegalStateException");
        check(new NonWritableChannelException() != null, "no-arg constructor");
        check(new NonWritableChannelException().getMessage() == null, "no-arg message is null");
        check(new NonWritableChannelException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("nw.bin");
            java.nio.file.Files.write(f, new byte[] {1});
            try (java.nio.channels.FileChannel ch = java.nio.channels.FileChannel.open(
                    f, java.nio.file.StandardOpenOption.READ)) {
                try {
                    ch.write(java.nio.ByteBuffer.wrap(new byte[] {2}));
                    check(false, "write on read-only should throw");
                } catch (NonWritableChannelException e) {
                    passed++;
                }
            }
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("nw.bin"));
            java.nio.file.Files.deleteIfExists(dir);
        }
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
        check(NonWritableChannelException.class.getSuperclass() == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalStateException.class.isAssignableFrom(NonWritableChannelException.class), "assignable to IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(NonWritableChannelException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(NonWritableChannelException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(NonWritableChannelException.class), "Serializable through Throwable");
        check(new NonWritableChannelException() instanceof java.lang.IllegalStateException, "instanceof IllegalStateException");
        check(NonWritableChannelException.class.getName().equals("java.nio.channels.NonWritableChannelException"), "fully-qualified name");
        check(!java.io.IOException.class.isAssignableFrom(NonWritableChannelException.class), "unchecked: not an IOException");
    }

    static void testConstructorContract() {
        NonWritableChannelException e = new NonWritableChannelException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = NonWritableChannelException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.NonWritableChannelException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("NonWritableChannelException"), "toString() contains the simple class name");
        NonWritableChannelException other = new NonWritableChannelException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(NonWritableChannelException.class).getSerialVersionUID() == -7071230488279011621L,
                "serialVersionUID == -7071230488279011621L (identical on JDK-8 and JDK-27, probed)");
        NonWritableChannelException original = new NonWritableChannelException();
        NonWritableChannelException back = (NonWritableChannelException) serializeRoundTrip(original);
        check(back.getClass() == NonWritableChannelException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        NonWritableChannelException e = new NonWritableChannelException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        NonWritableChannelException selfCause = new NonWritableChannelException();
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
        NonWritableChannelException thrown = new NonWritableChannelException();
        try {
            throw thrown;
        } catch (NonWritableChannelException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: write-side operations on a read-only FileChannel (temp file) ----
    static void testRealTriggerReadOnlyChannel() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a8nwr");
        try {
            java.nio.file.Path f = dir.resolve("r.bin");
            java.nio.file.Files.write(f, new byte[]{1, 2, 3, 4});
            try (java.nio.channels.FileChannel ch = java.nio.channels.FileChannel.open(
                    f, java.nio.file.StandardOpenOption.READ)) {
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> ch.write(java.nio.ByteBuffer.wrap(new byte[]{8})),
                        "relative write() on read-only channel -> NonWritableChannelException");
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> ch.write(java.nio.ByteBuffer.wrap(new byte[]{8}), 0L),
                        "absolute write(buf, pos) on read-only channel -> NonWritableChannelException");
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> ch.write(new java.nio.ByteBuffer[]{java.nio.ByteBuffer.wrap(new byte[]{8})}, 0, 1),
                        "gathering write(bufs, off, len) on read-only channel -> NonWritableChannelException");
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> ch.truncate(2),
                        "truncate() on read-only channel -> NonWritableChannelException");
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> ch.map(java.nio.channels.FileChannel.MapMode.READ_WRITE, 0, 1),
                        "READ_WRITE map on read-only channel -> NonWritableChannelException");
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> ch.tryLock(0, 2, false),
                        "exclusive tryLock on read-only channel -> NonWritableChannelException (needs write access)");
                check(ch.tryLock(0, 2, true) != null, "shared tryLock on read-only channel works");
                check(ch.read(java.nio.ByteBuffer.allocate(4)) == 4,
                        "read() on read-only channel works normally");
                ch.force(false);
                check(true, "force(false) on a read-only channel does not throw");
            }
        } finally {
            rmTempDir(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerReadOnlyChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
