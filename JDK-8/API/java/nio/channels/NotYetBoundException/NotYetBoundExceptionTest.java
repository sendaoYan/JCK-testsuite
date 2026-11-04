/*
 * @test
 * @summary java.nio.channels.NotYetBoundException conformance: hierarchy and constructor.
 * @run main NotYetBoundExceptionTest
 */

import java.nio.channels.NotYetBoundException;

public class NotYetBoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNotYetBoundException() {
        check(IllegalStateException.class.isAssignableFrom(NotYetBoundException.class), "extends IllegalStateException");
        NotYetBoundException e = new NotYetBoundException();
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
        check(NotYetBoundException.class.getSuperclass() == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalStateException.class.isAssignableFrom(NotYetBoundException.class), "assignable to IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(NotYetBoundException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(NotYetBoundException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(NotYetBoundException.class), "Serializable through Throwable");
        check(new NotYetBoundException() instanceof java.lang.IllegalStateException, "instanceof IllegalStateException");
        check(NotYetBoundException.class.getName().equals("java.nio.channels.NotYetBoundException"), "fully-qualified name");
        check(!java.io.IOException.class.isAssignableFrom(NotYetBoundException.class), "unchecked: not an IOException");
    }

    static void testConstructorContract() {
        NotYetBoundException e = new NotYetBoundException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = NotYetBoundException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.NotYetBoundException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("NotYetBoundException"), "toString() contains the simple class name");
        NotYetBoundException other = new NotYetBoundException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(NotYetBoundException.class).getSerialVersionUID() == 4640999303950202242L,
                "serialVersionUID == 4640999303950202242L (identical on JDK-8 and JDK-27, probed)");
        NotYetBoundException original = new NotYetBoundException();
        NotYetBoundException back = (NotYetBoundException) serializeRoundTrip(original);
        check(back.getClass() == NotYetBoundException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        NotYetBoundException e = new NotYetBoundException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        NotYetBoundException selfCause = new NotYetBoundException();
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
        NotYetBoundException thrown = new NotYetBoundException();
        try {
            throw thrown;
        } catch (NotYetBoundException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: accept() before bind (loopback) ----
    static void testRealTriggerAcceptBeforeBind() throws Exception {
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            check(ssc.getLocalAddress() == null, "local address null before bind");
            checkThrows(java.nio.channels.NotYetBoundException.class, () -> ssc.accept(),
                    "blocking accept() before bind -> NotYetBoundException");
            ssc.configureBlocking(false);
            checkThrows(java.nio.channels.NotYetBoundException.class, () -> ssc.accept(),
                    "non-blocking accept() before bind -> NotYetBoundException");
            ssc.configureBlocking(true);
            ssc.bind(null);
            java.net.InetSocketAddress addr = (java.net.InetSocketAddress) ssc.getLocalAddress();
            check(addr != null && addr.getPort() > 0, "bound after bind(null)");
            try (java.nio.channels.SocketChannel client = java.nio.channels.SocketChannel.open(addr)) {
                java.nio.channels.SocketChannel accepted = ssc.accept();
                check(accepted != null, "accept() succeeds once bound (client connected into the backlog)");
                accepted.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testNotYetBoundException();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerAcceptBeforeBind();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
