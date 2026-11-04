/*
 * @test
 * @summary JLS 3.10.2: a hexadecimal floating-point literal requires a binary
 *          exponent (p/P); 0x1.0 without a binary exponent is malformed.
 * @compile/fail HexFloatRequiresExponent.java
 */
public class HexFloatRequiresExponent {
    double x = 0x1.0;
}
