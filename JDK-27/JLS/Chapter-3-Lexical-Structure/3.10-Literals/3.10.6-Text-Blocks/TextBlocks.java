/*
 * @test
 * @summary JLS 3.10.6: a text block is of type String; its value is derived by
 *          normalizing line terminators, removing incidental white space
 *          (stripIndent), and interpreting escape sequences; identical text
 *          blocks refer to the same interned instance.
 * @run main TextBlocks
 */

public class TextBlocks {
    public static void main(String[] args) {
        // (1) No incidental indentation (content and closing delimiter at
        // column 0): the value is exactly the literal content.
        String a = """
abc
def
""";
        check(a, "abc\ndef\n", "no incidental indentation");

        // (2) Incidental white space is removed as by String.stripIndent.
        String b = """
            x
            y
            """;
        check(b, "x\ny\n", "incidental whitespace removal");

        // (3) Escape sequences are interpreted (JLS 3.10.6 step 3).
        String c = """
a\nb""";
        check(c, "a\nb", "\\n escape");

        String d = """
a\"b""";
        check(d, "a\"b", "\\\" escape");

        // (4) The closing delimiter may follow the last content character.
        String e = """
winter""";
        check(e, "winter", "closing delimiter on last line");

        // (5) An empty text block has zero length.
        String empty = """
""";
        check(empty, "", "empty text block");

        // (6) Identical text blocks refer to the same interned String instance.
        String x = """
same
""";
        String y = """
same
""";
        if (x != y) {
            throw new AssertionError("identical text blocks must be the same instance");
        }

        // (7) A text block is of type String and may be used in concatenation.
        String concat = "ab" + """
cde
""";
        check(concat, "abcde\n", "text block in concatenation");

        System.out.println("PASS");
    }

    static void check(String actual, String expected, String what) {
        if (!expected.equals(actual)) {
            throw new AssertionError(what + ": expected " + quote(expected)
                    + ", got " + quote(actual));
        }
    }

    static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\"", "\\\"") + "\"";
    }
}
