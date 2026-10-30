/*
 * @test
 * @summary java.lang.ReflectiveOperationException API conformance: checked-exception
 *          hierarchy, all four constructors, and that NoSuchMethodException and
 *          ClassNotFoundException extend it.
 * @run main ReflectiveOperationExceptionTest
 */

public class ReflectiveOperationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        ReflectiveOperationException e = new ReflectiveOperationException();
        check(e instanceof Exception, "ROE is an Exception");
        check(!((Object) e instanceof RuntimeException), "ROE is checked (not RuntimeException)");
        check(new NoSuchMethodException() instanceof ReflectiveOperationException,
              "NoSuchMethodException extends ROE");
        check(new ClassNotFoundException() instanceof ReflectiveOperationException,
              "ClassNotFoundException extends ROE");
    }

    static void testConstructors() {
        ReflectiveOperationException e1 = new ReflectiveOperationException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        ReflectiveOperationException e2 = new ReflectiveOperationException("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
        ReflectiveOperationException cause = new ReflectiveOperationException("cause");
        ReflectiveOperationException e3 = new ReflectiveOperationException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        ReflectiveOperationException e4 = new ReflectiveOperationException(cause);
        check(e4.getCause() == cause, "(Throwable) ctor sets cause");
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
