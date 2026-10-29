/*
 * @test
 * @summary JLS 3.10.2: floating-point literals (decimal and hexadecimal);
 *          F/f suffix gives type float, otherwise double (D/d optional); the
 *          shortest and hexadecimal literals for the extreme float/double
 *          values have exactly the specified values.
 * @run main FloatingPointLiteralValues
 */

public class FloatingPointLiteralValues {
    public static void main(String[] args) {
        // Type: f/F suffix -> float, otherwise double (d/D optional).
        if (((Object) 1e1f).getClass() != Float.class) {
            throw new AssertionError("1e1f must be of type float");
        }
        if (((Object) 1e1).getClass() != Double.class) {
            throw new AssertionError("1e1 must be of type double");
        }
        if (((Object) 1e1d).getClass() != Double.class) {
            throw new AssertionError("1e1d must be of type double");
        }
        if (((Object) 3.14f).getClass() != Float.class) {
            throw new AssertionError("3.14f must be of type float");
        }

        // Decimal float extremes (JLS 3.10.2).
        if (3.4028235e38f != Float.MAX_VALUE) {
            throw new AssertionError("3.4028235e38f != Float.MAX_VALUE");
        }
        if (1.4e-45f != Float.MIN_VALUE) {
            throw new AssertionError("1.4e-45f != Float.MIN_VALUE");
        }

        // Decimal double extremes.
        if (1.7976931348623157e308 != Double.MAX_VALUE) {
            throw new AssertionError("1.7976931348623157e308 != Double.MAX_VALUE");
        }
        if (4.9e-324 != Double.MIN_VALUE) {
            throw new AssertionError("4.9e-324 != Double.MIN_VALUE");
        }

        // Hexadecimal float extremes.
        if (0x1.fffffeP+127f != Float.MAX_VALUE) {
            throw new AssertionError("0x1.fffffeP+127f != Float.MAX_VALUE");
        }
        if (0x0.000002P-126f != Float.MIN_VALUE) {
            throw new AssertionError("0x0.000002P-126f != Float.MIN_VALUE");
        }
        if (0x1.0P-149f != Float.MIN_VALUE) {
            throw new AssertionError("0x1.0P-149f != Float.MIN_VALUE");
        }

        // Hexadecimal double extremes.
        if (0x1.f_ffff_ffff_ffffP+1023 != Double.MAX_VALUE) {
            throw new AssertionError("0x1.f_ffff_ffff_ffffP+1023 != Double.MAX_VALUE");
        }
        if (0x0.0_0000_0000_0001P-1022 != Double.MIN_VALUE) {
            throw new AssertionError("0x0.0_0000_0000_0001P-1022 != Double.MIN_VALUE");
        }
        if (0x1.0P-1074 != Double.MIN_VALUE) {
            throw new AssertionError("0x1.0P-1074 != Double.MIN_VALUE");
        }

        // Ordinary literals.
        if (1e1f != 10.0f) throw new AssertionError("1e1f");
        if (2.f != 2.0f) throw new AssertionError("2.f");
        if (.3f != 0.3f) throw new AssertionError(".3f");
        if (6.022137e+23f != 6.022137e23f) throw new AssertionError("6.022137e+23f");
        if (2. != 2.0) throw new AssertionError("2.");
        if (1e-9d != 1e-9) throw new AssertionError("1e-9d");

        System.out.println("PASS");
    }
}
