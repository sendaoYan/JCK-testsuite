/*
 * @test
 * @summary JLS 15.17/15.18/15.19: integer arithmetic at the value-set
 *          boundaries, evaluated at run time (operands passed through method
 *          parameters so javac cannot constant-fold them).  Multiplication and
 *          addition wrap mod 2^32, division by zero throws ArithmeticException,
 *          MIN_VALUE/-1 overflows to the dividend (MIN_VALUE) and MIN_VALUE%-1 is
 *          0, the remainder takes the dividend's sign, and shift distances are
 *          masked mod 32 (int) or mod 64 (long).
 * @run main IntBoundaryArithmeticTest
 */

public class IntBoundaryArithmeticTest {
    static int add(int a, int b)  { return a + b; }
    static int sub(int a, int b)  { return a - b; }
    static int mul(int a, int b)  { return a * b; }
    static int div(int a, int b)  { return a / b; }
    static int rem(int a, int b)  { return a % b; }
    static int shl(int a, int b)  { return a << b; }
    static int shr(int a, int b)  { return a >> b; }
    static int ushr(int a, int b) { return a >>> b; }
    static long lshl(long a, int b) { return a << b; }
    static long lushr(long a, int b) { return a >>> b; }

    public static void main(String[] args) {
        // ---- 15.17.1 multiplicative: overflow wraps ----
        if (mul(Integer.MAX_VALUE, 2) != -2)
            throw new AssertionError("MAX_VALUE*2 wrap");
        if (mul(Integer.MIN_VALUE, -1) != Integer.MIN_VALUE)
            throw new AssertionError("MIN_VALUE*-1 wraps to itself");
        if (mul(Integer.MIN_VALUE, 1) != Integer.MIN_VALUE)
            throw new AssertionError("MIN_VALUE*1");

        // ---- 15.17.2 division / remainder ----
        if (div(7, 2) != 3 || div(7, -2) != -3 || div(-7, 2) != -3 || div(-7, -2) != 3)
            throw new AssertionError("division truncates toward zero");
        if (rem(7, 2) != 1 || rem(7, -2) != 1 || rem(-7, 2) != -1 || rem(-7, -2) != -1)
            throw new AssertionError("remainder takes dividend sign");

        expectArithmeticException("x / 0",          new Runnable() { public void run() { div(1, 0); } });
        expectArithmeticException("x % 0",          new Runnable() { public void run() { rem(1, 0); } });
        // MIN_VALUE / -1 and MIN_VALUE % -1 overflow but do not throw
        // (JLS 15.17.2/15.17.3): the quotient wraps to the dividend, the remainder is 0.
        if (div(Integer.MIN_VALUE, -1) != Integer.MIN_VALUE)
            throw new AssertionError("MIN_VALUE/-1 wraps to MIN_VALUE");
        if (rem(Integer.MIN_VALUE, -1) != 0)
            throw new AssertionError("MIN_VALUE%-1 == 0");
        if (div(Integer.MIN_VALUE, 1) != Integer.MIN_VALUE)
            throw new AssertionError("MIN_VALUE/1");

        // ---- 15.17.3 additive: overflow wraps ----
        if (add(Integer.MAX_VALUE, 1) != Integer.MIN_VALUE)
            throw new AssertionError("MAX_VALUE+1 wrap");
        if (sub(Integer.MIN_VALUE, 1) != Integer.MAX_VALUE)
            throw new AssertionError("MIN_VALUE-1 wrap");

        // ---- 15.18/15.19 shifts: distance masked ----
        if (shl(1, 31) != Integer.MIN_VALUE) throw new AssertionError("1<<31");
        if (shl(1, 32) != 1)                 throw new AssertionError("1<<32 masks to 0");
        if (shl(1, 33) != 2)                 throw new AssertionError("1<<33 masks to 1");
        if (shl(1, -1) != Integer.MIN_VALUE) throw new AssertionError("1<<-1 masks to 31");
        if (shr(-1, 1) != -1)                throw new AssertionError(">> sign-extends");
        if (ushr(-1, 1) != 0x7FFFFFFF)       throw new AssertionError(">>> zero-fills");

        if (lshl(1L, 63) != Long.MIN_VALUE) throw new AssertionError("1L<<63");
        if (lshl(1L, 64) != 1L)             throw new AssertionError("1L<<64 masks to 0");
        if (lushr(-1L, 1) != Long.MAX_VALUE) throw new AssertionError("-1L>>>1");

        expectArithmeticException("MIN_VALUE(64) / 0", new Runnable() { public void run() { long z = Long.MIN_VALUE / 0L; } });
        if (Long.MIN_VALUE / -1L != Long.MIN_VALUE)
            throw new AssertionError("Long.MIN_VALUE/-1 wraps to MIN_VALUE");

        System.out.println("PASS");
    }

    static void expectArithmeticException(String what, Runnable r) {
        try {
            r.run();
            throw new AssertionError(what + ": ArithmeticException expected");
        } catch (ArithmeticException expected) {
            // expected
        }
    }
}
