/*
 * @test
 * @summary java.lang.ClassNotFoundException API conformance: ReflectiveOperationException
 *          hierarchy, no-arg/(String)/(String,Throwable) constructors, and getException()
 *          returning the cause.
 * @run main ClassNotFoundExceptionTest
 */

public class ClassNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        ClassNotFoundException e = new ClassNotFoundException();
        check(e instanceof ReflectiveOperationException, "CNFE is a ReflectiveOperationException");
        check(e instanceof Exception, "CNFE is an Exception");
        check(!((Object) e instanceof RuntimeException), "CNFE is checked (not RuntimeException)");
    }

    static void testConstructors() {
        ClassNotFoundException e1 = new ClassNotFoundException();
        check(e1.getMessage() == null && e1.getException() == null, "no-arg ctor");
        ClassNotFoundException e2 = new ClassNotFoundException("C");
        check("C".equals(e2.getMessage()) && e2.getException() == null, "(String) ctor");
        Throwable cause = new Throwable("cause");
        ClassNotFoundException e3 = new ClassNotFoundException("C", cause);
        check("C".equals(e3.getMessage()), "(String,Throwable) ctor message");
        check(e3.getException() == cause, "getException returns the cause");
        check(e3.getCause() == cause, "getCause returns the cause");
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
