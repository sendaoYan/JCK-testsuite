/*
 * @test
 * @summary java.lang.Error API conformance: hierarchy (Error -> Throwable, not Exception),
 *          all four constructors, and message/cause propagation.
 * @run main ErrorTest
 */

public class ErrorTest {
    static int passed;

    // Exposes the protected (String, Throwable, boolean, boolean) constructor.
    static class SuppressionError extends Error {
        SuppressionError(String m, Throwable c, boolean enableSuppression, boolean writableStackTrace) {
            super(m, c, enableSuppression, writableStackTrace);
        }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Error");
        check(c.getSuperclass() == Throwable.class, "Error extends Throwable");
        check(!c.isInterface(), "Error is a class");
        check(c.getConstructor() != null && c.getConstructor(String.class) != null
              && c.getConstructor(String.class, Throwable.class) != null
              && c.getConstructor(Throwable.class) != null, "four constructors present");
    }

    static void testHierarchy() {
        Throwable t = new Error();
        check(t instanceof Error, "Error is a Throwable");
        check(!(t instanceof Exception), "Error is not an Exception");
    }

    static void testConstructors() {
        Error e1 = new Error();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        Error e2 = new Error("msg");
        check("msg".equals(e2.getMessage()) && e2.getCause() == null, "(String) ctor");
        Error cause = new Error("cause");
        Error e3 = new Error("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        Error e4 = new Error(cause);
        check(e4.getCause() == cause && cause.toString().equals(e4.getMessage()),
              "(Throwable) ctor message == cause.toString()");
    }

    // ---- Part 2b: protected (String, Throwable, boolean, boolean) ctor ----
    static void testBlackBox() {
        Exception cause = new Exception("cause");

        SuppressionError e = new SuppressionError("m", cause, false, false);
        check("m".equals(e.getMessage()) && e.getCause() == cause, "4-arg ctor sets message and cause");
        e.addSuppressed(new Exception("s"));
        check(e.getSuppressed().length == 0, "enableSuppression=false -> addSuppressed is a no-op");
        e.setStackTrace(new StackTraceElement[]{ new StackTraceElement("C", "m", "F.java", 1) });
        check(e.getStackTrace().length == 0, "writableStackTrace=false -> stack trace stays empty");

        SuppressionError e2 = new SuppressionError("m2", cause, true, true);
        e2.addSuppressed(new Exception("s2"));
        check(e2.getSuppressed().length == 1, "enableSuppression=true -> addSuppressed records");
        check(e2.getStackTrace().length > 0, "writableStackTrace=true -> stack trace filled");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testHierarchy();
        testConstructors();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
