/*
 * @test
 * @summary java.lang.ArrayIndexOutOfBoundsException conformance: white-box structure + black-box behavior.
 * @run main ArrayIndexOutOfBoundsExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ArrayIndexOutOfBoundsExceptionTest {
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
        Class<?> c = Class.forName("java.lang.ArrayIndexOutOfBoundsException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == IndexOutOfBoundsException.class, "superclass is IndexOutOfBoundsException");
        check(c.getDeclaredConstructors().length == 3, "3 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ArrayIndexOutOfBoundsException");

        ArrayIndexOutOfBoundsException e0 = new ArrayIndexOutOfBoundsException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }
        { ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException(5); check(e.getMessage() != null, "(int) ctor non-null message"); }

        // common Throwable contract
        ArrayIndexOutOfBoundsException t = new ArrayIndexOutOfBoundsException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.ArrayIndexOutOfBoundsException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        // trigger: array access out of range
        expect(ArrayIndexOutOfBoundsException.class, () -> { int[] a = new int[1]; int x = a[1]; }, "trigger array access out of range");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
