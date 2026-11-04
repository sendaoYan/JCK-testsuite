/*
 * @test
 * @summary java.lang.RuntimeException API conformance: unchecked-exception hierarchy
 *          (RuntimeException -> Exception -> Throwable), all four constructors, and
 *          message/cause propagation.
 * @run main RuntimeExceptionTest
 */

public class RuntimeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        RuntimeException e = new RuntimeException();
        check(e instanceof Exception, "RuntimeException is an Exception");
        check(e instanceof Throwable, "RuntimeException is a Throwable");
    }

    static void testConstructors() {
        RuntimeException e1 = new RuntimeException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        RuntimeException e2 = new RuntimeException("msg");
        check("msg".equals(e2.getMessage()) && e2.getCause() == null, "(String) ctor");
        RuntimeException cause = new RuntimeException("cause");
        RuntimeException e3 = new RuntimeException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        RuntimeException e4 = new RuntimeException(cause);
        check(e4.getCause() == cause && cause.toString().equals(e4.getMessage()),
              "(Throwable) ctor message == cause.toString()");
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
