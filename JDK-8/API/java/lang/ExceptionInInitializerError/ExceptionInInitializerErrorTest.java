/*
 * @test
 * @summary java.lang.ExceptionInInitializerError API conformance: LinkageError hierarchy,
 *          constructors, and a thrown instance wrapping the exception from a failing
 *          static initializer, exposed via getCause()/getException().
 * @run main ExceptionInInitializerErrorTest
 */

public class ExceptionInInitializerErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class BadInit {
        static {
            String s = null;
            s.length(); // throws NullPointerException during <clinit>
        }
    }

    static void testHierarchy() {
        ExceptionInInitializerError e = new ExceptionInInitializerError();
        check(e instanceof LinkageError, "EIIE is a LinkageError");
        check(e instanceof Error, "EIIE is an Error");
    }

    static void testConstructors() {
        ExceptionInInitializerError e1 = new ExceptionInInitializerError();
        check(e1.getMessage() == null, "no-arg ctor null message");
        ExceptionInInitializerError e2 = new ExceptionInInitializerError("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
        Throwable cause = new Throwable("cause");
        ExceptionInInitializerError e3 = new ExceptionInInitializerError(cause);
        check(e3.getCause() == cause, "(Throwable) ctor sets cause");
    }

    static void testThrown() {
        try {
            new BadInit();
            throw new AssertionError("expected ExceptionInInitializerError");
        } catch (ExceptionInInitializerError e) {
            check(e.getCause() instanceof NullPointerException,
                  "cause is the exception from <clinit>");
            check(e.getException() == e.getCause(), "getException returns the cause");
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
