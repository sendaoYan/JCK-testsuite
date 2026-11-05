/*
 * @test
 * @summary java.lang.InterruptedException API conformance: checked-exception hierarchy
 *          (InterruptedException -> Exception), all four constructors, and message/cause
 *          propagation.
 * @run main InterruptedExceptionTest
 */

public class InterruptedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        InterruptedException e = new InterruptedException();
        check(e instanceof Exception, "InterruptedException is an Exception");
        check(!((Object) e instanceof RuntimeException),
              "InterruptedException is checked (not RuntimeException)");
    }

    static void testConstructors() {
        InterruptedException e1 = new InterruptedException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        InterruptedException e2 = new InterruptedException("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
    }


    // ===================== 契约深化（2026-11）=====================

    static void expectISE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
    }

    static void testConstructorsFull() {
        InterruptedException e0 = new InterruptedException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getCause() == null, "no-arg cause null");
        InterruptedException e1 = new InterruptedException("boom");
        check("boom".equals(e1.getMessage()), "(String) message");
        check(e1.getCause() == null, "(String) cause null");
        check(e1.toString().equals("java.lang.InterruptedException: boom"),
              "toString exact with message");
        check(e0.toString().equals("java.lang.InterruptedException"),
              "toString exact without message");
        // initCause contract.
        Exception cause = new Exception("root");
        check(e0.initCause(cause) == e0, "initCause returns this");
        check(e0.getCause() == cause, "initCause sets cause");
        expectISE(() -> e0.initCause(new Exception("again")), "initCause twice ISE");
        // initCause(this) is rejected with an exact message on both JDKs.
        InterruptedException e2 = new InterruptedException();
        try {
            e2.initCause(e2);
            throw new AssertionError("self-cause should be rejected");
        } catch (IllegalArgumentException iae) {
            check("Self-causation not permitted".equals(iae.getMessage()),
                  "initCause(this) IAE exact message");
        }
        check(e2.getCause() == null, "rejected self-cause leaves cause null");
        check(e1.getLocalizedMessage().equals(e1.getMessage()), "getLocalizedMessage delegates");
    }

    static void testSuppressed() {
        InterruptedException e = new InterruptedException("sup");
        check(e.getSuppressed().length == 0, "no suppressed by default");
        Exception s1 = new Exception("s1");
        Exception s2 = new Exception("s2");
        e.addSuppressed(s1);
        e.addSuppressed(s2);
        check(e.getSuppressed().length == 2, "two suppressed recorded");
        check(e.getSuppressed()[0] == s1 && e.getSuppressed()[1] == s2,
              "suppressed order and identity");
        e.addSuppressed(s1);
        check(e.getSuppressed().length == 3, "duplicate suppression allowed");
        check(e.getCause() == null, "suppressed do not affect cause");
    }

    static void testStackTraceContract() {
        InterruptedException e = new InterruptedException("st");
        check(e.getStackTrace().length > 0, "stack trace captured");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        StackTraceElement[] frames = e.getStackTrace();
        check(frames[0].getClassName().equals(InterruptedExceptionTest.class.getName()),
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
        check(out.contains("java.lang.InterruptedException: st"), "printStackTrace prints class+message");
        check(out.contains("Foo.bar"), "printStackTrace prints set frames");
    }

    static void testSerializationRoundTrip() throws Exception {
        InterruptedException original = new InterruptedException("serial-msg");
        Exception cause = new Exception("serial-cause");
        original.initCause(cause);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        check(back instanceof InterruptedException, "round-trip preserves the class");
        check("serial-msg".equals(((InterruptedException) back).getMessage()),
              "round-trip preserves the message");
        check(((InterruptedException) back).getCause() instanceof Exception
              && "serial-cause".equals(((InterruptedException) back).getCause().getMessage()),
              "round-trip preserves the cause");
        check(back instanceof java.io.Serializable, "InterruptedException is Serializable");
    }

    static void testRealTriggerSelfInterrupt() throws Exception {
        Thread.currentThread().interrupt();
        InterruptedException caught = null;
        try {
            Thread.sleep(60_000);
        } catch (InterruptedException e) {
            caught = e;
        }
        check(caught != null, "pending interrupt makes sleep throw InterruptedException");
        check("sleep interrupted".equals(caught.getMessage()),
              "sleep-triggered IE message is \"sleep interrupted\" on both JDKs");
        check(!Thread.currentThread().isInterrupted(),
              "interrupt status cleared when InterruptedException was thrown");
        check(caught instanceof Exception, "triggered exception is an Exception");
        check(!((Object) caught instanceof RuntimeException), "still a checked exception");
        // Re-interrupt and confirm interrupted() consumes it once.
        Thread.currentThread().interrupt();
        check(Thread.interrupted(), "flag re-settable after IE");
        check(!Thread.interrupted(), "interrupted() cleared it");
    }

    static void testRealTriggerLatch() throws Exception {
        final java.util.concurrent.CountDownLatch entered = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch caught = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        final InterruptedException[] seen = {null};
        final boolean[] flagCleared = {false};
        Thread t = new Thread(() -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                seen[0] = e;
                flagCleared[0] = !Thread.currentThread().isInterrupted();
                caught.countDown();
            }
        }, "ie-latch-worker");
        t.setDaemon(true);
        t.start();
        check(entered.await(10, java.util.concurrent.TimeUnit.SECONDS), "worker entered await");
        t.interrupt();
        check(caught.await(10, java.util.concurrent.TimeUnit.SECONDS), "worker caught IE");
        check(seen[0] instanceof InterruptedException, "latch.await interrupt yields InterruptedException");
        check(seen[0].getMessage() == null, "latch-triggered IE message null");
        check(flagCleared[0], "worker's interrupt flag cleared by the thrown IE");
        release.countDown();
        t.join(10_000);
        check(!t.isAlive(), "worker terminated");
    }

    static void testRealTriggerWait() throws Exception {
        Object lock = new Object();
        Thread.currentThread().interrupt();
        boolean gotIE = false;
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                gotIE = true;
                check(e instanceof InterruptedException, "wait interrupt yields InterruptedException");
            }
        }
        check(gotIE, "pending interrupt makes wait throw InterruptedException");
        check(!Thread.currentThread().isInterrupted(), "flag cleared after IE from wait");
    }

    static void testCatchBySupertype() {
        try {
            InterruptedException probe = new InterruptedException("direct");
            throw probe;
        } catch (Exception e) {
            check(e instanceof InterruptedException, "catchable as Exception");
            check("direct".equals(e.getMessage()), "message through supertype catch");
        }
    }

    static void testWhiteBoxMore() throws Exception {
        Class<?> c = Class.forName("java.lang.InterruptedException");
        check(c.getSuperclass() == Exception.class, "superclass is Exception");
        check(c.getDeclaredConstructors().length == 2, "exactly 2 public constructors");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "class is concrete");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable via Throwable");
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testConstructors();
        // ---- 契约深化（2026-11）----
        testConstructorsFull();
        testSuppressed();
        testStackTraceContract();
        testSerializationRoundTrip();
        testRealTriggerSelfInterrupt();
        testRealTriggerLatch();
        testRealTriggerWait();
        testCatchBySupertype();
        testWhiteBoxMore();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
