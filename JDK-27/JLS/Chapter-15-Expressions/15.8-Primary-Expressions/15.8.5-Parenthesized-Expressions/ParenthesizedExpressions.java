/*
 * @test
 * @summary JLS 15.8.5: a parenthesized expression has the same type, value and
 *          semantics as the contained expression; parentheses merely group.
 * @run main ParenthesizedExpressions
 */

public class ParenthesizedExpressions {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check((42) == 42, "(42) == 42");
        int x = 5;
        int y = (x);
        check(y == 5, "(x) == x");
        Object o = ("abc");
        check(o instanceof String, "parenthesized String literal is a String");
        check(((1 + 2) * 3) == 9, "nested parentheses group sub-expressions");
        check((-(5)) == -5, "(-(5)) == -5");
        // a parenthesized variable is still a variable: it can be assigned
        (x) = 7;
        check(x == 7, "(x) = 7 assigns the variable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
