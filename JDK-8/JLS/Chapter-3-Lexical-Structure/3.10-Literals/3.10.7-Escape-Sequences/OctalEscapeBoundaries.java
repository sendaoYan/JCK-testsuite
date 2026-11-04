/*
 * @test
 * @summary JLS 3.10.7 (Java SE 8): an octal escape is a backslash followed by
 *          one, two, or three octal digits, where a three-digit octal escape is
 *          at most \377; the input \400 is parsed (longest match) as the
 *          two-digit escape \40 followed by the character 0.
 * @run main OctalEscapeBoundaries
 */

public class OctalEscapeBoundaries {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        check("\0".charAt(0) == 0, "\\0 == NUL");
        check("\7".charAt(0) == 7, "\\7");
        check("\07".charAt(0) == 7, "\\07");
        check("\077".charAt(0) == 63, "\\077 == 63");
        check("\377".charAt(0) == (char) 0xFF, "\\377 == 0xFF (max three-digit octal)");
        check('\377' == (char) 0xFF, "char '\\377' == 0xFF");
        check("\400".equals(" " + "0"), "\\400 is \\40 followed by '0'");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
