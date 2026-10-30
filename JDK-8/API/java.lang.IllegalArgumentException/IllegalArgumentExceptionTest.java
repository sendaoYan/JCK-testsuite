/*
 * @test
 * @summary java.lang.IllegalArgumentException API conformance: RuntimeException hierarchy,
 *          all four constructors, and a thrown instance (Random.nextInt with a non-positive
 *          bound) with its "bound must be positive" message.
 * @run main IllegalArgumentExceptionTest
 */

public class IllegalArgumentExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        IllegalArgumentException e = new IllegalArgumentException();
        check(e instanceof RuntimeException, "IAE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "IAE is Exception/Throwable");
    }

    static void testConstructors() {
        IllegalArgumentException e1 = new IllegalArgumentException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        IllegalArgumentException e2 = new IllegalArgumentException("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
        IllegalArgumentException cause = new IllegalArgumentException("cause");
        IllegalArgumentException e3 = new IllegalArgumentException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        IllegalArgumentException e4 = new IllegalArgumentException(cause);
        check(e4.getCause() == cause, "(Throwable) ctor sets cause");
    }

    static void testThrown() {
        try {
            new java.util.Random().nextInt(0);
            throw new AssertionError("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check("bound must be positive".equals(e.getMessage()), "nextInt(0) message");
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
