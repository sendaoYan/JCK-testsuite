/*
 * @test
 * @summary JLS 3.10.4: a character literal is a single character or escape
 *          sequence in single quotes, of type char, representing one UTF-16
 *          code unit.
 * @run main CharacterLiterals
 */

public class CharacterLiterals {
    public static void main(String[] args) {
        check('a', (char) 97, "'a'");
        check('%', (char) 37, "'%'");
        check('\t', (char) 9, "'\\t'");
        check('\\', (char) 92, "'\\\\'");
        check('\'', (char) 39, "'\\''");
        check('"', (char) 34, "'\"'");
        check('Ω', (char) 0x03A9, "'\\u03a9'");
        check('￿', (char) 0xFFFF, "'\\uFFFF'");
        check('\177', (char) 0x7F, "'\\177' (octal escape)");
        check('™', (char) 0x2122, "'™'");

        // A character literal is always of type char.
        if (((Object) 'x').getClass() != Character.class) {
            throw new AssertionError("a character literal must be of type char");
        }

        System.out.println("PASS");
    }

    static void check(char actual, char expected, String what) {
        if (actual != expected) {
            throw new AssertionError(what + ": expected " + (int) expected + ", got " + (int) actual);
        }
    }
}
