/*
 * @test
 * @summary JLS 4.2.3: float/double use IEEE 754 binary32/binary64, including
 *          positive/negative zero, positive/negative infinity, and NaN;
 *          positive and negative zero compare equal; 1.0/0.0 is +infinity and
 *          1.0/-0.0 is -infinity.
 * @run main FloatingPointTypesAndValues
 */

public class FloatingPointTypesAndValues {
    public static void main(String[] args) {
        // Positive zero and negative zero compare equal (JLS 4.2.3).
        if (!(0.0 == -0.0)) throw new AssertionError("0.0 == -0.0 must be true");
        if (0.0 > -0.0) throw new AssertionError("0.0 > -0.0 must be false");
        if (!(0.0f == -0.0f)) throw new AssertionError("0.0f == -0.0f must be true");

        // Division distinguishes the zeros: sign of infinity.
        if (1.0 / 0.0 != Double.POSITIVE_INFINITY) {
            throw new AssertionError("1.0/0.0 must be +infinity");
        }
        if (1.0 / -0.0 != Double.NEGATIVE_INFINITY) {
            throw new AssertionError("1.0/-0.0 must be -infinity");
        }
        if (-1.0 / 0.0 != Double.NEGATIVE_INFINITY) {
            throw new AssertionError("-1.0/0.0 must be -infinity");
        }

        // NaN constants are predefined.
        if (Float.isNaN(Float.NaN) == false) throw new AssertionError("Float.NaN");
        if (Double.isNaN(Double.NaN) == false) throw new AssertionError("Double.NaN");

        // 0.0/0.0 is NaN.
        double nan = 0.0 / 0.0;
        if (!Double.isNaN(nan)) throw new AssertionError("0.0/0.0 must be NaN");

        // NaN is unordered: comparison operators return false if either operand is NaN.
        if (nan < 0.0) throw new AssertionError("NaN < 0.0 must be false");
        if (nan <= 0.0) throw new AssertionError("NaN <= 0.0 must be false");
        if (nan > 0.0) throw new AssertionError("NaN > 0.0 must be false");
        if (nan >= 0.0) throw new AssertionError("NaN >= 0.0 must be false");
        // (x<y) == !(x>=y) is false if x or y is NaN.
        if ((nan < 1.0) == !(nan >= 1.0)) {
            throw new AssertionError("(NaN<1.0)==!(NaN>=1.0) must be false");
        }

        // == returns false if either operand is NaN; != returns true.
        if (nan == nan) throw new AssertionError("NaN == NaN must be false");
        if (!(nan != nan)) throw new AssertionError("NaN != NaN must be true");
        // x!=x is true iff x is NaN.
        if (!(nan != nan)) throw new AssertionError("x!=x must be true for NaN");

        // float is binary32 (24-bit significand), double is binary64 (53-bit).
        if (Float.MIN_EXPONENT != -126 || Float.MAX_EXPONENT != 127) {
            throw new AssertionError("float exponent range");
        }
        if (Double.MIN_EXPONENT != -1022 || Double.MAX_EXPONENT != 1023) {
            throw new AssertionError("double exponent range");
        }

        System.out.println("PASS");
    }
}
