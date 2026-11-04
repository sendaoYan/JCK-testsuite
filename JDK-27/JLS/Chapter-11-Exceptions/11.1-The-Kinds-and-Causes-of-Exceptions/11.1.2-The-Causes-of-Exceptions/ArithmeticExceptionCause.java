/*
 * @test
 * @summary JLS 11.1.2: an abnormal execution condition synchronously detected by
 *          the JVM (integer divide by zero) causes an ArithmeticException.
 * @run main ArithmeticExceptionCause
 */

public class ArithmeticExceptionCause {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int zero = 0;
        boolean threw = false;
        try {
            int x = 1 / zero;
        } catch (ArithmeticException expected) {
            threw = true;
        }
        check(threw, "integer divide by zero throws ArithmeticException");

        // Remainder by zero also throws ArithmeticException.
        threw = false;
        try {
            int y = 1 % zero;
        } catch (ArithmeticException expected) {
            threw = true;
        }
        check(threw, "integer remainder by zero throws ArithmeticException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
