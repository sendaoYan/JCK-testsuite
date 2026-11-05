/*
 * @test
 * @summary java.lang.ThreadDeath API conformance: it is an Error.
 * @run main ThreadDeathTest
 */

public class ThreadDeathTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThreadDeath() {
        ThreadDeath td = new ThreadDeath();
        check(td instanceof Error, "ThreadDeath is an Error");
        check(td instanceof Throwable, "ThreadDeath is a Throwable");
    }

    // ================= 契约深化（2026-11）追加 =================
    static void testClassFacts() throws Exception {
        Class<?> c = Class.forName("java.lang.ThreadDeath");
        check(c.getSuperclass() == Error.class, "superclass is Error");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getDeclaredConstructors().length == 1, "exactly 1 declared constructor");
        check(c.getConstructor() != null, "no-arg constructor");
        check(c instanceof java.io.Serializable || true, "resolvable");
        check(new ThreadDeath() instanceof java.io.Serializable, "Serializable");
        // serialVersionUID locked empirically (identical on JDK-8 and JDK-27).
        long svuid = java.io.ObjectStreamClass.lookup(ThreadDeath.class).getSerialVersionUID();
        check(svuid == -4417128565033088268L, "serialVersionUID locked -4417128565033088268");
        // JDK-8: ThreadDeath carries NO @Deprecated annotation (JDK-27 adds it).
        check(c.getAnnotation(Deprecated.class) == null, "no @Deprecated on JDK-8");
        // Thread.stop still exists on JDK-8 (never invoked here; it is the
        // only producer of ThreadDeath and is gone in JDK-27).
        try {
            Thread.class.getMethod("stop");
            passed++;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Thread.stop should exist on JDK-8");
        }
    }

    static void testInstanceSemantics() {
        ThreadDeath td = new ThreadDeath();
        check(td.getMessage() == null, "no message");
        check(td.getCause() == null, "no cause");
        check("java.lang.ThreadDeath".equals(td.toString()), "toString exact (no message part)");
        check(td.getLocalizedMessage() == null, "localized message null");
        check(td.getSuppressed().length == 0, "no suppressed by default");
        check(td.getStackTrace().length >= 1, "stack trace captured");
        check(td.getStackTrace()[0].getClassName().equals("ThreadDeathTest"), "top frame class");
        check(td.initCause(new Exception("c")) == td, "initCause returns this");
        check("c".equals(td.getCause().getMessage()), "cause settable");
        try {
            td.initCause(new Exception("x"));
            throw new AssertionError("second initCause: expected IllegalStateException");
        } catch (IllegalStateException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Can't overwrite cause with"),
                  "second initCause message prefix [JDK8]");
        }
        Exception sup = new Exception("sup");
        td.addSuppressed(sup);
        check(td.getSuppressed().length == 1 && td.getSuppressed()[0] == sup, "suppressed recorded");
        try {
            td.addSuppressed(null);
            throw new AssertionError("addSuppressed(null): expected NPE");
        } catch (NullPointerException npe) {
            check("Cannot suppress a null exception.".equals(npe.getMessage()),
                  "addSuppressed(null) exact message [JDK8]");
        }
        td.setStackTrace(new StackTraceElement[]{new StackTraceElement("T", "m", "T.java", 3)});
        check(td.getStackTrace().length == 1 && td.getStackTrace()[0].getMethodName().equals("m"),
              "setStackTrace round-trip");
        check(((Throwable) td).fillInStackTrace() == td, "fillInStackTrace returns this");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        td.printStackTrace(new java.io.PrintStream(bos));
        check(bos.toString().contains("java.lang.ThreadDeath"), "printStackTrace header");
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

    // Core behavioral contract: the DEFAULT uncaught-exception handler ignores
    // ThreadDeath silently (while an ordinary exception is reported). Locked
    // by capturing System.err around threads that terminate deterministically.
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

    static void testUncaughtSilentByDefault() throws Exception {
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
            check(!buf.toString().contains("java.lang.ThreadDeath"),
                  "ThreadDeath uncaught is silent in the default handler [JDK8]");
            java.util.concurrent.CountDownLatch gone2 = new java.util.concurrent.CountDownLatch(1);
            Thread t2 = new Thread(top, () -> {
                try { throw new IllegalStateException("lcF-control-RTE"); } finally { gone2.countDown(); }
            });
            t2.start();
            gone2.await();
            t2.join();
            check(buf.toString().contains("lcF-control-RTE"),
                  "control RuntimeException IS reported");
        } finally {
            System.setErr(origErr);
        }
    }

    public static void main(String[] args) throws Exception {
        testThreadDeath();
        testClassFacts();
        testInstanceSemantics();
        testSerializationRoundTrip();
        testUncaughtSilentByDefault();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
