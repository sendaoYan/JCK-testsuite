/*
 * @test
 * @summary JLS 3.10.2: underscores in a floating-point literal may appear only
 *          between digits; they are not allowed adjacent to the decimal point
 *          or to the exponent indicator (e/E).
 * @compile/fail FloatingPointBadUnderscores.java
 */
public class FloatingPointBadUnderscores {
    double a = 1_.0;
    double b = 1._0;
    double c = 1.0_;
    double d = 1e_1;
    double e = 1e1_;
}
