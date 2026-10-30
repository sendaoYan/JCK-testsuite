/*
 * @test
 * @summary java.lang.Exception API conformance: checked-exception hierarchy (Exception ->
 *          Throwable, not RuntimeException), all four constructors, and message/cause
 *          propagation including the (Throwable) constructor's message == cause.toString().
 * @run main ExceptionTest
 */

public class ExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
