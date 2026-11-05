/*
 * @test
 * @summary java.lang.IllegalCallerException conformance: white-box structure + black-box behavior.
 * @run main IllegalCallerExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class IllegalCallerExceptionTest {
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
        Class<?> c = Class.forName("java.lang.IllegalCallerException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 4, "4 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.IllegalCallerException");

        IllegalCallerException e0 = new IllegalCallerException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { IllegalCallerException e = new IllegalCallerException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }
        { Exception cause = new Exception("cause"); IllegalCallerException e = new IllegalCallerException("boom", cause); check("boom".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }
        { Exception cause = new Exception("cause"); IllegalCallerException e = new IllegalCallerException(cause); check(e.getCause() == cause, "(Throwable) ctor cause"); }

        // common Throwable contract
        IllegalCallerException t = new IllegalCallerException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.IllegalCallerException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    // ============================================================
    // ---- Part 3: contract deepening (2026-11, appended) ----
    // ============================================================

    // Locked via ObjectStreamClass.lookup on JDK 27.0.1 (declared in source).
    static final long EXPECTED_SVUID = -2349421918363102232L;

    // No deterministic in-java.lang trigger is reachable from this suite:
    // StackWalker.getCallerClass() with no caller frame throws IllegalCallerException,
    // but under jtreg the harness itself is the caller of main, so the bottom frame
    // always has a Java caller. Constructor-chain + Throwable contract coverage only.
    static void testToStringExact() {
        check("java.lang.IllegalCallerException: boom"
              .equals(new IllegalCallerException("boom").toString()), "toString exact with message");
        check("java.lang.IllegalCallerException"
              .equals(new IllegalCallerException().toString()), "toString exact without message");
        Exception cause = new Exception("why");
        check("java.lang.Exception: why".equals(new IllegalCallerException(cause).getMessage()),
              "(Throwable) ctor message defaults to cause.toString()");
        check(new IllegalCallerException(cause).toString()
              .equals("java.lang.IllegalCallerException: java.lang.Exception: why"),
              "toString embeds the default cause message");
    }

    static void testTypeHierarchy() {
        IllegalCallerException e = new IllegalCallerException("m");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(e instanceof Exception, "instanceof Exception");
        check(e instanceof Throwable, "instanceof Throwable");
        check(!java.lang.reflect.Modifier.isFinal(IllegalCallerException.class.getModifiers()),
              "IllegalCallerException is NOT final (extensible)");
        check(java.io.Serializable.class.isAssignableFrom(IllegalCallerException.class),
              "Serializable");
    }

    static void testSerialVersionUid() throws Exception {
        java.io.ObjectStreamClass osc =
            java.io.ObjectStreamClass.lookup(IllegalCallerException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.getSerialVersionUID() == EXPECTED_SVUID, "serialVersionUID locked (JDK-27)");
    }

    static void testSerializationRoundTrip() throws Exception {
        Exception cause = new Exception("c");
        IllegalCallerException original = new IllegalCallerException("boom", cause);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        java.io.ObjectInputStream ois =
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        IllegalCallerException copy = (IllegalCallerException) ois.readObject();
        check(copy instanceof IllegalCallerException, "round-trip type preserved");
        check("boom".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() instanceof Exception && "c".equals(copy.getCause().getMessage()),
              "round-trip cause preserved");
        check(copy.toString().equals(original.toString()), "round-trip toString equal");
    }

    static void testRealStackFrame() throws Exception {
        IllegalCallerException e = new IllegalCallerException("here");
        StackTraceElement[] st = e.getStackTrace();
        check(st.length >= 1, "fresh exception has a stack trace");
        check("IllegalCallerExceptionTest".equals(st[0].getClassName()), "[0] class is this test class");
        check("testRealStackFrame".equals(st[0].getMethodName()), "[0] method exact");
        check(st[0].getLineNumber() > 0, "[0] line positive");
        check(Class.forName(st[0].getClassName()) == IllegalCallerExceptionTest.class,
              "Class.forName(frame class) == test class");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
    }

    static void testSetStackTrace() {
        IllegalCallerException e = new IllegalCallerException("m");
        StackTraceElement[] custom = {
            new StackTraceElement("P", "q", "P.java", 3),
            new StackTraceElement("R", "s", "R.java", 4)
        };
        e.setStackTrace(custom);
        check(e.getStackTrace().length == 2, "setStackTrace length");
        check(e.getStackTrace()[0].equals(custom[0]), "setStackTrace [0] value");
        check("P".equals(e.getStackTrace()[0].getClassName())
              && "q".equals(e.getStackTrace()[0].getMethodName()), "setStackTrace getters");
        custom[0] = null;
        check(e.getStackTrace()[0] != null, "setStackTrace copies the array");
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "empty setStackTrace yields empty trace");
        expect(java.lang.NullPointerException.class,
               () -> e.setStackTrace(null), "setStackTrace(null)");
    }

    static void testSuppressedContract() {
        IllegalCallerException e = new IllegalCallerException("m");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        Throwable s1 = new Exception("s1");
        Throwable s2 = new Exception("s2");
        e.addSuppressed(s1);
        e.addSuppressed(s2);
        check(e.getSuppressed().length == 2, "two suppressed recorded");
        check(e.getSuppressed()[0] == s1 && e.getSuppressed()[1] == s2,
              "suppressed preserved in order");
        e.getSuppressed()[0] = null;
        check(e.getSuppressed()[0] == s1, "getSuppressed returns a defensive copy");
    }

    static void testPrintStackTraceContent() {
        IllegalCallerException e = new IllegalCallerException("boom");
        e.addSuppressed(new Exception("sup"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bos));
        String out = bos.toString();
        check(out.contains("java.lang.IllegalCallerException: boom"), "header with message");
        check(out.contains("Suppressed: java.lang.Exception: sup"), "Suppressed: line present");
        check(out.contains("\tat IllegalCallerExceptionTest.testPrintStackTraceContent("),
              "own frame listed with \\tat prefix");
    }

    static void testCauseEdgeCases() {
        IllegalCallerException e = new IllegalCallerException("m");
        expect(java.lang.IllegalArgumentException.class,
               () -> e.initCause(e), "initCause(self)");
        IllegalCallerException chained = new IllegalCallerException("outer",
                new IllegalCallerException("inner", new Exception("root")));
        check("root".equals(chained.getCause().getCause().getMessage()),
              "three-level cause chain traverses");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testToStringExact();
        testTypeHierarchy();
        testSerialVersionUid();
        testSerializationRoundTrip();
        testRealStackFrame();
        testSetStackTrace();
        testSuppressedContract();
        testPrintStackTraceContent();
        testCauseEdgeCases();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
