/*
 * @test
 * @summary java.nio.channels.ClosedChannelException conformance: white-box structure + black-box behavior.
 * @run main ClosedChannelExceptionTest
 */

import java.nio.channels.ClosedChannelException;

public class ClosedChannelExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.io.IOException.class.isAssignableFrom(ClosedChannelException.class), "hierarchy: extends java.io.IOException");
        check(new ClosedChannelException() != null, "no-arg constructor");
        check(new ClosedChannelException().getMessage() == null, "no-arg message is null");
        check(new ClosedChannelException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("c.bin");
            java.nio.file.Files.write(f, new byte[] {1});
            java.nio.channels.SeekableByteChannel ch =
                java.nio.file.Files.newByteChannel(f, java.nio.file.StandardOpenOption.READ);
            ch.close();
            try {
                ch.read(java.nio.ByteBuffer.allocate(1));
                check(false, "read after close should throw");
            } catch (ClosedChannelException e) {
                passed++;
            }
            // thrown as the cause of AsynchronousCloseException family
            check(new java.nio.channels.AsynchronousCloseException()
                  instanceof ClosedChannelException, "family relationship");
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("c.bin"));
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
        check(ClosedChannelException.class.getSuperclass() == java.io.IOException.class,
                "direct superclass is IOException (javap-verified on JDK-8 and JDK-27)");
        check(java.io.IOException.class.isAssignableFrom(ClosedChannelException.class), "assignable to IOException");
        check(java.io.IOException.class.isAssignableFrom(ClosedChannelException.class), "assignable to IOException");
        check(java.lang.Throwable.class.isAssignableFrom(ClosedChannelException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(ClosedChannelException.class), "Serializable through Throwable");
        check(new ClosedChannelException() instanceof java.io.IOException, "instanceof IOException");
        check(ClosedChannelException.class.getName().equals("java.nio.channels.ClosedChannelException"), "fully-qualified name");
        check(java.nio.channels.AsynchronousCloseException.class.getSuperclass()
                        == java.nio.channels.ClosedChannelException.class,
                "AsynchronousCloseException extends ClosedChannelException (javap-verified)");
        check(new java.io.IOException() instanceof java.lang.Exception, "IOException branch of the hierarchy");
    }

    static void testConstructorContract() {
        ClosedChannelException e = new ClosedChannelException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = ClosedChannelException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.ClosedChannelException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("ClosedChannelException"), "toString() contains the simple class name");
        ClosedChannelException other = new ClosedChannelException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ClosedChannelException.class).getSerialVersionUID() == 882777185433553857L,
                "serialVersionUID == 882777185433553857L (identical on JDK-8 and JDK-27, probed)");
        ClosedChannelException original = new ClosedChannelException();
        ClosedChannelException back = (ClosedChannelException) serializeRoundTrip(original);
        check(back.getClass() == ClosedChannelException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        ClosedChannelException e = new ClosedChannelException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        ClosedChannelException selfCause = new ClosedChannelException();
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
        ClosedChannelException thrown = new ClosedChannelException();
        try {
            throw thrown;
        } catch (ClosedChannelException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world triggers: operations on closed channels (temp file + loopback) ----
    static void testRealTriggerClosedOperations() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a8cce");
        try {
            java.nio.file.Path f = dir.resolve("data.bin");
            java.nio.file.Files.write(f, new byte[]{1, 2, 3});
            java.nio.channels.FileChannel fc = java.nio.channels.FileChannel.open(f,
                    java.nio.file.StandardOpenOption.READ, java.nio.file.StandardOpenOption.WRITE);
            fc.close();
            check(!fc.isOpen(), "file channel closed");
            fc.close();
            check(true, "FileChannel.close() is idempotent");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> fc.read(java.nio.ByteBuffer.allocate(1)),
                    "read() on closed FileChannel -> ClosedChannelException");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> fc.position(0),
                    "position() on closed FileChannel -> ClosedChannelException");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> fc.tryLock(0, 1, false),
                    "tryLock() on closed FileChannel -> ClosedChannelException");
            java.nio.file.Files.deleteIfExists(f);
        } finally {
            rmTempDir(dir);
        }
        java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open();
        sc.close();
        checkThrows(java.nio.channels.ClosedChannelException.class,
                () -> sc.connect(new java.net.InetSocketAddress(java.net.InetAddress.getLoopbackAddress(), 1)),
                "connect() on closed SocketChannel -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                () -> sc.read(java.nio.ByteBuffer.allocate(1)),
                "read() on closed SocketChannel -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                () -> sc.write(java.nio.ByteBuffer.wrap(new byte[]{1})),
                "write() on closed SocketChannel -> ClosedChannelException");
        java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open();
        ssc.bind(null);
        ssc.close();
        checkThrows(java.nio.channels.ClosedChannelException.class, () -> ssc.accept(),
                "accept() on closed ServerSocketChannel -> ClosedChannelException");
        try (java.nio.channels.Selector sel = java.nio.channels.Selector.open()) {
            java.nio.channels.SocketChannel sc2 = java.nio.channels.SocketChannel.open();
            sc2.configureBlocking(false);
            sc2.close();
            checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> sc2.register(sel, java.nio.channels.SelectionKey.OP_READ),
                    "register() on a closed channel -> ClosedChannelException");
        }
        check(new java.nio.channels.AsynchronousCloseException()
                instanceof java.nio.channels.ClosedChannelException,
                "AsynchronousCloseException is-a ClosedChannelException (family, javap-verified)");
        check(new java.nio.channels.ClosedByInterruptException()
                instanceof java.nio.channels.ClosedChannelException,
                "ClosedByInterruptException is-a ClosedChannelException (family, javap-verified)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerClosedOperations();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
