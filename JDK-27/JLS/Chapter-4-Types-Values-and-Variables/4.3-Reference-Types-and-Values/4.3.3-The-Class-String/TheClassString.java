/*
 * @test
 * @summary JLS 4.3.3: instances of String represent sequences of Unicode code
 *          points; a String object has a constant (unchanging) value. String
 *          literals and text blocks are references to instances of String.
 * @run main TheClassString
 */

public class TheClassString {
    public static void main(String[] args) {
        // String literals are references to instances of class String.
        String s = "hello";
        if (!(s instanceof String)) throw new AssertionError("literal must be a String");

        // A text block is also a reference to a String instance.
        String block = """
                       hello""";
        if (!(block instanceof String)) throw new AssertionError("text block must be a String");
        if (!block.equals("hello")) throw new AssertionError("text block value");

        // Strings are sequences of Unicode code points.
        String cp = "你好";   // "你好"
        if (cp.codePointCount(0, cp.length()) != 2) {
            throw new AssertionError("two code points");
        }

        // A String's value is constant: concatenation produces a new String.
        String s2 = s;
        if (s2 != s) throw new AssertionError("same literal must be the same reference");
        String s3 = s + "!";
        if (s3.equals(s)) throw new AssertionError("concatenation must produce a new value");
        if (s3 == s) throw new AssertionError("concatenation must produce a new object");

        System.out.println("PASS");
    }
}
