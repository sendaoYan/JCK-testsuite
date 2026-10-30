/*
 * @test
 * @summary JLS 4.2.2 / 15.17.2 / 15.17.3: long arithmetic wraps modulo 2^64;
 *          Long.MAX_VALUE + 1 == Long.MIN_VALUE, Long.MIN_VALUE - 1 ==
 *          Long.MAX_VALUE, 2^32 * 2^32 wraps to 0, and Long.MIN_VALUE / -1
 *          wraps to Long.MIN_VALUE (no ArithmeticException) while
 *          Long.MIN_VALUE % -1 == 0.  Operands route through method parameters
 *          so javac cannot constant-fold the arithmetic.
 * @run main LongBoundaryArithmetic
 */

public class LongBoundaryArithmetic {
    static long add(long a, long b) { return a + b; }
    static long sub(long a, long b) { return a - b; }
    static long mul(long a, long b) { return a * b; }
    static long div(long a, long b) { return a / b; }
    static long mod(long a, long b) { return a % b; }

    public static void main(String[] args) {
        // overflow/underflow wrap silently.
        if (add(Long.MAX_VALUE, 1L) != Long.MIN_VALUE) throw new AssertionError("MAX+1 must wrap to MIN");
        if (sub(Long.MIN_VALUE, 1L) != Long.MAX_VALUE) throw new AssertionError("MIN-1 must wrap to MAX");
        if (mul(4294967296L, 4294967296L) != 0L) throw new AssertionError("2^32 * 2^32 must wrap to 0");

        // JLS 15.17.2/15.17.3: MIN_VALUE / -1 wraps to MIN_VALUE (no exception),
        // MIN_VALUE % -1 == 0.
        if (div(Long.MIN_VALUE, -1L) != Long.MIN_VALUE) throw new AssertionError("MIN / -1 must wrap to MIN");
        if (mod(Long.MIN_VALUE, -1L) != 0L) throw new AssertionError("MIN % -1 must be 0");
        if (div(Long.MIN_VALUE, 1L) != Long.MIN_VALUE) throw new AssertionError("MIN / 1 must be MIN");
        if (mod(Long.MIN_VALUE, 1L) != 0L) throw new AssertionError("MIN % 1 must be 0");

        // division/remainder by zero throw ArithmeticException.
        try { div(1L, 0L); throw new AssertionError("long / 0 did not throw"); }
        catch (ArithmeticException expected) { /* expected */ }
        try { mod(1L, 0L); throw new AssertionError("long % 0 did not throw"); }
        catch (ArithmeticException expected) { /* expected */ }

        System.out.println("PASS");
    }
}
