/*
 * @test
 * @summary JLS 4.2.4 (Java SE 8): a floating-point operation that overflows
 *          produces a signed infinity; one that underflows produces a
 *          denormalized value or a signed zero; an operation with no
 *          mathematically definite result produces NaN; all numeric operations
 *          with NaN as an operand produce NaN.
 * @run main FloatingPointOverflowUnderflow
 */

public class FloatingPointOverflowUnderflow {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // Overflow produces a signed infinity.
        double big = 1e308;
        check(big * 10 == Double.POSITIVE_INFINITY, "double overflow yields +infinity");
        check(-big * 10 == Double.NEGATIVE_INFINITY, "double overflow yields -infinity");
        check(Float.MAX_VALUE * 2.0f == Float.POSITIVE_INFINITY, "float overflow yields +infinity");

        // Underflow produces a denormalized value or a signed zero.
        double tiny = Double.MIN_NORMAL;         // smallest normalized double
        check(tiny / 2 > 0.0, "gradual underflow keeps a positive denormalized value");
        check(Double.MIN_VALUE / Double.MAX_VALUE == 0.0, "extreme underflow yields signed zero");

        // An operation with no mathematically definite result produces NaN.
        check(Double.isNaN(0.0 / 0.0), "0.0/0.0 is NaN");
        check(Double.isNaN(Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY),
              "infinity - infinity is NaN");
        check(Double.isNaN(Math.sqrt(-1.0)), "sqrt(-1) is NaN");

        // All numeric operations with NaN as an operand produce NaN.
        check(Double.isNaN(Double.NaN + 1.0), "NaN + x is NaN");
        check(Double.isNaN(1.0 * Double.NaN), "x * NaN is NaN");
        check(Double.isNaN(Double.NaN / 1.0), "NaN / x is NaN");
        check(Float.isNaN(Float.NaN - 0.0f), "float NaN - x is NaN");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
