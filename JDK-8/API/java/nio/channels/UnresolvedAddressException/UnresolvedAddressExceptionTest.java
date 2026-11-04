/*
 * @test
 * @summary java.nio.channels.UnresolvedAddressException conformance: hierarchy and constructor.
 * @run main UnresolvedAddressExceptionTest
 */

import java.nio.channels.UnresolvedAddressException;

public class UnresolvedAddressExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnresolvedAddressException() {
        check(IllegalArgumentException.class.isAssignableFrom(UnresolvedAddressException.class), "extends IllegalArgumentException");
        UnresolvedAddressException e = new UnresolvedAddressException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalArgumentException, "instanceof IllegalArgumentException");
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
        check(UnresolvedAddressException.class.getSuperclass() == java.lang.IllegalArgumentException.class,
                "direct superclass is IllegalArgumentException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(UnresolvedAddressException.class), "assignable to IllegalArgumentException");
        check(java.lang.RuntimeException.class.isAssignableFrom(UnresolvedAddressException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(UnresolvedAddressException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(UnresolvedAddressException.class), "Serializable through Throwable");
        check(new UnresolvedAddressException() instanceof java.lang.IllegalArgumentException, "instanceof IllegalArgumentException");
        check(UnresolvedAddressException.class.getName().equals("java.nio.channels.UnresolvedAddressException"), "fully-qualified name");
        check(java.lang.IllegalArgumentException.class.getSuperclass() == java.lang.RuntimeException.class,
                "chain: IllegalArgumentException extends RuntimeException");
    }

    static void testConstructorContract() {
        UnresolvedAddressException e = new UnresolvedAddressException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = UnresolvedAddressException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.UnresolvedAddressException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("UnresolvedAddressException"), "toString() contains the simple class name");
        UnresolvedAddressException other = new UnresolvedAddressException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(UnresolvedAddressException.class).getSerialVersionUID() == 6136959093620794148L,
                "serialVersionUID == 6136959093620794148L (identical on JDK-8 and JDK-27, probed)");
        UnresolvedAddressException original = new UnresolvedAddressException();
        UnresolvedAddressException back = (UnresolvedAddressException) serializeRoundTrip(original);
        check(back.getClass() == UnresolvedAddressException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        UnresolvedAddressException e = new UnresolvedAddressException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        UnresolvedAddressException selfCause = new UnresolvedAddressException();
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
        UnresolvedAddressException thrown = new UnresolvedAddressException();
        try {
            throw thrown;
        } catch (UnresolvedAddressException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: unresolved socket addresses (no DNS lookup involved) ----
    static void testRealTriggerUnresolvedAddress() throws Exception {
        final java.net.InetSocketAddress unresolved =
                java.net.InetSocketAddress.createUnresolved("nosuchhost.invalid", 81);
        check(unresolved.isUnresolved(), "createUnresolved() yields an unresolved address (no DNS lookup)");
        check(unresolved.getAddress() == null, "unresolved address carries no InetAddress");
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(new java.net.InetSocketAddress(lo, 0));
            java.net.InetSocketAddress addr = (java.net.InetSocketAddress) ssc.getLocalAddress();
            try (java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open()) {
                checkThrows(java.nio.channels.UnresolvedAddressException.class, () -> sc.connect(unresolved),
                        "blocking connect() to an unresolved address -> UnresolvedAddressException");
                sc.configureBlocking(false);
                checkThrows(java.nio.channels.UnresolvedAddressException.class, () -> sc.connect(unresolved),
                        "non-blocking connect() to an unresolved address -> UnresolvedAddressException");
                checkThrows(java.nio.channels.UnresolvedAddressException.class, () -> sc.bind(unresolved),
                        "bind() to an unresolved address -> UnresolvedAddressException");
                check(sc.isOpen(), "channel still open after the refused attempts");
            }
            try (java.nio.channels.SocketChannel sc2 = java.nio.channels.SocketChannel.open(addr);
                 java.nio.channels.SocketChannel peer = ssc.accept()) {
                check(sc2.isConnected(), "contrast: resolved loopback address connects fine");
            }
        }
        try (java.nio.channels.ServerSocketChannel ssc2 = java.nio.channels.ServerSocketChannel.open()) {
            checkThrows(java.nio.channels.UnresolvedAddressException.class, () -> ssc2.bind(unresolved),
                    "ServerSocketChannel.bind() to an unresolved address -> UnresolvedAddressException");
        }
        try (java.nio.channels.DatagramChannel dc = java.nio.channels.DatagramChannel.open()) {
            checkThrows(java.nio.channels.UnresolvedAddressException.class, () -> dc.connect(unresolved),
                    "DatagramChannel.connect() to an unresolved address -> UnresolvedAddressException");
        }
    }

    public static void main(String[] args) throws Exception {
        testUnresolvedAddressException();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerUnresolvedAddress();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
