/*
 * @test
 * @summary java.lang.ExceptionInInitializerError conformance: white-box structure + black-box behavior.
 * @run main ExceptionInInitializerErrorTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ExceptionInInitializerErrorTest {
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
        Class<?> c = Class.forName("java.lang.ExceptionInInitializerError");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == LinkageError.class, "superclass is LinkageError");
        check(c.getDeclaredConstructors().length == 3, "3 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ExceptionInInitializerError");

        ExceptionInInitializerError e0 = new ExceptionInInitializerError();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { Exception cause = new Exception("cause"); ExceptionInInitializerError e = new ExceptionInInitializerError(cause); check(e.getCause() == cause, "(Throwable) ctor cause"); }
        { ExceptionInInitializerError e = new ExceptionInInitializerError("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        ExceptionInInitializerError t = new ExceptionInInitializerError();
        check(t.getCause() == null, "fresh cause null");
        // All constructors pin the cause (serialization compatibility), so
        // initCause always fails with IllegalStateException (javadoc-specified).
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("cause")), "initCause always rejected");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.ExceptionInInitializerError"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        { ExceptionInInitializerError e = new ExceptionInInitializerError(new Exception("c")); check(e.getException() != null && "c".equals(e.getException().getMessage()), "getException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
