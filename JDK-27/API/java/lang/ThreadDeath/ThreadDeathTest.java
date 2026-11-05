/*
 * @test
 * @summary java.lang.ThreadDeath conformance: white-box structure + black-box behavior.
 * @run main ThreadDeathTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ThreadDeathTest {
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
        Class<?> c = Class.forName("java.lang.ThreadDeath");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == Error.class, "superclass is Error");
        check(c.getDeclaredConstructors().length == 1, "1 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ThreadDeath");

        ThreadDeath e0 = new ThreadDeath();
        check(e0.getMessage() == null, "no-arg ctor has null message");

        // common Throwable contract
        ThreadDeath t = new ThreadDeath();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.ThreadDeath"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    // ================= 契约深化（2026-11）追加 =================
    static void testClassFacts() throws Exception {
        Class<?> c = Class.forName("java.lang.ThreadDeath");
        check(c.getSuperclass() == Error.class, "superclass is Error");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getDeclaredConstructors().length == 1, "exactly 1 declared constructor");
        check(new ThreadDeath() instanceof java.io.Serializable, "Serializable");
        // serialVersionUID locked empirically (identical on JDK-8 and JDK-27).
        long svuid = java.io.ObjectStreamClass.lookup(ThreadDeath.class).getSerialVersionUID();
        check(svuid == -4417128565033088268L, "serialVersionUID locked -4417128565033088268");
        // JDK-27 marks ThreadDeath @Deprecated (for removal); JDK-8 does not.
        Deprecated d = c.getAnnotation(Deprecated.class);
        check(d != null, "@Deprecated present on JDK-27");
        check(d.since().equals("20"), "@Deprecated since 20");
        check(d.forRemoval(), "@Deprecated for removal");
        // Thread.stop was removed in JDK-27 (JDK-8 still has it).
        try {
            Thread.class.getMethod("stop");
            throw new AssertionError("Thread.stop should be removed on JDK-27");
        } catch (NoSuchMethodException expected) {
            passed++;
        }
    }

    static void testInstanceSemanticsDeep() {
        ThreadDeath td = new ThreadDeath();
        check(td.getMessage() == null, "no message");
        check(td.getCause() == null, "no cause");
        check("java.lang.ThreadDeath".equals(td.toString()), "toString exact (no message part)");
        check(td.getSuppressed().length == 0, "no suppressed by default");
        check(td.getStackTrace()[0].getClassName().equals("ThreadDeathTest"), "top frame class");
        try {
            td.addSuppressed(null);
            throw new AssertionError("addSuppressed(null): expected NPE");
        } catch (NullPointerException npe) {
            check("Cannot suppress a null exception.".equals(npe.getMessage()),
                  "addSuppressed(null) exact message [JDK27]");
        }
        td.setStackTrace(new StackTraceElement[]{new StackTraceElement("T", "m", "T.java", 3)});
        check(td.getStackTrace().length == 1 && td.getStackTrace()[0].getMethodName().equals("m"),
              "setStackTrace round-trip");
        try {
            td.setStackTrace(new StackTraceElement[]{null});
            throw new AssertionError("setStackTrace({null}): expected NPE");
        } catch (NullPointerException npe) {
            check(npe.getMessage() != null && npe.getMessage().startsWith("stackTrace[0]"),
                  "setStackTrace({null}) message [JDK27]");
        }
    }

    static void testSerializationRoundTrip() throws Exception {
        ThreadDeath td = new ThreadDeath();
        td.setStackTrace(new StackTraceElement[]{new StackTraceElement("S", "t", "S.java", 9)});
        td.initCause(new Exception("cause-msg"));
        td.addSuppressed(new IllegalStateException("sup-msg"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(td);
        }
        ThreadDeath back = (ThreadDeath) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getClass() == ThreadDeath.class, "round-trip exact type");
        check(back.getMessage() == null, "round-trip message still null");
        check("cause-msg".equals(back.getCause().getMessage()), "round-trip cause preserved");
        check(back.getStackTrace().length == 1, "round-trip stack trace preserved");
        check(back.getStackTrace()[0].getLineNumber() == 9, "round-trip stack trace content");
        check(back.getSuppressed().length == 1, "round-trip suppressed preserved");
        check("sup-msg".equals(back.getSuppressed()[0].getMessage()), "round-trip suppressed content");
    }

    // Behavior difference vs JDK-8: since the removal of Thread.stop the
    // default uncaught-exception handler no longer special-cases
    // ThreadDeath; it is REPORTED like any other Throwable (JDK-8 is silent).
    // The observation threads are anchored in the TOP-LEVEL thread group so
    // that the platform default handler actually executes: harnesses such as
    // jtreg run the test inside their own ThreadGroup whose uncaughtException
    // calls System.exit on any non-ThreadDeath throwable, which would both
    // hijack the behavior under test and kill the JVM (verified empirically).
    static ThreadGroup topGroup() {
        ThreadGroup g = Thread.currentThread().getThreadGroup();
        while (g.getParent() != null) g = g.getParent();
        return g;
    }

    static void testUncaughtReported() throws Exception {
        java.io.PrintStream origErr = System.err;
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        System.setErr(new java.io.PrintStream(buf, true));
        try {
            ThreadGroup top = topGroup();
            java.util.concurrent.CountDownLatch gone = new java.util.concurrent.CountDownLatch(1);
            Thread t1 = new Thread(top, () -> {
                try { throw new ThreadDeath(); } finally { gone.countDown(); }
            });
            t1.start();
            gone.await();
            t1.join();
            check(buf.toString().contains("java.lang.ThreadDeath"),
                  "ThreadDeath uncaught is REPORTED on JDK-27 [vs silent JDK-8]");
            java.util.concurrent.CountDownLatch gone2 = new java.util.concurrent.CountDownLatch(1);
            Thread t2 = new Thread(top, () -> {
                try { throw new IllegalStateException("lcF-control-RTE"); } finally { gone2.countDown(); }
            });
            t2.start();
            gone2.await();
            t2.join();
            check(buf.toString().contains("lcF-control-RTE"),
                  "control RuntimeException also reported");
        } finally {
            System.setErr(origErr);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testClassFacts();
        testInstanceSemanticsDeep();
        testSerializationRoundTrip();
        testUncaughtReported();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
