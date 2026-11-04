/*
 * @test
 * @summary java.lang.ArithmeticException API conformance: RuntimeException hierarchy,
 *          no-arg and (String) constructors, and the "/ by zero" message from integer
 *          division by zero.
 * @run main ArithmeticExceptionTest
 */

public class ArithmeticExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        ArithmeticException e = new ArithmeticException();
        check(e instanceof RuntimeException, "ArithmeticException is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable,
              "ArithmeticException is an Exception and Throwable");
    }

    static void testConstructors() {
        ArithmeticException e1 = new ArithmeticException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        ArithmeticException e2 = new ArithmeticException("divide");
        check("divide".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        int a = 1, b = 0;
        try {
            int x = a / b;
            check(x != 0, "unreachable"); // never reached: division by zero throws first
        } catch (ArithmeticException e) {
            check("/ by zero".equals(e.getMessage()), "division by zero message");
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
