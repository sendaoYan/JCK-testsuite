/*
 * @test
 * @summary java.lang.SecurityException API conformance: RuntimeException hierarchy and all
 *          four constructors.
 * @run main SecurityExceptionTest
 */

public class SecurityExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        SecurityException e = new SecurityException();
        check(e instanceof RuntimeException, "SecurityException is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable,
              "SecurityException is Exception/Throwable");
    }

    static void testConstructors() {
        SecurityException e1 = new SecurityException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        SecurityException e2 = new SecurityException("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
        SecurityException cause = new SecurityException("cause");
        SecurityException e3 = new SecurityException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        SecurityException e4 = new SecurityException(cause);
        check(e4.getCause() == cause, "(Throwable) ctor sets cause");
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
