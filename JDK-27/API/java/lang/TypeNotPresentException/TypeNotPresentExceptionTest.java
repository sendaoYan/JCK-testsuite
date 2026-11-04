/*
 * @test
 * @summary java.lang.TypeNotPresentException conformance: white-box structure + black-box behavior.
 * @run main TypeNotPresentExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TypeNotPresentExceptionTest {
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
        Class<?> c = Class.forName("java.lang.TypeNotPresentException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 1, "1 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.TypeNotPresentException");

        { Exception cause = new Exception("cause"); TypeNotPresentException e = new TypeNotPresentException("jck.Type", cause); check("Type jck.Type not present".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }

        // common Throwable contract
        TypeNotPresentException t = new TypeNotPresentException("jck.Type", new Exception("cause"));
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set by constructor");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause after ctor-set cause");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.TypeNotPresentException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        { TypeNotPresentException e = new TypeNotPresentException("jck.Type", new Exception("c")); check("jck.Type".equals(e.typeName()), "typeName"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
