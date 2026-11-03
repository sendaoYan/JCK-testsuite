/*
 * @test
 * @summary java.lang.Error conformance: white-box structure + black-box behavior.
 * @run main ErrorTest
 */

public class ErrorTest {
    static int passed;

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

    // ---- Part 2: black-box (behavioral) ----
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
