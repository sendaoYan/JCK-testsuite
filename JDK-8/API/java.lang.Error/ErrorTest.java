/*
 * @test
 * @summary java.lang.Error API conformance: hierarchy (Error -> Throwable, not Exception),
 *          all four constructors, and message/cause propagation.
 * @run main ErrorTest
 */

public class ErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
