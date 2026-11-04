/*
 * @test
 * @summary JLS 6.2 (Java SE 27): "_" is a keyword (since Java SE 9), not an
 *          identifier, so it cannot be used as the name of a field (or method,
 *          type, or parameter).
 * @compile/fail UnderscoreIdentifier.java
 */

class UnderscoreIdentifier {
    int _ = 1;   // error: underscore not allowed here (_ is a keyword)
}
