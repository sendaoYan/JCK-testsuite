/*
 * @test
 * @summary JLS 15.16 (with 5.1.3): narrowing a floating-point value to an
 *          integral type rounds toward zero; NaN becomes 0, values beyond the
 *          target range clamp to the nearest extreme, and double->float overflow
 *          yields infinity.
 * @run main CastFloatDoubleToIntClamp
 */

public class CastFloatDoubleToIntClamp {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check((int) 3.99 == 3, "double->int truncates");
        check((int) -3.99 == -3, "double->int negative truncates");
        check((int) Double.NaN == 0, "NaN -> 0");
        check((int) Double.POSITIVE_INFINITY == Integer.MAX_VALUE, "+Inf -> MAX_VALUE");
        check((int) Double.NEGATIVE_INFINITY == Integer.MIN_VALUE, "-Inf -> MIN_VALUE");
        check((int) 2147483647.0 == 2147483647, "exact MAX_VALUE");
        check((int) 2147483648.0 == Integer.MAX_VALUE, "above MAX clamps to MAX_VALUE");
        check((int) -2147483649.0 == Integer.MIN_VALUE, "below MIN clamps to MIN_VALUE");

        check((long) Double.NaN == 0L, "NaN -> 0L");
        check((long) Double.POSITIVE_INFINITY == Long.MAX_VALUE, "+Inf -> Long.MAX_VALUE");
        check((long) Double.NEGATIVE_INFINITY == Long.MIN_VALUE, "-Inf -> Long.MIN_VALUE");

        check((int) 1.5f == 1, "float->int truncates");
        check((int) Float.NaN == 0, "float NaN -> 0");
        check((int) Float.POSITIVE_INFINITY == Integer.MAX_VALUE, "float +Inf -> MAX_VALUE");

        check((float) Double.MAX_VALUE == Float.POSITIVE_INFINITY, "double MAX -> float Inf");
        check((float) 1.0e40 == Float.POSITIVE_INFINITY, "double overflow -> float Inf");
        check((float) 1.0e-46 == 0.0f, "double underflow -> 0.0f");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
