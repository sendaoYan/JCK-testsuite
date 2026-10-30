/*
 * @test
 * @summary JLS 3.9 (Java SE 8): keywords (class) and reserved words (goto, const)
 *          cannot be used as identifiers.
 * @compile/fail KeywordAsIdentifier.java
 */

class KeywordAsIdentifier {
    int class = 1;    // error: 'class' is a keyword
    int goto_ = 2;    // ok, but:
    int goto = 3;     // error: 'goto' is reserved
    int const = 4;    // error: 'const' is reserved
}
