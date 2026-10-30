/*
 * @test
 * @summary java.lang.NumberFormatException API conformance: IllegalArgumentException
 *          hierarchy, no-arg and (String) constructors, and Integer.parseInt throwing NFE
 *          (including the null-input case, which is NFE rather than NPE).
 * @run main NumberFormatExceptionTest
 */

public class NumberFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        NumberFormatException e = new NumberFormatException();
        check(e instanceof IllegalArgumentException, "NFE is an IllegalArgumentException");
        check(e instanceof RuntimeException, "NFE is a RuntimeException");
    }

    static void testConstructors() {
        NumberFormatException e1 = new NumberFormatException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        NumberFormatException e2 = new NumberFormatException("bad");
        check("bad".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        try {
            Integer.parseInt("not-a-number");
            throw new AssertionError("expected NumberFormatException");
        } catch (NumberFormatException e) {
            check("For input string: \"not-a-number\"".equals(e.getMessage()), "parseInt message");
        }
        try {
            Integer.parseInt(null);
            throw new AssertionError("parseInt(null): expected NumberFormatException");
        } catch (NumberFormatException e) {
            check(e instanceof NumberFormatException, "parseInt(null) throws NFE (not NPE)");
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
