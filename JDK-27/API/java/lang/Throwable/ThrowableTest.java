/*
 * @test
 * @summary java.lang.Throwable conformance: white-box structure + black-box behavior.
 * @run main ThrowableTest
 */

import java.lang.reflect.Modifier;

public class ThrowableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Custom extends Throwable {
        Custom(String m) { super(m); }
    }

    // Exposes the protected (String, Throwable, boolean, boolean) constructor.
    static class SuppressionThrowable extends Throwable {
        SuppressionThrowable(String m, Throwable c, boolean enableSuppression, boolean writableStackTrace) {
            super(m, c, enableSuppression, writableStackTrace);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Throwable");
        check(!c.isInterface(), "Throwable is a class");
        check(c.getSuperclass() == Object.class, "Throwable extends Object");
        check(!Modifier.isFinal(c.getModifiers()), "Throwable is subclassable");
        check(java.io.Serializable.class.isAssignableFrom(c), "Throwable is Serializable");
        check(c.getConstructor() != null && c.getConstructor(String.class) != null
              && c.getConstructor(String.class, Throwable.class) != null
              && c.getConstructor(Throwable.class) != null, "four constructors present");
    }

    // ---- Part 2: black-box (behavioral) ----
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

    // ---- Part 2b: black-box for the protected (String, Throwable, boolean, boolean) ctor ----
    static void testBlackBox() {
        Exception cause = new Exception("cause");

        SuppressionThrowable e = new SuppressionThrowable("m", cause, false, false);
        check("m".equals(e.getMessage()) && e.getCause() == cause, "4-arg ctor sets message and cause");
        e.addSuppressed(new Exception("s"));
        check(e.getSuppressed().length == 0, "enableSuppression=false -> addSuppressed is a no-op");
        e.setStackTrace(new StackTraceElement[]{ new StackTraceElement("C", "m", "F.java", 1) });
        check(e.getStackTrace().length == 0, "writableStackTrace=false -> stack trace stays empty");

        SuppressionThrowable e2 = new SuppressionThrowable("m2", cause, true, true);
        e2.addSuppressed(new Exception("s2"));
        check(e2.getSuppressed().length == 1, "enableSuppression=true -> addSuppressed records");
        check(e2.getStackTrace().length > 0, "writableStackTrace=true -> stack trace filled");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testInitCause();
        testSuppressed();
        testStackTrace();
        testToStringAndMessage();
        testPrintStackTrace();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
