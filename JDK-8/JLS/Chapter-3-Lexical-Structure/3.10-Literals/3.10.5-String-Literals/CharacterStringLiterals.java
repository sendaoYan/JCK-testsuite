/*
 * @test
 * @summary JLS 3.10.4 / 3.10.5 (Java SE 8): character and string literals use
 *          escape sequences for special characters and octal/Unicode escapes.
 *          Java 8 has no text blocks.
 * @run main CharacterStringLiterals
 */

public class CharacterStringLiterals {
    public static void main(String[] args) {
        if ('a' != 97) throw new AssertionError("char literal");
        if ('\n' != 10 || '\t' != 9) throw new AssertionError("char escape");
        if ('\u0041' != 'A') throw new AssertionError("char unicode escape");
        if ('\101' != 'A') throw new AssertionError("char octal escape");
        if ('\\' != '\\' || '\'' != '\'' || '\"' != '"') throw new AssertionError("char escaped quote");

        if (!"hello".equals("hello")) throw new AssertionError("string literal");
        if (!"a\nb".equals("a" + '\n' + "b")) throw new AssertionError("string escape");
        if (!"\u0041".equals("A")) throw new AssertionError("string unicode escape");
        if ("a\nb".length() != 3) throw new AssertionError("string escape length");

        if (!("a" + "b").equals("ab")) throw new AssertionError("constant concat");

        System.out.println("PASS");
    }
}
