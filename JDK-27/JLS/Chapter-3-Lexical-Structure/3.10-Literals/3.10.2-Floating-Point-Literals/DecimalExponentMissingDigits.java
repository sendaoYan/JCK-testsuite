/*
 * @test
 * @summary JLS 3.10.2: the exponent part of a decimal floating-point literal
 *          requires at least one digit after e/E and its optional sign; 1e and
 *          1e+ are malformed.
 * @compile/fail DecimalExponentMissingDigits.java
 */
public class DecimalExponentMissingDigits {
    double a = 1e;
    double b = 1e+;
}
