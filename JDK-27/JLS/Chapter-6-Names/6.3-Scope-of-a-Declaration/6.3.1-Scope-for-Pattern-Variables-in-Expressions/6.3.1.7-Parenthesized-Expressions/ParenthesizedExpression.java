/*
 * @test
 * @summary JLS 6.3.1.7: (a) introduces a pattern variable when true/false iff a
 *          does, so parentheses do not change pattern variable scope.
 * @run main ParenthesizedExpression
 */

public class ParenthesizedExpression {
    public static void main(String[] args) {
        Object o = "paren";
        if ((o instanceof String s) && s.length() == 5) {
            if (!s.equals("paren")) throw new AssertionError("s in scope");
        } else {
            throw new AssertionError("expected paren match");
        }

        System.out.println("PASS");
    }
}
