/*
 * @test
 * @summary java.nio.channels.AlreadyBoundException conformance: hierarchy and constructor.
 * @run main AlreadyBoundExceptionTest
 */

import java.nio.channels.AlreadyBoundException;

public class AlreadyBoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAlreadyBoundException() {
        check(IllegalStateException.class.isAssignableFrom(AlreadyBoundException.class), "extends IllegalStateException");
        AlreadyBoundException e = new AlreadyBoundException();
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
        check(AlreadyBoundException.class.getSuperclass() == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalStateException.class.isAssignableFrom(AlreadyBoundException.class), "assignable to IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(AlreadyBoundException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(AlreadyBoundException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(AlreadyBoundException.class), "Serializable through Throwable");
        check(new AlreadyBoundException() instanceof java.lang.IllegalStateException, "instanceof IllegalStateException");
        check(AlreadyBoundException.class.getName().equals("java.nio.channels.AlreadyBoundException"), "fully-qualified name");
        check(!java.io.IOException.class.isAssignableFrom(AlreadyBoundException.class), "unchecked: not an IOException");
    }

    static void testConstructorContract() {
        AlreadyBoundException e = new AlreadyBoundException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = AlreadyBoundException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.AlreadyBoundException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("AlreadyBoundException"), "toString() contains the simple class name");
        AlreadyBoundException other = new AlreadyBoundException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(AlreadyBoundException.class).getSerialVersionUID() == 6796072983322737592L,
                "serialVersionUID == 6796072983322737592L (identical on JDK-8 and JDK-27, probed)");
        AlreadyBoundException original = new AlreadyBoundException();
        AlreadyBoundException back = (AlreadyBoundException) serializeRoundTrip(original);
        check(back.getClass() == AlreadyBoundException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        AlreadyBoundException e = new AlreadyBoundException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        AlreadyBoundException selfCause = new AlreadyBoundException();
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
        AlreadyBoundException thrown = new AlreadyBoundException();
        try {
            throw thrown;
        } catch (AlreadyBoundException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: ServerSocketChannel.bind() twice (loopback, ephemeral ports) ----
    static void testRealTriggerDoubleBind() throws Exception {
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            java.net.InetSocketAddress bound =
                    (java.net.InetSocketAddress) ssc.bind(null).getLocalAddress();
            check(bound != null && bound.getPort() > 0, "first bind(null) takes an ephemeral port");
            checkThrows(java.nio.channels.AlreadyBoundException.class, () -> ssc.bind(null),
                    "second bind(null) -> AlreadyBoundException");
            checkThrows(java.nio.channels.AlreadyBoundException.class,
                    () -> ssc.bind(new java.net.InetSocketAddress(java.net.InetAddress.getLoopbackAddress(), 0)),
                    "second bind with an explicit address -> AlreadyBoundException");
            check(ssc.isOpen(), "channel still open after AlreadyBoundException");
            java.net.InetSocketAddress stillBound = (java.net.InetSocketAddress) ssc.getLocalAddress();
            check(stillBound.getPort() == bound.getPort(), "original binding survives the failed rebind");
            ssc.configureBlocking(false);
            checkThrows(java.nio.channels.AlreadyBoundException.class, () -> ssc.bind(null),
                    "double bind in non-blocking mode -> AlreadyBoundException");
        }
    }

    public static void main(String[] args) throws Exception {
        testAlreadyBoundException();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerDoubleBind();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
