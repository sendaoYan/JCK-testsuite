/*
 * @test
 * @summary JLS 3.3: The character produced by a Unicode escape does not
 *          participate in further Unicode escapes. The raw input
 *          \u005c + u005a yields a backslash followed by u005a, so inside
 *          a string literal the content has \u as an illegal escape
 *          sequence (JLS 3.10.7). Must be a compile-time error.
 * @compile/fail IllegalEscapeFromUnicode.java
 */

public class IllegalEscapeFromUnicode {
    public static void main(String[] args) {
        String s = "\u005cu005a";
    }
}
