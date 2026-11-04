/*
 * @test
 * @summary JLS 3.8: an identifier must begin with a Java letter; a digit is a
 *          Java letter-or-digit but not a Java letter, so 3abc is not a valid
 *          identifier.
 * @compile/fail DigitStartInvalid.java
 */
public class DigitStartInvalid {
    int 3abc = 1;
}
