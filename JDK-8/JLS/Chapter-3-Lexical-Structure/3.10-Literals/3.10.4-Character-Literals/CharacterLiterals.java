/*
 * @test
 * @summary JLS 3.10.4 (Java SE 8): a character literal is a single character or
 *          escape sequence in single quotes, of type char, representing one
 *          UTF-16 code unit.
 * @run main CharacterLiterals
 */

public class CharacterLiterals {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        check('a' == 97, "'a'");
        check('%' == 37, "'%'");
        check('\t' == 9, "'\\t'");
        check('\\' == 92, "'\\\\'");
        check('\'' == 39, "'\\''");
        check('"' == 34, "'\"'");
        check('Ω' == 0x03A9, "'\\u03a9'");
        check('\177' == 0x7F, "'\\177' (octal escape)");

        check(((Object) 'x').getClass() == Character.class,
                "a character literal is of type char");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
