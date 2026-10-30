/*
 * @test
 * @summary JLS 3.10.2 (Java SE 8): a non-zero floating-point literal that rounds
 *          to an IEEE 754 infinity is too large (compile-time error); a non-zero
 *          literal that rounds to zero is too small (compile-time error).
 * @compile/fail FloatingPointLiteralErrors.java
 */

public class FloatingPointLiteralErrors {
    float tooLargeFloat = 3.5e38f;       // > Float.MAX_VALUE -> infinity
    double tooLargeDouble = 1.8e308;     // > Double.MAX_VALUE -> infinity
    float tooSmallFloat = 1e-46f;        // < Float.MIN_VALUE -> zero
    double tooSmallDouble = 1e-325;      // < Double.MIN_VALUE -> zero
    float hexTooLarge = 0x1.0p128f;      // > Float.MAX_VALUE -> infinity
    double hexTooLarge2 = 0x1.0p1024;    // > Double.MAX_VALUE -> infinity
}
