/*
 * @test
 * @summary java.nio.channels.ClosedByInterruptException conformance: white-box structure + black-box behavior.
 * @run main ClosedByInterruptExceptionTest
 */

import java.nio.channels.ClosedByInterruptException;

public class ClosedByInterruptExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.channels.AsynchronousCloseException.class.isAssignableFrom(ClosedByInterruptException.class), "hierarchy: extends java.nio.channels.AsynchronousCloseException");
        check(new ClosedByInterruptException() != null, "no-arg constructor");
        check(new ClosedByInterruptException().getMessage() == null, "no-arg message is null");
        check(new ClosedByInterruptException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // interrupting a thread blocked on a channel read
        final java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        final Exception[] caught = new Exception[1];
        Thread reader = new Thread(() -> {
            try {
                pipe.source().read(java.nio.ByteBuffer.allocate(1));
            } catch (Exception e) {
                caught[0] = e;
            }
        }, "interrupt-reader");
        reader.setDaemon(true);
        reader.start();
        Thread.sleep(300); // let the reader block
        reader.interrupt();
        reader.join(10_000);
        check(!reader.isAlive(), "reader finished after interrupt");
        check(caught[0] instanceof ClosedByInterruptException,
              "interrupted blocked read -> ClosedByInterruptException, got " + caught[0]);
        check(!pipe.source().isOpen(), "channel closed by interrupt");
        pipe.sink().close();
        pipe.source().close();
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
        check(ClosedByInterruptException.class.getSuperclass() == java.nio.channels.AsynchronousCloseException.class,
                "direct superclass is AsynchronousCloseException (javap-verified on JDK-8 and JDK-27)");
        check(java.nio.channels.AsynchronousCloseException.class.isAssignableFrom(ClosedByInterruptException.class), "assignable to AsynchronousCloseException");
        check(java.io.IOException.class.isAssignableFrom(ClosedByInterruptException.class), "assignable to IOException");
        check(java.lang.Throwable.class.isAssignableFrom(ClosedByInterruptException.class), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(ClosedByInterruptException.class), "Serializable through Throwable");
        check(new ClosedByInterruptException() instanceof java.nio.channels.AsynchronousCloseException, "instanceof AsynchronousCloseException");
        check(ClosedByInterruptException.class.getName().equals("java.nio.channels.ClosedByInterruptException"), "fully-qualified name");
        check(java.nio.channels.ClosedChannelException.class.getSuperclass() == java.io.IOException.class,
                "full chain: ClosedChannelException extends IOException (javap-verified)");
        check(java.nio.channels.AsynchronousCloseException.class.getSuperclass()
                        == java.nio.channels.ClosedChannelException.class,
                "full chain: AsynchronousCloseException extends ClosedChannelException (javap-verified)");
        check(java.nio.channels.ClosedByInterruptException.class.getSuperclass()
                        == java.nio.channels.AsynchronousCloseException.class,
                "full chain: ClosedByInterruptException extends AsynchronousCloseException (javap-verified)");
    }

    static void testConstructorContract() {
        ClosedByInterruptException e = new ClosedByInterruptException();
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e.getLocalizedMessage() == null, "localized message null");
        java.lang.reflect.Constructor<?>[] ctors = ClosedByInterruptException.class.getConstructors();
        check(ctors.length == 1, "exactly one public constructor (no message-taking overload on either JDK)");
        check(ctors[0].getParameterCount() == 0, "the single public constructor takes no arguments");
        check("java.nio.channels.ClosedByInterruptException".equals(e.toString()), "toString() is the bare class name when the message is null");
        check(e.toString().contains("ClosedByInterruptException"), "toString() contains the simple class name");
        ClosedByInterruptException other = new ClosedByInterruptException();
        check(other != e && other.getClass() == e.getClass(), "constructor yields fresh instances of the same type");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ClosedByInterruptException.class).getSerialVersionUID() == -4488191543534286750L,
                "serialVersionUID == -4488191543534286750L (identical on JDK-8 and JDK-27, probed)");
        ClosedByInterruptException original = new ClosedByInterruptException();
        ClosedByInterruptException back = (ClosedByInterruptException) serializeRoundTrip(original);
        check(back.getClass() == ClosedByInterruptException.class, "round-trip preserves the exact type");
        check(back != original, "round-trip yields a distinct instance");
        check(back.getMessage() == null, "round-trip keeps the null message");
        check(back.getCause() == null, "round-trip keeps the null cause");
        check(back.getSuppressed().length == 0, "round-trip keeps the empty suppressed list");
        check(back.getStackTrace().length == original.getStackTrace().length,
                "round-trip keeps the captured stack depth");
    }

    static void testThrowableContract() {
        ClosedByInterruptException e = new ClosedByInterruptException();
        check(e.getCause() == null, "default cause is null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        IllegalStateException cause = new IllegalStateException("root-cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() returns the cause just installed");
        checkThrows(java.lang.IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("nope")),
                "second initCause() -> IllegalStateException");
        check(e.getCause() == cause, "refused second initCause() leaves the cause unchanged");
        ClosedByInterruptException selfCause = new ClosedByInterruptException();
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
        ClosedByInterruptException thrown = new ClosedByInterruptException();
        try {
            throw thrown;
        } catch (ClosedByInterruptException caught) {
            check(caught == thrown, "throw/catch preserves identity");
            check(caught.getStackTrace().length > 0, "thrown instance carries stack frames");
        }
    }

    // ---- Real-world trigger: interrupting a thread blocked in channel read (loopback) ----
    // Determinism: whether the interrupt lands before or after read() parks, the channel
    // machinery reacts the same way (AbstractInterruptibleChannel checks the interrupt
    // status both on entry into and exit out of a blocking operation).
    static void testRealTriggerInterruptedRead() throws Exception {
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(new java.net.InetSocketAddress(lo, 0));
            final java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open();
            sc.connect(ssc.getLocalAddress());
            try (java.nio.channels.SocketChannel peer = ssc.accept()) {
                final java.util.concurrent.CountDownLatch started = new java.util.concurrent.CountDownLatch(1);
                final java.util.concurrent.atomic.AtomicReference<String> caught =
                        new java.util.concurrent.atomic.AtomicReference<>("nothing");
                final java.util.concurrent.atomic.AtomicBoolean interruptedInside =
                        new java.util.concurrent.atomic.AtomicBoolean(false);
                Thread reader = new Thread(() -> {
                    started.countDown();
                    try {
                        sc.read(java.nio.ByteBuffer.allocate(1));
                        caught.set("returned-normally");
                    } catch (Throwable t) {
                        caught.set(t.getClass().getName());
                        interruptedInside.set(Thread.currentThread().isInterrupted());
                    }
                }, "a8-interrupt-reader");
                reader.setDaemon(true);
                reader.start();
                check(started.await(10, java.util.concurrent.TimeUnit.SECONDS), "reader thread started");
                reader.interrupt();
                reader.join(10_000);
                check(!reader.isAlive(), "reader terminated after interrupt (bounded join)");
                check("java.nio.channels.ClosedByInterruptException".equals(caught.get()),
                        "interrupted blocking read -> exact ClosedByInterruptException, got " + caught.get());
                check(interruptedInside.get(), "thread interrupt status preserved while throwing (checked in-thread)");
                check(!sc.isOpen(), "channel closed by the interrupt");
                checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> sc.read(java.nio.ByteBuffer.allocate(1)),
                        "reading the interrupt-closed channel -> ClosedChannelException");
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
        testRealTriggerInterruptedRead();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
