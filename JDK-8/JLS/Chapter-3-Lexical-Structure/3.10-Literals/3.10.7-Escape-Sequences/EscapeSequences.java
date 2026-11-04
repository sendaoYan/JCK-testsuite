/*
 * @test
 * @summary JLS 3.10.7 (Java SE 8): escape sequences \b \t \n \f \r \" \' \\ and
 *          octal escapes denote the specified characters.  (Java SE 8 has no
 *          \s escape and no text blocks.)
 * @run main EscapeSequences
 */

public class EscapeSequences {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        check("\b".charAt(0) == '\b', "\\b backspace");
        check("\t".charAt(0) == '\t', "\\t horizontal tab");
        check("\n".charAt(0) == '\n', "\\n linefeed");
        check("\f".charAt(0) == '\f', "\\f form feed");
        check("\r".charAt(0) == '\r', "\\r carriage return");
        check("\"".charAt(0) == '"', "\\\" double quote");
        check("\'".charAt(0) == '\'', "\\' single quote");
        check("\\".charAt(0) == '\\', "\\\\ backslash");

        check("\0".charAt(0) == 0, "\\0");
        check("\101".charAt(0) == 'A', "\\101 == 'A'");
        check("\177".charAt(0) == 0x7F, "\\177 == 0x7F");
        check("\377".charAt(0) == 0xFF, "\\377 == 0xFF");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
