/*
 * @test
 * @summary JLS 4.2.2: integer operators use 32-bit (int) or 64-bit (long)
 *          precision depending on the operands; overflow/underflow are not
 *          indicated (they wrap); / and % by zero throw ArithmeticException.
 * @run main IntegerOperations
 */

public class IntegerOperations {
    public static void main(String[] args) {
        // Example 4.2.2-1: int multiplication overflows to the low 32 bits.
        int i = 1000000;
        check(i * i, -727379968, "int overflow wraps");

        long l = i;
        check(l * l, 1000000000000L, "long multiplication keeps full precision");

        // Integer overflow/underflow wrap silently.
        check(Integer.MAX_VALUE + 1, Integer.MIN_VALUE, "int overflow to MIN_VALUE");
        check(Integer.MIN_VALUE - 1, Integer.MAX_VALUE, "int underflow to MAX_VALUE");
        check(Long.MAX_VALUE + 1, Long.MIN_VALUE, "long overflow to MIN_VALUE");

        // Integer division by zero throws ArithmeticException.
        try {
            int x = 20296 / (int) (l - i);   // l - i == 0
            throw new AssertionError("division by zero did not throw");
        } catch (ArithmeticException expected) {
            // expected
        }

        // Integer remainder by zero throws ArithmeticException.
        try {
            int x = 5 % 0;
            throw new AssertionError("remainder by zero did not throw");
        } catch (ArithmeticException expected) {
            // expected
        }

        // A shift operation does not signal overflow (masking of shift count).
        check(1 << 31, Integer.MIN_VALUE, "1 << 31");

        System.out.println("PASS");
    }

    static void check(int actual, int expected, String what) {
        if (actual != expected) {
            throw new AssertionError(what + ": expected " + expected + ", got " + actual);
        }
    }

    static void check(long actual, long expected, String what) {
        if (actual != expected) {
            throw new AssertionError(what + ": expected " + expected + ", got " + actual);
        }
    }
}
