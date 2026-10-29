/*
 * @test
 * @summary JLS 3.8: identifiers are unlimited-length sequences of Java letters
 *          and digits; letters/digits are defined by
 *          Character.isJavaIdentifierStart/Part; Unicode letters from any
 *          script are allowed; look-alike letters are distinct identifiers.
 * @run main IdentifierUnicode
 */

public class IdentifierUnicode {
    // Greek, CJK, dollar and underscore are all valid Java letters.
    static int αρετη = 1;
    static int 变量 = 2;
    static int $dollar = 3;
    static int under_score = 4;

    // Look-alike letters are distinct identifiers: Latin A, Greek capital
    // alpha, and Cyrillic capital A (JLS 3.8 example).
    static int A = 10;
    static int Α = 11;   // GREEK CAPITAL LETTER ALPHA U+0391
    static int А = 12;   // CYRILLIC CAPITAL LETTER A U+0410

    public static void main(String[] args) {
        if (αρετη != 1 || 变量 != 2 || $dollar != 3 || under_score != 4) {
            throw new AssertionError("unicode/dollar/underscore identifiers failed");
        }
        if (A != 10 || Α != 11 || А != 12) {
            throw new AssertionError("look-alike letters were treated as the same identifier");
        }

        // Java letters / digits are defined by the Character methods (JLS 3.8).
        if (!Character.isJavaIdentifierStart('A')) {
            throw new AssertionError("'A' must be a Java identifier start");
        }
        if (Character.isJavaIdentifierStart('3')) {
            throw new AssertionError("a digit must not be a Java identifier start");
        }
        if (!Character.isJavaIdentifierPart('3')) {
            throw new AssertionError("'3' must be a Java identifier part");
        }

        System.out.println("PASS");
    }
}
