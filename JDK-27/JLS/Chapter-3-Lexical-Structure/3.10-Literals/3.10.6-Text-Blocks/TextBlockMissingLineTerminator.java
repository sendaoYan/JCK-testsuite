/*
 * @test
 * @summary JLS 3.10.6: the opening delimiter of a text block must be followed by
 *          a line terminator; """abc""" is a compile-time error.
 * @compile/fail TextBlockMissingLineTerminator.java
 */
public class TextBlockMissingLineTerminator {
    public static void main(String[] args) {
        String s = """abc""";
    }
}
