/*
 * @test
 * @summary java.nio.channels.IllegalSelectorException conformance: white-box structure + black-box behavior.
 * @run main IllegalSelectorExceptionTest
 */

import java.nio.channels.IllegalSelectorException;

public class IllegalSelectorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(IllegalSelectorException.class), "hierarchy: extends java.lang.IllegalArgumentException");
        check(new IllegalSelectorException() != null, "no-arg constructor");
        check(new IllegalSelectorException().getMessage() == null, "no-arg message is null");
        check(new IllegalSelectorException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IllegalSelectorException e = new IllegalSelectorException();
        check(e.getMessage() == null, "no message");
        check(e instanceof IllegalArgumentException, "IllegalArgumentException family");
        try {
            throw e;
        } catch (IllegalSelectorException again) {
            check(again == e, "rethrow preserves identity");
        }
    }



    /** A SelectableChannel implementation that is not a sun.nio.ch channel. */
    static final class ForeignSelectableChannel extends java.nio.channels.spi.AbstractSelectableChannel {
        ForeignSelectableChannel(java.nio.channels.spi.SelectorProvider provider) { super(provider); }
        @Override protected void implCloseSelectableChannel() { }
        @Override protected void implConfigureBlocking(boolean block) { }
        @Override public int validOps() { return java.nio.channels.SelectionKey.OP_READ; }
    }

    /** A provider instance distinct from the one that created the default selector. */
    static java.nio.channels.spi.SelectorProvider foreignProvider() {
        return new java.nio.channels.spi.SelectorProvider() {
            @Override public java.nio.channels.DatagramChannel openDatagramChannel() throws java.io.IOException { throw new java.io.IOException("unused"); }
            @Override public java.nio.channels.DatagramChannel openDatagramChannel(java.net.ProtocolFamily family) throws java.io.IOException { throw new java.io.IOException("unused"); }
            @Override public java.nio.channels.Pipe openPipe() throws java.io.IOException { throw new java.io.IOException("unused"); }
            @Override public java.nio.channels.spi.AbstractSelector openSelector() throws java.io.IOException { throw new java.io.IOException("unused"); }
            @Override public java.nio.channels.ServerSocketChannel openServerSocketChannel() throws java.io.IOException { throw new java.io.IOException("unused"); }
            @Override public java.nio.channels.SocketChannel openSocketChannel() throws java.io.IOException { throw new java.io.IOException("unused"); }
        };
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
        check(IllegalSelectorException.class.getSuperclass() == java.lang.IllegalArgumentException.class,
                "direct superclass is IllegalArgumentException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(IllegalSelectorException.class), "assignable to IllegalArgumentException");
        check(java.lang.RuntimeException.class.isAssignableFrom(IllegalSelectorException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(IllegalSelectorException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(IllegalSelectorException.class), "Serializable through Throwable");
        check(new IllegalSelectorException() instanceof java.lang.IllegalArgumentException, "instanceof IllegalArgumentException");
        check(IllegalSelectorException.class.getName().equals("java.nio.channels.IllegalSelectorException"), "fully-qualified name");
        check(java.lang.RuntimeException.class.getSuperclass() == java.lang.Exception.class,
                "chain: RuntimeException extends Exception");
    }

    static void testConstructorContract() {
        IllegalSelectorException e = new IllegalSelectorException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = IllegalSelectorException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.IllegalSelectorException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("IllegalSelectorException"), "toString() contains the simple class name");
        IllegalSelectorException other = new IllegalSelectorException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(IllegalSelectorException.class).getSerialVersionUID() == -8406323347253320987L,
                "serialVersionUID == -8406323347253320987L (identical on JDK-8 and JDK-27, probed)");
        IllegalSelectorException original = new IllegalSelectorException();
        IllegalSelectorException back = (IllegalSelectorException) serializeRoundTrip(original);
        check(back.getClass() == IllegalSelectorException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        IllegalSelectorException e = new IllegalSelectorException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        IllegalSelectorException selfCause = new IllegalSelectorException();
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
        IllegalSelectorException thrown = new IllegalSelectorException();
        try {
            throw thrown;
        } catch (IllegalSelectorException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: registering a foreign (non sun.nio.ch) selectable channel ----
    // javap/OpenJDK source: the only throw site of IllegalSelectorException is
    // sun.nio.ch.SelectorImpl.register, guarded by "!(ch instanceof SelChImpl)".
    static void testRealTriggerForeignChannelRegister() throws Exception {
        try (java.nio.channels.Selector sel = java.nio.channels.Selector.open()) {
            java.nio.channels.SelectableChannel foreign = new ForeignSelectableChannel(foreignProvider());
            check(foreign.isOpen(), "custom channel open");
            foreign.configureBlocking(false);
            check(!foreign.isBlocking(), "custom channel switched to non-blocking");
            checkThrows(java.nio.channels.IllegalSelectorException.class,
                    () -> foreign.register(sel, java.nio.channels.SelectionKey.OP_READ, null),
                    "register() of a foreign-provider channel -> IllegalSelectorException");
            foreign.close();
            check(!foreign.isOpen(), "custom channel closed");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> foreign.register(sel, java.nio.channels.SelectionKey.OP_READ, null),
                    "closed check runs before the provider check -> ClosedChannelException");
            // Contrast: a channel from the same provider as the selector registers normally
            try (java.nio.channels.SocketChannel normal = java.nio.channels.SocketChannel.open()) {
                normal.configureBlocking(false);
                java.nio.channels.SelectionKey k =
                        normal.register(sel, java.nio.channels.SelectionKey.OP_READ);
                check(k != null && k.isValid(), "same-provider registration works normally");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerForeignChannelRegister();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
