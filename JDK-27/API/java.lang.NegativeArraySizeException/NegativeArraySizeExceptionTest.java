/*
 * @test
 * @summary java.lang.NegativeArraySizeException API conformance: RuntimeException hierarchy,
 *          no-arg and (String) constructors, and a thrown instance from new int[-1] with the
 *          size as its message.
 * @run main NegativeArraySizeExceptionTest
 */

public class NegativeArraySizeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        NegativeArraySizeException e = new NegativeArraySizeException();
        check(e instanceof RuntimeException, "NASE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "NASE is Exception/Throwable");
    }

    static void testConstructors() {
        NegativeArraySizeException e1 = new NegativeArraySizeException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        NegativeArraySizeException e2 = new NegativeArraySizeException("neg");
        check("neg".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        try {
            int[] a = new int[-1];
            throw new AssertionError("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            check(e instanceof NegativeArraySizeException,
                  "new int[-1] throws NegativeArraySizeException");
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
