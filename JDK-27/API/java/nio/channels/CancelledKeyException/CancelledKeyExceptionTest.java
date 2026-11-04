/*
 * @test
 * @summary java.nio.channels.CancelledKeyException conformance: white-box structure + black-box behavior.
 * @run main CancelledKeyExceptionTest
 */

import java.nio.channels.CancelledKeyException;

public class CancelledKeyExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(CancelledKeyException.class), "hierarchy: extends IllegalStateException");
        check(new CancelledKeyException() != null, "no-arg constructor");
        check(new CancelledKeyException().getMessage() == null, "no-arg message is null");
        check(new CancelledKeyException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        try (java.nio.channels.Selector sel = java.nio.channels.Selector.open()) {
            pipe.source().configureBlocking(false);
            java.nio.channels.SelectionKey key =
                    pipe.source().register(sel, java.nio.channels.SelectionKey.OP_READ);
            key.cancel();
            try {
                key.interestOps(java.nio.channels.SelectionKey.OP_READ);
                check(false, "ops on cancelled key should throw");
            } catch (CancelledKeyException e) {
                passed++;
            }
            sel.selectNow();
        } finally {
            try { pipe.sink().close(); } catch (Exception ignore) { }
            try { pipe.source().close(); } catch (Exception ignore) { }
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
        check(CancelledKeyException.class.getSuperclass() == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalStateException.class.isAssignableFrom(CancelledKeyException.class), "assignable to IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(CancelledKeyException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(CancelledKeyException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(CancelledKeyException.class), "Serializable through Throwable");
        check(new CancelledKeyException() instanceof java.lang.IllegalStateException, "instanceof IllegalStateException");
        check(CancelledKeyException.class.getName().equals("java.nio.channels.CancelledKeyException"), "fully-qualified name");
        check(!java.io.IOException.class.isAssignableFrom(CancelledKeyException.class), "unchecked: not an IOException");
    }

    static void testConstructorContract() {
        CancelledKeyException e = new CancelledKeyException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = CancelledKeyException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.CancelledKeyException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("CancelledKeyException"), "toString() contains the simple class name");
        CancelledKeyException other = new CancelledKeyException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(CancelledKeyException.class).getSerialVersionUID() == -8438032138028814268L,
                "serialVersionUID == -8438032138028814268L (identical on JDK-8 and JDK-27, probed)");
        CancelledKeyException original = new CancelledKeyException();
        CancelledKeyException back = (CancelledKeyException) serializeRoundTrip(original);
        check(back.getClass() == CancelledKeyException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        CancelledKeyException e = new CancelledKeyException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        CancelledKeyException selfCause = new CancelledKeyException();
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
        CancelledKeyException thrown = new CancelledKeyException();
        try {
            throw thrown;
        } catch (CancelledKeyException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: operations on a cancelled SelectionKey ----
    static void testRealTriggerCancelledKey() throws Exception {
        try (java.nio.channels.Selector sel = java.nio.channels.Selector.open();
             java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open()) {
            sc.configureBlocking(false);
            java.nio.channels.SelectionKey key = sc.register(sel, java.nio.channels.SelectionKey.OP_READ);
            check(key.isValid(), "key valid right after registration");
            check(key.interestOps() == java.nio.channels.SelectionKey.OP_READ,
                    "interestOps readable before cancel");
            key.cancel();
            check(!key.isValid(), "key invalid after cancel()");
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.interestOps(),
                    "interestOps() on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class,
                    () -> key.interestOps(java.nio.channels.SelectionKey.OP_WRITE),
                    "interestOps(int) on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.readyOps(),
                    "readyOps() on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.isReadable(),
                    "isReadable() on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.isWritable(),
                    "isWritable() on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.isAcceptable(),
                    "isAcceptable() on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.isConnectable(),
                    "isConnectable() on cancelled key -> CancelledKeyException");
            check(key.channel() == sc, "channel() still returns the original channel after cancel");
            check(key.selector() == sel, "selector() still returns the original selector after cancel");
            check(key.attachment() == null, "attachment() null when nothing was attached");
            check(key.attach("payload") == null, "attach() keeps working after cancel, returns old attachment");
            check("payload".equals(key.attachment()), "attachment readable after cancel");
            key.cancel();
            check(true, "cancel() is idempotent: the second cancel() does not throw");
            sel.selectNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerCancelledKey();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
