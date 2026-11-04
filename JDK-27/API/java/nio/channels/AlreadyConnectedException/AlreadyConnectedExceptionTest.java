/*
 * @test
 * @summary java.nio.channels.AlreadyConnectedException conformance: white-box structure + black-box behavior.
 * @run main AlreadyConnectedExceptionTest
 */

import java.nio.channels.AlreadyConnectedException;

public class AlreadyConnectedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(AlreadyConnectedException.class), "hierarchy: extends IllegalStateException");
        check(new AlreadyConnectedException() != null, "no-arg constructor");
        check(new AlreadyConnectedException().getMessage() == null, "no-arg message is null");
        check(new AlreadyConnectedException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(new java.net.InetSocketAddress(lo, 0));
            java.net.InetSocketAddress addr = (java.net.InetSocketAddress) ssc.getLocalAddress();
            try (java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open(addr)) {
                check(sc.isConnected(), "connected once");
                try {
                    sc.connect(addr);
                    check(false, "second connect should throw");
                } catch (AlreadyConnectedException e) {
                    passed++;
                }
            }
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
        check(AlreadyConnectedException.class.getSuperclass() == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalStateException.class.isAssignableFrom(AlreadyConnectedException.class), "assignable to IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(AlreadyConnectedException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(AlreadyConnectedException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(AlreadyConnectedException.class), "Serializable through Throwable");
        check(new AlreadyConnectedException() instanceof java.lang.IllegalStateException, "instanceof IllegalStateException");
        check(AlreadyConnectedException.class.getName().equals("java.nio.channels.AlreadyConnectedException"), "fully-qualified name");
        check(!java.io.IOException.class.isAssignableFrom(AlreadyConnectedException.class), "unchecked: not an IOException");
    }

    static void testConstructorContract() {
        AlreadyConnectedException e = new AlreadyConnectedException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = AlreadyConnectedException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.AlreadyConnectedException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("AlreadyConnectedException"), "toString() contains the simple class name");
        AlreadyConnectedException other = new AlreadyConnectedException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(AlreadyConnectedException.class).getSerialVersionUID() == -7331895245053773357L,
                "serialVersionUID == -7331895245053773357L (identical on JDK-8 and JDK-27, probed)");
        AlreadyConnectedException original = new AlreadyConnectedException();
        AlreadyConnectedException back = (AlreadyConnectedException) serializeRoundTrip(original);
        check(back.getClass() == AlreadyConnectedException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        AlreadyConnectedException e = new AlreadyConnectedException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        AlreadyConnectedException selfCause = new AlreadyConnectedException();
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
        AlreadyConnectedException thrown = new AlreadyConnectedException();
        try {
            throw thrown;
        } catch (AlreadyConnectedException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: connect() on an already-connected channel (loopback) ----
    static void testRealTriggerReconnect() throws Exception {
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(new java.net.InetSocketAddress(lo, 0));
            java.net.InetSocketAddress addr = (java.net.InetSocketAddress) ssc.getLocalAddress();
            java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open(addr);
            try (java.nio.channels.SocketChannel peer = ssc.accept()) {
                check(sc.isConnected(), "blocking loopback connect succeeded");
                checkThrows(java.nio.channels.AlreadyConnectedException.class, () -> sc.connect(addr),
                        "connect() again on a connected channel -> AlreadyConnectedException");
                checkThrows(java.nio.channels.AlreadyConnectedException.class,
                        () -> sc.connect(new java.net.InetSocketAddress(lo, addr.getPort())),
                        "connect() again to another address object -> AlreadyConnectedException");
                check(sc.isConnected(), "still connected after the refused reconnects");
                check(sc.write(java.nio.ByteBuffer.wrap(new byte[]{7})) == 1,
                        "channel remains writable after AlreadyConnectedException");
            }
            sc.close();
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> sc.connect(addr),
                    "once closed the failure becomes ClosedChannelException instead");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerReconnect();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
