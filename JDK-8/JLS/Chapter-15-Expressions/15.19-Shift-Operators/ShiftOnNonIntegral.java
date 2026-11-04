/*
 * @test
 * @summary JLS 15.19 (Java SE 8): each operand of a shift operator must be of an
 *          integral type after unary numeric promotion; a floating-point operand
 *          is a compile-time error.
 * @compile/fail ShiftOnNonIntegral.java
 */

class ShiftOnNonIntegral {
    int m() {
        return 1 << 1.5;   // error: operator << cannot be applied to int,double
    }
}
