/*
 * @test
 * @summary java.lang.AssertionError conformance: white-box structure + black-box behavior.
 * @run main AssertionErrorTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class AssertionErrorTest {
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
        Class<?> c = Class.forName("java.lang.AssertionError");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == Error.class, "superclass is Error");
        check(c.getConstructors().length == 9, "9 public constructors (10 declared incl. private String)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.AssertionError");

        AssertionError e0 = new AssertionError();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { AssertionError e = new AssertionError("obj"); check(e.getMessage() != null, "(Object) ctor non-null message"); }
        { AssertionError e = new AssertionError(true); check(e.getMessage() != null, "(boolean) ctor non-null message"); }
        { AssertionError e = new AssertionError('x'); check(e.getMessage() != null, "(char) ctor non-null message"); }
        { AssertionError e = new AssertionError(7); check(e.getMessage() != null, "(int) ctor non-null message"); }
        { AssertionError e = new AssertionError(7L); check(e.getMessage() != null, "(long) ctor non-null message"); }
        { AssertionError e = new AssertionError(1.5f); check(e.getMessage() != null, "(float) ctor non-null message"); }
        { AssertionError e = new AssertionError(2.5); check(e.getMessage() != null, "(double) ctor non-null message"); }
        { Exception cause = new Exception("cause"); AssertionError e = new AssertionError("boom", cause); check("boom".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }

        // common Throwable contract
        AssertionError t = new AssertionError();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.AssertionError"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
