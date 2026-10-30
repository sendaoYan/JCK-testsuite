/*
 * @test
 * @summary JLS 3.10.2 (Java SE 8): floating-point literals may be decimal or
 *          hexadecimal (0x...p...), may contain underscores, and take an
 *          optional float (f/F) or double (d/D) suffix.
 * @run main FloatingPointLiterals
 */

public class FloatingPointLiterals {
    public static void main(String[] args) {
        if (1.5 != 1.5) throw new AssertionError("decimal");
        if (1e3 != 1000.0) throw new AssertionError("exponent");
        if (1.5e-3 != 0.0015) throw new AssertionError("negative exponent");

        if (0x1.8p1 != 3.0) throw new AssertionError("hex float: 1.5 * 2^1 == 3.0");
        if (0x1p0 != 1.0) throw new AssertionError("hex float");

        if (1_000.5 != 1000.5) throw new AssertionError("underscore");

        if (1.0f != 1.0f) throw new AssertionError("float suffix");
        if (1.0d != 1.0d) throw new AssertionError("double suffix");
        if (1.0F != 1.0F || 1.0D != 1.0D) throw new AssertionError("capital suffix");

        // A float literal's value is the float nearest to the decimal value.
        if (0.1f == 0.1) throw new AssertionError("0.1f should not equal double 0.1");

        System.out.println("PASS");
    }
}
