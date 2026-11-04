/*
 * @test
 * @summary java.nio.channels.ClosedSelectorException conformance: hierarchy and constructor.
 * @run main ClosedSelectorExceptionTest
 */

import java.nio.channels.ClosedSelectorException;

public class ClosedSelectorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClosedSelectorException() {
        check(IllegalStateException.class.isAssignableFrom(ClosedSelectorException.class), "extends IllegalStateException");
        ClosedSelectorException e = new ClosedSelectorException();
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
        check(ClosedSelectorException.class.getSuperclass() == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException (javap-verified on JDK-8 and JDK-27)");
        check(java.lang.IllegalStateException.class.isAssignableFrom(ClosedSelectorException.class), "assignable to IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(ClosedSelectorException.class), "assignable to RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(ClosedSelectorException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(ClosedSelectorException.class), "Serializable through Throwable");
        check(new ClosedSelectorException() instanceof java.lang.IllegalStateException, "instanceof IllegalStateException");
        check(ClosedSelectorException.class.getName().equals("java.nio.channels.ClosedSelectorException"), "fully-qualified name");
        check(!java.io.IOException.class.isAssignableFrom(ClosedSelectorException.class), "unchecked: not an IOException");
    }

    static void testConstructorContract() {
        ClosedSelectorException e = new ClosedSelectorException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = ClosedSelectorException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.ClosedSelectorException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("ClosedSelectorException"), "toString() contains the simple class name");
        ClosedSelectorException other = new ClosedSelectorException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ClosedSelectorException.class).getSerialVersionUID() == 6466297122317847835L,
                "serialVersionUID == 6466297122317847835L (identical on JDK-8 and JDK-27, probed)");
        ClosedSelectorException original = new ClosedSelectorException();
        ClosedSelectorException back = (ClosedSelectorException) serializeRoundTrip(original);
        check(back.getClass() == ClosedSelectorException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        ClosedSelectorException e = new ClosedSelectorException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        ClosedSelectorException selfCause = new ClosedSelectorException();
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
        ClosedSelectorException thrown = new ClosedSelectorException();
        try {
            throw thrown;
        } catch (ClosedSelectorException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: selector operations after close() ----
    static void testRealTriggerClosedSelector() throws Exception {
        java.nio.channels.Selector sel = java.nio.channels.Selector.open();
        check(sel.isOpen(), "selector open right after creation");
        check(sel.selectNow() == 0, "selectNow() on an open empty selector returns 0");
        sel.close();
        check(!sel.isOpen(), "selector closed");
        sel.close();
        check(true, "second close() is idempotent");
        checkThrows(java.nio.channels.ClosedSelectorException.class, () -> sel.selectNow(),
                "selectNow() on closed selector -> ClosedSelectorException");
        checkThrows(java.nio.channels.ClosedSelectorException.class, () -> sel.select(1),
                "select(timeout) on closed selector -> ClosedSelectorException");
        checkThrows(java.nio.channels.ClosedSelectorException.class, () -> sel.keys(),
                "keys() on closed selector -> ClosedSelectorException");
        checkThrows(java.nio.channels.ClosedSelectorException.class, () -> sel.selectedKeys(),
                "selectedKeys() on closed selector -> ClosedSelectorException");
        check(sel.wakeup() == sel, "wakeup() on a closed selector does not throw and returns the selector");
        check(sel.provider() != null, "provider() on a closed selector does not throw");
    }

    public static void main(String[] args) throws Exception {
        testClosedSelectorException();
        testHierarchyContract();
        testConstructorContract();
        testSerializationContract();
        testThrowableContract();
        testRealTriggerClosedSelector();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
