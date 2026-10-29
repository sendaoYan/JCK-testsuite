/*
 * @test
 * @summary JLS 3.10.7: escape sequences \b \s \t \n \f \r \" \' \\ and octal
 *          escapes, plus the line-continuation escape in a text block, denote
 *          the specified characters.
 * @run main EscapeSequences
 */

public class EscapeSequences {
    public static void main(String[] args) {
        check("\b".charAt(0), '\b', "\\b backspace");
        check("\s".charAt(0), ' ', "\\s space");
        check("\t".charAt(0), '\t', "\\t horizontal tab");
        check("\n".charAt(0), '\n', "\\n linefeed");
        check("\f".charAt(0), '\f', "\\f form feed");
        check("\r".charAt(0), '\r', "\\r carriage return");
        check("\"".charAt(0), '"', "\\\" double quote");
        check("\'".charAt(0), '\'', "\\' single quote");
        check("\\".charAt(0), '\\', "\\\\ backslash");

        // Octal escapes express Unicode \u0000 through ÿ.
        check("\0".charAt(0), (char) 0, "\\0");
        check("\101".charAt(0), 'A', "\\101 == 'A'");
        check("\177".charAt(0), (char) 0x7F, "\\177 == 0x7F");
        check("\377".charAt(0), (char) 0xFF, "\\377 == 0xFF");

        // The line-continuation escape (backslash + line terminator) is
        // removed; it may appear in a text block.
        String t = """
line1\
line2""";
        check(t, "line1line2", "line continuation in text block");

        System.out.println("PASS");
    }

    static void check(char actual, char expected, String what) {
        if (actual != expected) {
            throw new AssertionError(what + ": expected " + (int) expected + ", got " + (int) actual);
        }
    }

    static void check(String actual, String expected, String what) {
        if (!expected.equals(actual)) {
            throw new AssertionError(what + ": expected '" + expected + "', got '" + actual + "'");
        }
    }
}
