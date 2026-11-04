/*
 * @test
 * @summary java.nio.channels.IllegalBlockingModeException conformance: white-box structure + black-box behavior.
 * @run main IllegalBlockingModeExceptionTest
 */

import java.nio.channels.IllegalBlockingModeException;

public class IllegalBlockingModeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(IllegalBlockingModeException.class), "hierarchy: extends IllegalStateException");
        check(new IllegalBlockingModeException() != null, "no-arg constructor");
        check(new IllegalBlockingModeException().getMessage() == null, "no-arg message is null");
        check(new IllegalBlockingModeException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // using the socket adaptor stream while the channel is in non-blocking mode
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open();
             java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open()) {
            ssc.bind(new java.net.InetSocketAddress(lo, 0));
            sc.connect(ssc.getLocalAddress());
            try (java.nio.channels.SocketChannel peer = ssc.accept()) {
                sc.configureBlocking(false);
                try {
                    sc.socket().getInputStream().read();
                    check(false, "adaptor read in non-blocking mode should throw");
                } catch (IllegalBlockingModeException e) {
                    passed++;
                } catch (java.io.IOException io) {
                    check(false, "expected IllegalBlockingModeException, got " + io);
                }
                try {
                    sc.socket().getOutputStream().write(1);
                    check(false, "adaptor write in non-blocking mode should throw");
                } catch (IllegalBlockingModeException e) {
                    passed++;
                } catch (java.io.IOException io) {
                    check(false, "expected IllegalBlockingModeException (write), got " + io);
                }
            }
        }
        // adaptor accept on a non-blocking server channel
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(null);
            ssc.configureBlocking(false);
            try {
                ssc.socket().accept();
                check(false, "adaptor accept in non-blocking mode should throw");
            } catch (IllegalBlockingModeException e) {
                passed++;
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
        check(IllegalBlockingModeException.class.getSuperclass() == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalStateException.class.isAssignableFrom(IllegalBlockingModeException.class), "assignable to IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(IllegalBlockingModeException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(IllegalBlockingModeException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(IllegalBlockingModeException.class), "Serializable through Throwable");
        check(new IllegalBlockingModeException() instanceof java.lang.IllegalStateException, "instanceof IllegalStateException");
        check(IllegalBlockingModeException.class.getName().equals("java.nio.channels.IllegalBlockingModeException"), "fully-qualified name");
        check(!java.io.IOException.class.isAssignableFrom(IllegalBlockingModeException.class), "unchecked: not an IOException");
    }

    static void testConstructorContract() {
        IllegalBlockingModeException e = new IllegalBlockingModeException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = IllegalBlockingModeException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.IllegalBlockingModeException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("IllegalBlockingModeException"), "toString() contains the simple class name");
        IllegalBlockingModeException other = new IllegalBlockingModeException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(IllegalBlockingModeException.class).getSerialVersionUID() == -3335774961855590474L,
                "serialVersionUID == -3335774961855590474L (identical on JDK-8 and JDK-27, probed)");
        IllegalBlockingModeException original = new IllegalBlockingModeException();
        IllegalBlockingModeException back = (IllegalBlockingModeException) serializeRoundTrip(original);
        check(back.getClass() == IllegalBlockingModeException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        IllegalBlockingModeException e = new IllegalBlockingModeException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        IllegalBlockingModeException selfCause = new IllegalBlockingModeException();
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
        IllegalBlockingModeException thrown = new IllegalBlockingModeException();
        try {
            throw thrown;
        } catch (IllegalBlockingModeException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world triggers: blocking-mode violations (loopback) ----
    static void testRealTriggerBlockingMode() throws Exception {
        // 1) configureBlocking(true) while registered with a selector
        try (java.nio.channels.Selector sel = java.nio.channels.Selector.open();
             java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open()) {
            sc.configureBlocking(false);
            sc.register(sel, java.nio.channels.SelectionKey.OP_READ);
            checkThrows(java.nio.channels.IllegalBlockingModeException.class,
                    () -> sc.configureBlocking(true),
                    "configureBlocking(true) while registered -> IllegalBlockingModeException");
            check(!sc.isBlocking(), "channel stays non-blocking after the refusal");
        }
        // 2) socket adaptor streams on a connected non-blocking channel
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(new java.net.InetSocketAddress(lo, 0));
            java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open(ssc.getLocalAddress());
            try (java.nio.channels.SocketChannel peer = ssc.accept()) {
                sc.configureBlocking(false);
                final java.io.InputStream in = sc.socket().getInputStream();
                final java.io.OutputStream out = sc.socket().getOutputStream();
                check(in != null && out != null, "adaptor streams obtainable while non-blocking");
                checkThrows(java.nio.channels.IllegalBlockingModeException.class, () -> in.read(),
                        "adaptor InputStream.read() in non-blocking mode -> IllegalBlockingModeException");
                checkThrows(java.nio.channels.IllegalBlockingModeException.class,
                        () -> { out.write(1); return null; },
                        "adaptor OutputStream.write() in non-blocking mode -> IllegalBlockingModeException");
                check(sc.isOpen(), "channel unaffected by the refused adaptor calls");
            }
            sc.close();
        }
        // 3) ServerSocket adaptor accept on a non-blocking server channel
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(null);
            ssc.configureBlocking(false);
            checkThrows(java.nio.channels.IllegalBlockingModeException.class, () -> ssc.socket().accept(),
                    "adaptor ServerSocket.accept() in non-blocking mode -> IllegalBlockingModeException");
        }
        // Contrast: in blocking mode the adaptor streams work (EOF after the peer closes)
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(new java.net.InetSocketAddress(lo, 0));
            java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open(ssc.getLocalAddress());
            java.nio.channels.SocketChannel peer = ssc.accept();
            peer.close();
            check(sc.socket().getInputStream().read() == -1,
                    "blocking adaptor read() reports EOF normally");
            sc.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerBlockingMode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
