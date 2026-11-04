/*
 * @test
 * @summary java.nio.channels.UnsupportedAddressTypeException conformance: white-box structure + black-box behavior.
 * @run main UnsupportedAddressTypeExceptionTest
 */

import java.nio.channels.UnsupportedAddressTypeException;

public class UnsupportedAddressTypeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(UnsupportedAddressTypeException.class), "hierarchy: extends java.lang.IllegalArgumentException");
        check(new UnsupportedAddressTypeException() != null, "no-arg constructor");
        check(new UnsupportedAddressTypeException().getMessage() == null, "no-arg message is null");
        check(new UnsupportedAddressTypeException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        UnsupportedAddressTypeException e = new UnsupportedAddressTypeException();
        check(e.getMessage() == null, "no message");
        check(e instanceof IllegalArgumentException, "IllegalArgumentException family");
        try {
            throw e;
        } catch (UnsupportedAddressTypeException again) {
            check(again == e, "rethrow preserves identity");
        }
    }



    /** A SocketAddress type no stock channel implementation understands. */
    static final class OpaqueAddress extends java.net.SocketAddress {
        @Override public String toString() { return "OpaqueAddress"; }
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
        check(UnsupportedAddressTypeException.class.getSuperclass() == java.lang.IllegalArgumentException.class,
                "direct superclass is IllegalArgumentException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(UnsupportedAddressTypeException.class), "assignable to IllegalArgumentException");
        check(java.lang.RuntimeException.class.isAssignableFrom(UnsupportedAddressTypeException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(UnsupportedAddressTypeException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(UnsupportedAddressTypeException.class), "Serializable through Throwable");
        check(new UnsupportedAddressTypeException() instanceof java.lang.IllegalArgumentException, "instanceof IllegalArgumentException");
        check(UnsupportedAddressTypeException.class.getName().equals("java.nio.channels.UnsupportedAddressTypeException"), "fully-qualified name");
        check(java.lang.IllegalArgumentException.class.getSuperclass() == java.lang.RuntimeException.class,
                "chain: IllegalArgumentException extends RuntimeException");
    }

    static void testConstructorContract() {
        UnsupportedAddressTypeException e = new UnsupportedAddressTypeException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = UnsupportedAddressTypeException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.UnsupportedAddressTypeException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("UnsupportedAddressTypeException"), "toString() contains the simple class name");
        UnsupportedAddressTypeException other = new UnsupportedAddressTypeException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(UnsupportedAddressTypeException.class).getSerialVersionUID() == -2964323842829700493L,
                "serialVersionUID == -2964323842829700493L (identical on JDK-8 and JDK-27, probed)");
        UnsupportedAddressTypeException original = new UnsupportedAddressTypeException();
        UnsupportedAddressTypeException back = (UnsupportedAddressTypeException) serializeRoundTrip(original);
        check(back.getClass() == UnsupportedAddressTypeException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        UnsupportedAddressTypeException e = new UnsupportedAddressTypeException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        UnsupportedAddressTypeException selfCause = new UnsupportedAddressTypeException();
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
        UnsupportedAddressTypeException thrown = new UnsupportedAddressTypeException();
        try {
            throw thrown;
        } catch (UnsupportedAddressTypeException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: bind/connect with an unsupported SocketAddress type ----
    static void testRealTriggerOpaqueAddress() throws Exception {
        final java.net.SocketAddress opaque = new OpaqueAddress();
        try (java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open()) {
            checkThrows(java.nio.channels.UnsupportedAddressTypeException.class, () -> sc.bind(opaque),
                    "SocketChannel.bind() with an unsupported address type -> UnsupportedAddressTypeException");
            checkThrows(java.nio.channels.UnsupportedAddressTypeException.class, () -> sc.connect(opaque),
                    "SocketChannel.connect() with an unsupported address type -> UnsupportedAddressTypeException");
        }
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            checkThrows(java.nio.channels.UnsupportedAddressTypeException.class, () -> ssc.bind(opaque),
                    "ServerSocketChannel.bind() with an unsupported address type -> UnsupportedAddressTypeException");
        }
        try (java.nio.channels.DatagramChannel dc = java.nio.channels.DatagramChannel.open()) {
            checkThrows(java.nio.channels.UnsupportedAddressTypeException.class, () -> dc.bind(opaque),
                    "DatagramChannel.bind() with an unsupported address type -> UnsupportedAddressTypeException");
            checkThrows(java.nio.channels.UnsupportedAddressTypeException.class, () -> dc.connect(opaque),
                    "DatagramChannel.connect() with an unsupported address type -> UnsupportedAddressTypeException");
        }
        // Contrast: InetSocketAddress is supported, and null means "auto-assign"
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(null);
            check(ssc.getLocalAddress() != null, "bind(null) is legal (auto-assigned address)");
        }
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(new java.net.InetSocketAddress(java.net.InetAddress.getLoopbackAddress(), 0));
            check(ssc.getLocalAddress() != null, "bind(InetSocketAddress) is supported");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerOpaqueAddress();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
