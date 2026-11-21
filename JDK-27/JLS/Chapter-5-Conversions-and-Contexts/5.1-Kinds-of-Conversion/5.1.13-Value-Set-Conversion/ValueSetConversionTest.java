/*
 * @test
 * @summary JLS 5.1.13: the float value set is a subset of the double value set;
 *          float -> double widening is exact, while double -> float rounds to
 *          the nearest member of the float value set.
 * @run main ValueSetConversionTest
 */

public class ValueSetConversionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // float -> double is exact: every float value set member is also a
        // double value set member.
        float f = 0.1f;
        double d = f;
        check(d == (double) f, "float -> double widening is exact");
        check(d != 0.1d, "float 0.1 and double 0.1 are distinct real values");

        // double -> float rounds to the nearest float value set member.
        float rounded = (float) 0.1d;
        check(Float.floatToIntBits(rounded) == Float.floatToIntBits(0.1f),
              "double -> float rounds to the float value set");

        // The float value set has strictly less precision than the double set:
        // two adjacent double values collapse to the same float value.
        double d1 = 1.0;
        double d2 = Math.nextUp(1.0); // the next double above 1.0
        check(d1 != d2, "nextUp(1.0) is a distinct double");
        check((float) d1 == (float) d2, "adjacent doubles collapse to the same float value set member");

        // Round-trip: double -> float -> double recovers the float member, not
        // the original double.
        check((double) (float) d2 == (float) d2, "double -> float -> double is the float value");
        check((double) (float) d2 != d2, "double -> float -> double is not the original double");

        // Float special values map to their double counterparts exactly.
        check((double) Float.POSITIVE_INFINITY == Double.POSITIVE_INFINITY, "+Inf");
        check((double) Float.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY, "-Inf");
        check(Double.isNaN((double) Float.NaN), "NaN");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
