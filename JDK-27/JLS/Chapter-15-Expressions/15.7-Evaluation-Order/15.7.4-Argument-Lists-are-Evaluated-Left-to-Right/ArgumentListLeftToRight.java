/*
 * @test
 * @summary JLS 15.7.4 (Example 15.7.4-1): argument expressions are evaluated
 *          left to right, each fully evaluated before any to its right.
 * @run main ArgumentListLeftToRight
 */

public class ArgumentListLeftToRight {
    static String concat(String a, String b, String c) { return a + b + c; }

    public static void main(String[] args) {
        String s = "going, ";
        String r = concat(s, s, s = "gone");
        if (!"going, going, gone".equals(r))
            throw new AssertionError("argument order: " + r);
        System.out.println("PASS");
    }
}
