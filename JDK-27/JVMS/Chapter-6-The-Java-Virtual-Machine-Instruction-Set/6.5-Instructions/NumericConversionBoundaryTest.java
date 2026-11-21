/*
 * @test
 * @summary JVMS 6.5 per-opcode numeric conversion boundary matrix: i2l/i2f/i2d,
 *          l2i/l2f/l2d, f2i/f2l/f2d, d2i/d2l/d2f. Narrowing truncates toward
 *          zero, saturates out-of-range and infinite inputs to the target's
 *          extreme value, and maps NaN to zero; widening rounds to nearest.
 *          Operands are routed through helper parameters so javac emits the
 *          conversion opcodes instead of constant-folding the casts.
 * @run main NumericConversionBoundaryTest
 */

public class NumericConversionBoundaryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Each helper is a runtime cast so the corresponding conversion opcode runs.
    static long i2l(int v) { return (long) v; }
    static float i2f(int v) { return (float) v; }
    static double i2d(int v) { return (double) v; }
    static int l2i(long v) { return (int) v; }
    static float l2f(long v) { return (float) v; }
    static double l2d(long v) { return (double) v; }
    static int f2i(float v) { return (int) v; }
    static long f2l(float v) { return (long) v; }
    static double f2d(float v) { return (double) v; }
    static int d2i(double v) { return (int) v; }
    static long d2l(double v) { return (long) v; }
    static float d2f(double v) { return (float) v; }

    public static void main(String[] args) {
        // i2l: sign-extends, exact for every int
        check(i2l(0) == 0L, "i2l 0");
        check(i2l(Integer.MAX_VALUE) == 2147483647L, "i2l MAX exact");
        check(i2l(Integer.MIN_VALUE) == -2147483648L, "i2l MIN exact");

        // i2f: rounds to nearest float
        check(i2f(16777216) == 16777216.0f, "i2f 2^24 exact");
        check(i2f(16777217) == 16777216.0f, "i2f 2^24+1 rounds to even");
        check(i2f(-16777216) == -16777216.0f, "i2f -2^24 exact");

        // i2d: exact for every int
        check(i2d(Integer.MAX_VALUE) == 2147483647.0, "i2d MAX exact");

        // l2i: keeps the low 32 bits
        check(l2i(0x1_0000_0001L) == 1, "l2i low 32 bits");
        check(l2i(-1L) == -1, "l2i -1");

        // l2f: rounds to nearest float
        check(l2f(16777216L) == 16777216.0f, "l2f 2^24 exact");
        check(l2f(16777217L) == 16777216.0f, "l2f 2^24+1 rounds to even");
        check(l2f(Long.MAX_VALUE) == 9223372036854775808.0f, "l2f MAX rounds up to 2^63");

        // l2d: rounds to nearest double
        check(l2d(1L << 53) == 9007199254740992.0, "l2d 2^53 exact");
        check(l2d(Long.MAX_VALUE) == 9223372036854775808.0, "l2d MAX rounds up to 2^63");

        // f2i: truncates toward zero, saturates, NaN -> 0
        check(f2i(3.99f) == 3, "f2i truncate positive");
        check(f2i(-3.99f) == -3, "f2i truncate negative");
        check(f2i(Float.NaN) == 0, "f2i NaN -> 0");
        check(f2i(Float.POSITIVE_INFINITY) == Integer.MAX_VALUE, "f2i +inf -> MAX");
        check(f2i(Float.NEGATIVE_INFINITY) == Integer.MIN_VALUE, "f2i -inf -> MIN");
        check(f2i(2147483648.0f) == Integer.MAX_VALUE, "f2i 2^31 saturates to MAX");

        // f2l: truncates toward zero, saturates, NaN -> 0
        check(f2l(3.99f) == 3L, "f2l truncate positive");
        check(f2l(-3.99f) == -3L, "f2l truncate negative");
        check(f2l(Float.NaN) == 0L, "f2l NaN -> 0");
        check(f2l(Float.POSITIVE_INFINITY) == Long.MAX_VALUE, "f2l +inf -> MAX");
        check(f2l(Float.NEGATIVE_INFINITY) == Long.MIN_VALUE, "f2l -inf -> MIN");
        check(f2l(9223372036854775808.0f) == Long.MAX_VALUE, "f2l 2^63 saturates to MAX");

        // f2d: exact widening
        check(f2d(1.5f) == 1.5, "f2d 1.5 exact");

        // d2i: truncates toward zero, saturates, NaN -> 0
        check(d2i(3.99) == 3, "d2i truncate positive");
        check(d2i(-3.99) == -3, "d2i truncate negative");
        check(d2i(Double.NaN) == 0, "d2i NaN -> 0");
        check(d2i(Double.POSITIVE_INFINITY) == Integer.MAX_VALUE, "d2i +inf -> MAX");
        check(d2i(Double.NEGATIVE_INFINITY) == Integer.MIN_VALUE, "d2i -inf -> MIN");
        check(d2i(2147483648.0) == Integer.MAX_VALUE, "d2i 2^31 saturates to MAX");
        check(d2i(-2147483649.0) == Integer.MIN_VALUE, "d2i below MIN saturates to MIN");

        // d2l: truncates toward zero, saturates, NaN -> 0
        check(d2l(3.99) == 3L, "d2l truncate positive");
        check(d2l(-3.99) == -3L, "d2l truncate negative");
        check(d2l(Double.NaN) == 0L, "d2l NaN -> 0");
        check(d2l(Double.POSITIVE_INFINITY) == Long.MAX_VALUE, "d2l +inf -> MAX");
        check(d2l(Double.NEGATIVE_INFINITY) == Long.MIN_VALUE, "d2l -inf -> MIN");
        check(d2l(9223372036854775808.0) == Long.MAX_VALUE, "d2l 2^63 saturates to MAX");
        check(d2l(9223372036854774784.0) == 9223372036854774784L,
              "d2l largest double below 2^63 exact");

        // d2f: rounds to nearest float; overflow -> infinity
        check(d2f(1.5) == 1.5f, "d2f 1.5 exact");
        check(d2f(Double.MAX_VALUE) == Float.POSITIVE_INFINITY, "d2f overflow -> +inf");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
