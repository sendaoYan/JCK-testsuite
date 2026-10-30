/*
 * @test
 * @summary java.lang.IllegalStateException API conformance: RuntimeException hierarchy,
 *          all four constructors, and a thrown instance (Iterator.remove before next()).
 * @run main IllegalStateExceptionTest
 */

public class IllegalStateExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        IllegalStateException e = new IllegalStateException();
        check(e instanceof RuntimeException, "ISE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "ISE is Exception/Throwable");
    }

    static void testConstructors() {
        IllegalStateException e1 = new IllegalStateException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        IllegalStateException e2 = new IllegalStateException("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
        IllegalStateException cause = new IllegalStateException("cause");
        IllegalStateException e3 = new IllegalStateException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        IllegalStateException e4 = new IllegalStateException(cause);
        check(e4.getCause() == cause, "(Throwable) ctor sets cause");
    }

    static void testThrown() {
        java.util.Iterator<String> it = new java.util.ArrayList<String>().iterator();
        try {
            it.remove();
            throw new AssertionError("expected IllegalStateException");
        } catch (IllegalStateException e) {
            check(e instanceof IllegalStateException, "Iterator.remove before next() throws ISE");
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
