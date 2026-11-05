/*
 * @test
 * @summary java.lang.InterruptedException conformance: white-box structure + black-box behavior.
 * @run main InterruptedExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class InterruptedExceptionTest {
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
        Class<?> c = Class.forName("java.lang.InterruptedException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == Exception.class, "superclass is Exception");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.InterruptedException");

        InterruptedException e0 = new InterruptedException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { InterruptedException e = new InterruptedException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        InterruptedException t = new InterruptedException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.InterruptedException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }


    // ===================== 契约深化（2026-11）=====================

    static void testConstructorsFull() {
        InterruptedException e0 = new InterruptedException();
        check(e0.getMessage() == null && e0.getCause() == null, "no-arg ctor bare");
        InterruptedException e1 = new InterruptedException("boom");
        check(e1.toString().equals("java.lang.InterruptedException: boom"),
              "toString exact with message");
        check(new InterruptedException().toString().equals("java.lang.InterruptedException"),
              "toString exact without message");
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

    static void testSuppressedMore() {
        InterruptedException e = new InterruptedException("sup");
        e.addSuppressed(new Exception("s1"));
        e.addSuppressed(new Exception("s2"));
        e.addSuppressed(new Exception("s1"));
        check(e.getSuppressed().length == 3, "duplicate suppression allowed");
        check(e.getSuppressed()[0].getMessage().equals("s1")
              && e.getSuppressed()[1].getMessage().equals("s2"),
              "suppressed order");
        check(e.getCause() == null, "suppressed do not affect cause");
    }

    static void testSetStackTrace() {
        InterruptedException e = new InterruptedException("st");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace()[0].getClassName().equals(InterruptedExceptionTest.class.getName()),
              "top frame is this class");
        StackTraceElement[] replacement = new StackTraceElement[] {
            new StackTraceElement("Foo", "bar", "Foo.java", 42)
        };
        e.setStackTrace(replacement);
        check(e.getStackTrace().length == 1, "setStackTrace replaces frames");
        check(e.getStackTrace()[0].getMethodName().equals("bar"), "replaced frame content");
        check(e.getStackTrace() != replacement, "getStackTrace returns a copy");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bos));
        String out = new String(bos.toByteArray());
        check(out.contains("java.lang.InterruptedException: st"), "printStackTrace prints class+message");
        check(out.contains("Foo.bar"), "printStackTrace prints set frames");
    }

    static void testSerializationRoundTrip() throws Exception {
        InterruptedException original = new InterruptedException("serial-msg");
        original.initCause(new Exception("serial-cause"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        InterruptedException back = (InterruptedException) ois.readObject();
        check("serial-msg".equals(back.getMessage()), "round-trip preserves the message");
        check("serial-cause".equals(back.getCause().getMessage()), "round-trip preserves the cause");
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
              "sleep-triggered IE message is \"sleep interrupted\" on 27 (null on 8)");
        check(!Thread.currentThread().isInterrupted(), "status cleared when IE thrown");
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
            }
        }
        check(gotIE, "pending interrupt makes wait throw InterruptedException");
        check(!Thread.currentThread().isInterrupted(), "flag cleared after IE from wait");
    }

    static void testCatchBySupertype() {
        try {
            throw new InterruptedException("direct");
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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- 契约深化（2026-11）----
        testConstructorsFull();
        testSuppressedMore();
        testSetStackTrace();
        testSerializationRoundTrip();
        testRealTriggerSelfInterrupt();
        testRealTriggerLatch();
        testRealTriggerWait();
        testCatchBySupertype();
        testWhiteBoxMore();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
