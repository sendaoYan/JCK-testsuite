/*
 * @test
 * @summary java.lang.IllegalThreadStateException conformance: white-box structure + black-box behavior.
 * @run main IllegalThreadStateExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class IllegalThreadStateExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": got " + t.getClass().getName());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.IllegalThreadStateException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == IllegalArgumentException.class, "superclass is IllegalArgumentException");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.IllegalThreadStateException");

        IllegalThreadStateException e0 = new IllegalThreadStateException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { IllegalThreadStateException e = new IllegalThreadStateException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        IllegalThreadStateException t = new IllegalThreadStateException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.IllegalThreadStateException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }


    // ===================== 契约深化（2026-11）=====================

    static void expectITSE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalThreadStateException");
        } catch (IllegalThreadStateException expected) {
            passed++;
        }
    }

    static void testConstructorsFull() {
        IllegalThreadStateException e0 = new IllegalThreadStateException();
        check(e0.toString().equals("java.lang.IllegalThreadStateException"),
              "toString exact without message");
        IllegalThreadStateException e1 = new IllegalThreadStateException("boom");
        check(e1.toString().equals("java.lang.IllegalThreadStateException: boom"),
              "toString exact with message");
        // initCause(this) rejected with an exact message (same as JDK-8).
        IllegalThreadStateException e2 = new IllegalThreadStateException();
        try {
            e2.initCause(e2);
            throw new AssertionError("self-cause should be rejected");
        } catch (IllegalArgumentException iae) {
            check("Self-causation not permitted".equals(iae.getMessage()),
                  "initCause(this) IAE exact message");
        }
        check(e2.getCause() == null, "rejected self-cause leaves cause null");
    }

    static void testSuppressedMore() {
        IllegalThreadStateException e = new IllegalThreadStateException("sup");
        e.addSuppressed(new Exception("s1"));
        e.addSuppressed(new Exception("s2"));
        e.addSuppressed(new Exception("s1"));
        check(e.getSuppressed().length == 3, "duplicate suppression allowed");
        check(e.getSuppressed()[0].getMessage().equals("s1")
              && e.getSuppressed()[1].getMessage().equals("s2"),
              "suppressed order preserved");
    }

    static void testSetStackTrace() {
        IllegalThreadStateException e = new IllegalThreadStateException("st");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace()[0].getClassName().equals(IllegalThreadStateExceptionTest.class.getName()),
              "top frame is this class");
        StackTraceElement[] replacement = new StackTraceElement[] {
            new StackTraceElement("Foo", "bar", "Foo.java", 42)
        };
        e.setStackTrace(replacement);
        check(e.getStackTrace().length == 1, "setStackTrace replaces frames");
        check(e.getStackTrace() != replacement, "getStackTrace returns a copy");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bos));
        String out = new String(bos.toByteArray());
        check(out.contains("java.lang.IllegalThreadStateException: st"), "printStackTrace prints class+message");
        check(out.contains("Foo.bar"), "printStackTrace prints set frames");
    }

    static void testSerializationRoundTrip() throws Exception {
        IllegalThreadStateException original = new IllegalThreadStateException("serial-msg");
        original.initCause(new Exception("serial-cause"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        IllegalThreadStateException back = (IllegalThreadStateException) ois.readObject();
        check("serial-msg".equals(back.getMessage()), "round-trip preserves the message");
        check("serial-cause".equals(back.getCause().getMessage()), "round-trip preserves the cause");
    }

    static void testHierarchyFull() {
        try {
            throw new IllegalThreadStateException("supertype");
        } catch (IllegalArgumentException iae) {
            check(iae instanceof IllegalThreadStateException, "catchable as IllegalArgumentException");
        }
        check(!((Object) new IllegalThreadStateException() instanceof Error), "not an Error");
    }

    static void testStartTwiceLive() throws Exception {
        final java.util.concurrent.CountDownLatch entered = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        Thread t = new Thread(() -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException ignored) {
            }
        }, "itse-live");
        t.setDaemon(true);
        t.start();
        check(entered.await(10, java.util.concurrent.TimeUnit.SECONDS), "worker entered await");
        check(t.isAlive(), "worker alive");
        try {
            t.start();
            throw new AssertionError("second start on live thread should throw");
        } catch (IllegalThreadStateException e) {
            check(e.getMessage() == null, "live double-start ITSE message null");
        }
        release.countDown();
        t.join(10_000);
        check(!t.isAlive(), "worker still terminated normally after failed start");
        check(t.getState() == Thread.State.TERMINATED, "worker TERMINATED");
    }

    static void testStartAfterTerminatedAndDirectRun() throws Exception {
        Thread t = new Thread(() -> {});
        t.setDaemon(true);
        t.start();
        t.join(10_000);
        try {
            t.start();
            throw new AssertionError("start on terminated thread should throw");
        } catch (IllegalThreadStateException e) {
            check(e.getMessage() == null, "terminated-start ITSE message null");
        }
        expectITSE(() -> t.start(), "third start also ITSE");
        int[] runs = {0};
        Thread dr = new Thread(() -> runs[0]++);
        dr.run();
        check(runs[0] == 1, "direct run() executes the body once");
        check(dr.getState() == Thread.State.NEW, "still NEW after direct run()");
        dr.setDaemon(true);
        dr.start();
        dr.join(10_000);
        check(runs[0] == 2, "body ran again via start()");
        expectITSE(() -> dr.start(), "start after direct-run cycle now ITSE");
    }

    static void testSetDaemonAfterStart() throws Exception {
        final java.util.concurrent.CountDownLatch entered = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        Thread t = new Thread(() -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException ignored) {
            }
        }, "itse-daemon");
        t.setDaemon(true);
        t.start();
        check(entered.await(10, java.util.concurrent.TimeUnit.SECONDS), "worker entered await");
        try {
            t.setDaemon(true);
            throw new AssertionError("setDaemon(true) on live thread should throw");
        } catch (IllegalThreadStateException e) {
            check(e.getMessage() == null, "live setDaemon(true) ITSE message null");
        }
        try {
            t.setDaemon(false);
            throw new AssertionError("setDaemon(false) on live thread should throw");
        } catch (IllegalThreadStateException e) {
            check(e.getMessage() == null, "live setDaemon(false) ITSE message null");
        }
        release.countDown();
        t.join(10_000);
        check(t.isDaemon(), "daemon status unchanged by failed setDaemon calls");
        t.setDaemon(false);
        check(!t.isDaemon(), "setDaemon on terminated thread takes effect (as on 8)");
    }

    // JDK-27: ThreadGroup.destroy() is a no-op — no ITSE from any destroy usage.
    static void testGroupDestroyIsNoOp() throws Exception {
        final java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        @SuppressWarnings("removal")
        ThreadGroup busy = new ThreadGroup("itse-busy27");
        Thread worker = new Thread(busy, () -> {
            try {
                release.await();
            } catch (InterruptedException ignored) {
            }
        }, "itse-busy27-worker");
        worker.setDaemon(true);
        worker.start();
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (busy.activeCount() == 0 && System.nanoTime() < deadline) {
            Thread.yield();
        }
        check(busy.activeCount() == 1, "group has one live thread");
        @SuppressWarnings("removal")
        boolean noThrow = false;
        try {
            busy.destroy(); // no-op on 27: does not throw even when non-empty
            noThrow = true;
        } catch (IllegalThreadStateException e) {
            throw new AssertionError("27 destroy() must not throw ITSE");
        }
        check(noThrow, "destroy on non-empty group is a silent no-op on 27");
        release.countDown();
        worker.join(10_000);
        check(busy.activeCount() == 0, "worker left the group count");
        // New threads still join the "destroyed" group on 27.
        Thread still = new Thread(busy, () -> {});
        still.setDaemon(true);
        check(still.getThreadGroup() == busy, "new Thread accepts group after destroy() no-op");
    }

    // JDK-27-only: virtual threads follow the same start() ITSE contract.
    static void testVirtualThreadStartITSE() throws Exception {
        final java.util.concurrent.CountDownLatch entered = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        Thread vt = Thread.ofVirtual().start(() -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException ignored) {
            }
        });
        check(entered.await(10, java.util.concurrent.TimeUnit.SECONDS), "virtual worker entered await");
        try {
            vt.start();
            throw new AssertionError("second start on live virtual thread should throw");
        } catch (IllegalThreadStateException e) {
            // Virtual threads carry a message; platform-thread start() ITSE has none.
            check("Already started".equals(e.getMessage()),
                  "virtual live double-start ITSE message is \"Already started\"");
        }
        release.countDown();
        vt.join(10_000);
        check(!vt.isAlive(), "virtual worker terminated");
        try {
            vt.start();
            throw new AssertionError("start on finished virtual thread should throw");
        } catch (IllegalThreadStateException e) {
            check("Already started".equals(e.getMessage()),
                  "virtual terminated-start ITSE message is \"Already started\"");
        }
        Thread unstarted = Thread.ofVirtual().unstarted(() -> {});
        unstarted.start();
        unstarted.join(10_000);
        expectITSE(() -> unstarted.start(), "virtual unstarted-then-finished double-start ITSE");
    }

    static void testCatchBySupertype() {
        try {
            throw new IllegalThreadStateException("direct");
        } catch (RuntimeException re) {
            check(re instanceof IllegalThreadStateException, "catchable as RuntimeException");
            check("direct".equals(re.getMessage()), "message through supertype catch");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- 契约深化（2026-11）----
        testConstructorsFull();
        testSuppressedMore();
        testSetStackTrace();
        testSerializationRoundTrip();
        testHierarchyFull();
        testStartTwiceLive();
        testStartAfterTerminatedAndDirectRun();
        testSetDaemonAfterStart();
        testGroupDestroyIsNoOp();
        testVirtualThreadStartITSE();
        testCatchBySupertype();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
