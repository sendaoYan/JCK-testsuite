/*
 * @test
 * @summary java.lang.Exception conformance: white-box structure + black-box behavior.
 * @run main ExceptionTest
 */

public class ExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Exception");
        check(c.getSuperclass() == Throwable.class, "Exception extends Throwable");
        check(!c.isInterface(), "Exception is a class");
        check(c.getConstructor() != null && c.getConstructor(String.class) != null
              && c.getConstructor(String.class, Throwable.class) != null
              && c.getConstructor(Throwable.class) != null, "four constructors present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testHierarchy() {
        Exception e = new Exception();
        check(e instanceof Throwable, "Exception is a Throwable");
        check(!(e instanceof RuntimeException), "Exception is not a RuntimeException (checked)");
    }

    static void testConstructors() {
        Exception e1 = new Exception();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        Exception e2 = new Exception("msg");
        check("msg".equals(e2.getMessage()) && e2.getCause() == null, "(String) ctor");
        Exception cause = new Exception("cause");
        Exception e3 = new Exception("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        Exception e4 = new Exception(cause);
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
