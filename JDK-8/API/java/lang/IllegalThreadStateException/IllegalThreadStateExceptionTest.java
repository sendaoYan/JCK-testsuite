/*
 * @test
 * @summary java.lang.IllegalThreadStateException API conformance: restarting a dead thread.
 * @run main IllegalThreadStateExceptionTest
 */

public class IllegalThreadStateExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalThreadStateException() throws Exception {
        Thread t = new Thread(() -> {});
        t.start();
        t.join();
        try {
            t.start();
            check(false, "restarting a dead thread should throw");
        } catch (IllegalThreadStateException e) {
            check(true, "restarting a dead thread throws IllegalThreadStateException");
        }
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
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getCause() == null, "no-arg cause null");
        IllegalThreadStateException e1 = new IllegalThreadStateException("boom");
        check("boom".equals(e1.getMessage()), "(String) message transparency");
        check(e1.getCause() == null, "(String) cause null");
        check(e1.toString().equals("java.lang.IllegalThreadStateException: boom"),
              "toString exact with message");
        check(e0.toString().equals("java.lang.IllegalThreadStateException"),
              "toString exact without message");
        check(e1.getLocalizedMessage().equals(e1.getMessage()), "getLocalizedMessage delegates");
        // initCause contract.
        Exception cause = new Exception("root");
        check(e0.initCause(cause) == e0, "initCause returns this");
        check(e0.getCause() == cause, "initCause sets cause");
        try {
            e0.initCause(new Exception("again"));
            throw new AssertionError("initCause twice should throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        // initCause(this) is rejected with an exact message (same as JDK-27).
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
        check(e.getSuppressed().length == 0, "no suppressed by default");
        e.addSuppressed(new Exception("s1"));
        e.addSuppressed(new Exception("s2"));
        e.addSuppressed(new Exception("s1"));
        check(e.getSuppressed().length == 3, "duplicate suppression allowed");
        check(e.getSuppressed()[0].getMessage().equals("s1")
              && e.getSuppressed()[1].getMessage().equals("s2"),
              "suppressed order preserved");
        check(e.getCause() == null, "suppressed do not affect cause");
    }

    static void testStackTraceContract() {
        IllegalThreadStateException e = new IllegalThreadStateException("st");
        check(e.getStackTrace().length > 0, "stack trace captured");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace()[0].getClassName().equals(IllegalThreadStateExceptionTest.class.getName()),
              "top frame is this class");
        StackTraceElement[] replacement = new StackTraceElement[] {
            new StackTraceElement("Foo", "bar", "Foo.java", 42)
        };
        e.setStackTrace(replacement);
        check(e.getStackTrace().length == 1, "setStackTrace replaces frames");
        check(e.getStackTrace()[0].getMethodName().equals("bar"), "replaced frame content");
        check(e.getStackTrace() != replacement, "getStackTrace returns a copy");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray());
        check(out.contains("java.lang.IllegalThreadStateException: st"), "printStackTrace prints class+message");
        check(out.contains("Foo.bar"), "printStackTrace prints set frames");
    }

    static void testSerializationRoundTrip() throws Exception {
        IllegalThreadStateException original = new IllegalThreadStateException("serial-msg");
        original.initCause(new Exception("serial-cause"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        check(back instanceof IllegalThreadStateException, "round-trip preserves the class");
        check("serial-msg".equals(((IllegalThreadStateException) back).getMessage()),
              "round-trip preserves the message");
        check("serial-cause".equals(((IllegalThreadStateException) back).getCause().getMessage()),
              "round-trip preserves the cause");
        check(back instanceof java.io.Serializable, "IllegalThreadStateException is Serializable");
    }

    static void testHierarchyFull() {
        IllegalThreadStateException e = new IllegalThreadStateException();
        check(e instanceof RuntimeException, "is a RuntimeException");
        check(e instanceof IllegalArgumentException, "is an IllegalArgumentException");
        check(e instanceof Exception, "is an Exception");
        check(!((Object) e instanceof Error), "not an Error");
        try {
            throw new IllegalThreadStateException("supertype");
        } catch (IllegalArgumentException iae) {
            check(iae instanceof IllegalThreadStateException, "catchable as IllegalArgumentException");
        } catch (RuntimeException re) {
            throw new AssertionError("IllegalArgumentException catch must take precedence");
        }
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

    static void testStartAfterTerminated() throws Exception {
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
    }

    static void testStartAfterDirectRun() throws Exception {
        int[] runs = {0};
        Thread t = new Thread(() -> runs[0]++);
        t.run(); // direct call: state remains NEW, body runs once
        check(runs[0] == 1, "direct run() executes the body once");
        check(t.getState() == Thread.State.NEW, "still NEW after direct run()");
        t.setDaemon(true);
        t.start(); // legal: never started before
        t.join(10_000);
        check(runs[0] == 2, "body ran again via start()");
        expectITSE(() -> t.start(), "start after direct-run cycle now ITSE");
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
        // On a terminated thread setDaemon is a documented no-op success on JDK-8 (and 27).
        t.setDaemon(false);
        check(!t.isDaemon(), "setDaemon on terminated thread takes effect");
    }

    static void testGroupDestroyTriggers() throws Exception {
        final java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        ThreadGroup empty = new ThreadGroup("itse-empty");
        empty.destroy();
        check(empty.isDestroyed(), "empty group destroy succeeds");
        expectITSE(() -> empty.destroy(), "destroy twice ITSE");
        expectITSE(() -> new Thread(empty, () -> {}), "new Thread in destroyed group ITSE");
        expectITSE(() -> new ThreadGroup(empty, "child"), "new ThreadGroup in destroyed group ITSE");

        ThreadGroup busy = new ThreadGroup("itse-busy");
        Thread worker = new Thread(busy, () -> {
            try {
                release.await();
            } catch (InterruptedException ignored) {
            }
        }, "itse-busy-worker");
        worker.setDaemon(true);
        worker.start();
        // poll until the group reports the live thread (bounded 10s)
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (busy.activeCount() == 0 && System.nanoTime() < deadline) {
            Thread.yield();
        }
        check(busy.activeCount() == 1, "busy group has one live thread");
        try {
            busy.destroy();
            throw new AssertionError("destroy on non-empty group should throw");
        } catch (IllegalThreadStateException e) {
            check(e.getMessage() == null, "non-empty destroy ITSE message null");
        }
        release.countDown();
        worker.join(10_000);
        busy.destroy(); // now empty: succeeds
        check(busy.isDestroyed(), "group destroyable once worker died");
    }

    static void testWhiteBoxMore() throws Exception {
        Class<?> c = Class.forName("java.lang.IllegalThreadStateException");
        check(c.getSuperclass() == IllegalArgumentException.class, "superclass is IllegalArgumentException");
        check(c.getDeclaredConstructors().length == 2, "exactly 2 public constructors");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "class is concrete");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable via Throwable");
    }

    public static void main(String[] args) throws Exception {
        testIllegalThreadStateException();
        // ---- 契约深化（2026-11）----
        testConstructorsFull();
        testSuppressedMore();
        testStackTraceContract();
        testSerializationRoundTrip();
        testHierarchyFull();
        testStartTwiceLive();
        testStartAfterTerminated();
        testStartAfterDirectRun();
        testSetDaemonAfterStart();
        testGroupDestroyTriggers();
        testWhiteBoxMore();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
