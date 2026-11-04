/*
 * @test
 * @summary JLS 15.22 (Java SE 8): the bitwise operators &, ^ and | require both
 *          operands to be integral (or both boolean); a floating-point operand
 *          is a compile-time error.
 * @compile/fail BitwiseOnNonIntegral.java
 */

class BitwiseOnNonIntegral {
    double m(double d) {
        return d & 2.0;   // error: operator & cannot be applied to double,double
    }
}
