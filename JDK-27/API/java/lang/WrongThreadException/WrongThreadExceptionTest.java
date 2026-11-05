/*
 * @test
 * @summary java.lang.WrongThreadException conformance: white-box structure + black-box behavior.
 * @run main WrongThreadExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class WrongThreadExceptionTest {
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
        Class<?> c = Class.forName("java.lang.WrongThreadException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 4, "4 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.WrongThreadException");

        WrongThreadException e0 = new WrongThreadException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { WrongThreadException e = new WrongThreadException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }
        { Exception cause = new Exception("cause"); WrongThreadException e = new WrongThreadException("boom", cause); check("boom".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }
        { Exception cause = new Exception("cause"); WrongThreadException e = new WrongThreadException(cause); check(e.getCause() == cause, "(Throwable) ctor cause"); }

        // common Throwable contract
        WrongThreadException t = new WrongThreadException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.WrongThreadException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    // ============================================================
    // ---- Part 3: contract deepening (2026-11, appended) ----
    // ============================================================

    // Locked via ObjectStreamClass.lookup on JDK 27.0.1 (declared in source).
    static final long EXPECTED_SVUID = 4676498871006316905L;

    static void testToStringExact() {
        check("java.lang.WrongThreadException: boom"
              .equals(new WrongThreadException("boom").toString()), "toString exact with message");
        check("java.lang.WrongThreadException"
              .equals(new WrongThreadException().toString()), "toString exact without message");
        Exception cause = new Exception("why");
        check("java.lang.WrongThreadException: java.lang.Exception: why"
              .equals(new WrongThreadException(cause).toString()),
              "toString embeds the default cause.toString() message");
        check("java.lang.Exception: why".equals(new WrongThreadException(cause).getMessage()),
              "(Throwable) ctor message defaults to cause.toString()");
    }

    static void testTypeHierarchy() {
        WrongThreadException e = new WrongThreadException("m");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(e instanceof Exception, "instanceof Exception");
        check(e instanceof Throwable, "instanceof Throwable");
        check(java.lang.reflect.Modifier.isFinal(WrongThreadException.class.getModifiers()),
              "WrongThreadException is final");
        check(java.io.Serializable.class.isAssignableFrom(WrongThreadException.class),
              "Serializable");
    }

    static void testSerialVersionUid() throws Exception {
        java.io.ObjectStreamClass osc =
            java.io.ObjectStreamClass.lookup(WrongThreadException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.getSerialVersionUID() == EXPECTED_SVUID, "serialVersionUID locked (JDK-27)");
    }

    static void testSerializationRoundTrip() throws Exception {
        Exception cause = new Exception("c");
        WrongThreadException original = new WrongThreadException("boom", cause);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        java.io.ObjectInputStream ois =
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        WrongThreadException copy = (WrongThreadException) ois.readObject();
        check(copy instanceof WrongThreadException, "round-trip type preserved");
        check("boom".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() instanceof Exception && "c".equals(copy.getCause().getMessage()),
              "round-trip cause preserved");
        check(copy.toString().equals(original.toString()), "round-trip toString equal");
    }

    static void testRealStackFrame() throws Exception {
        WrongThreadException e = new WrongThreadException("here");
        StackTraceElement[] st = e.getStackTrace();
        check(st.length >= 1, "fresh exception has a stack trace");
        check("WrongThreadExceptionTest".equals(st[0].getClassName()), "[0] class is this test class");
        check("testRealStackFrame".equals(st[0].getMethodName()), "[0] method exact");
        check(st[0].getLineNumber() > 0, "[0] line positive");
        check(Class.forName(st[0].getClassName()) == WrongThreadExceptionTest.class,
              "Class.forName(frame class) == test class");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
    }

    static void testSetStackTrace() {
        WrongThreadException e = new WrongThreadException("m");
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
        WrongThreadException e = new WrongThreadException("m");
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
        WrongThreadException e = new WrongThreadException("boom");
        e.addSuppressed(new Exception("sup"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bos));
        String out = bos.toString();
        check(out.contains("java.lang.WrongThreadException: boom"), "header with message");
        check(out.contains("Suppressed: java.lang.Exception: sup"), "Suppressed: line present");
        check(out.contains("\tat WrongThreadExceptionTest.testPrintStackTraceContent("),
              "own frame listed with \\tat prefix");
    }

    static void testCauseEdgeCases() {
        WrongThreadException e = new WrongThreadException("m");
        expect(java.lang.IllegalArgumentException.class,
               () -> e.initCause(e), "initCause(self)");
        WrongThreadException chained = new WrongThreadException("outer",
                new WrongThreadException("inner", new Exception("root")));
        check(chained.getCause().getCause().getMessage().equals("root"),
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
