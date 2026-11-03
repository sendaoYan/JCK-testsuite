/*
 * @test
 * @summary java.lang.IndexOutOfBoundsException conformance: white-box structure + black-box behavior.
 * @run main IndexOutOfBoundsExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class IndexOutOfBoundsExceptionTest {
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
        Class<?> c = Class.forName("java.lang.IndexOutOfBoundsException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 4, "4 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.IndexOutOfBoundsException");

        IndexOutOfBoundsException e0 = new IndexOutOfBoundsException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { IndexOutOfBoundsException e = new IndexOutOfBoundsException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }
        { IndexOutOfBoundsException e = new IndexOutOfBoundsException(5); check(e.getMessage() != null, "(int) ctor non-null message"); }
        { IndexOutOfBoundsException e = new IndexOutOfBoundsException(5L); check(e.getMessage() != null, "(long) ctor non-null message"); }

        // common Throwable contract
        IndexOutOfBoundsException t = new IndexOutOfBoundsException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.IndexOutOfBoundsException"), "toString prefix");
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
