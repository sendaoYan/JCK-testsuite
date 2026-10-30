/*
 * @test
 * @summary java.lang.Throwable API conformance: constructors, getMessage/getCause/initCause,
 *          addSuppressed/getSuppressed, getStackTrace/setStackTrace/fillInStackTrace,
 *          toString/getLocalizedMessage/printStackTrace, and argument validation.
 * @run main ThrowableTest
 */

public class ThrowableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Custom extends Throwable {
        Custom(String m) { super(m); }
    }

    static void testConstructors() {
        Throwable t1 = new Throwable();
        check(t1.getMessage() == null && t1.getCause() == null, "no-arg ctor");
        Throwable t2 = new Throwable("msg");
        check("msg".equals(t2.getMessage()) && t2.getCause() == null, "(String) ctor");
        Exception cause = new Exception("cause");
        Throwable t3 = new Throwable("msg", cause);
        check("msg".equals(t3.getMessage()) && t3.getCause() == cause, "(String,Throwable) ctor");
        Throwable t4 = new Throwable(cause);
        check(t4.getCause() == cause, "(Throwable) ctor sets cause");
        check(cause.toString().equals(t4.getMessage()), "(Throwable) ctor message == cause.toString()");
    }

    static void testInitCause() {
        Throwable t = new Throwable();
        check(t.getCause() == null, "cause null initially");
        Exception c = new Exception("cause");
        check(t.initCause(c) == t, "initCause returns this");
        check(t.getCause() == c, "getCause returns set cause");
        try {
            t.initCause(new Exception("other"));
            throw new AssertionError("initCause twice: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }

        Throwable fresh = new Throwable();
        try {
            fresh.initCause(fresh);
            throw new AssertionError("initCause(self): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        Throwable withCause = new Throwable(c);
        try {
            withCause.initCause(new Exception("x"));
            throw new AssertionError("initCause on already-initialized: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
    }

    static void testSuppressed() {
        Throwable t = new Throwable("primary");
        check(t.getSuppressed().length == 0, "no suppressed initially");
        try {
            t.addSuppressed(null);
            throw new AssertionError("addSuppressed(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            t.addSuppressed(t);
            throw new AssertionError("addSuppressed(self): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        Exception s = new Exception("suppressed");
        t.addSuppressed(s);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == s, "addSuppressed adds");
        t.getSuppressed()[0] = null;
        check(t.getSuppressed()[0] == s, "getSuppressed returns a clone");
    }

    static void testStackTrace() {
        Throwable t = new Throwable();
        try {
            t.setStackTrace(null);
            throw new AssertionError("setStackTrace(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            t.setStackTrace(new StackTraceElement[] { null });
            throw new AssertionError("setStackTrace with null element: expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }

        StackTraceElement[] st = new StackTraceElement[] {
            new StackTraceElement("C", "m", "F.java", 1)
        };
        t.setStackTrace(st);
        check(t.getStackTrace().length == 1 && t.getStackTrace()[0].equals(st[0]),
              "setStackTrace/getStackTrace roundtrip");
        st[0] = new StackTraceElement("X", "y", "Z.java", 2);
        check(t.getStackTrace()[0].equals(new StackTraceElement("C", "m", "F.java", 1)),
              "setStackTrace clones the array");

        Throwable t2 = new Throwable();
        check(t2.fillInStackTrace() == t2, "fillInStackTrace returns this");
        check(t2.getStackTrace().length > 0, "fillInStackTrace records a frame");
    }

    static void testToStringAndMessage() {
        check(new Throwable("msg").getMessage().equals("msg"), "getMessage");
        check(new Throwable("msg").getLocalizedMessage().equals("msg"),
              "getLocalizedMessage defaults to getMessage");
        check(new Throwable().toString().equals("java.lang.Throwable"), "toString no message");
        check(new Throwable("boom").toString().equals("java.lang.Throwable: boom"),
              "toString with message");
        check(new Custom("m").toString().equals("ThrowableTest$Custom: m"),
              "toString subclass with message");
    }

    static void testPrintStackTrace() {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        Throwable t = new Throwable("x");
        t.printStackTrace(new java.io.PrintStream(bos));
        check(bos.toString().contains("Throwable: x"), "printStackTrace writes to stream");
    }

    public static void main(String[] args) {
        testConstructors();
        testInitCause();
        testSuppressed();
        testStackTrace();
        testToStringAndMessage();
        testPrintStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
