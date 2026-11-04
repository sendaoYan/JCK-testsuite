/*
 * @test
 * @summary JLS 15.7.3: evaluation respects parentheses and operator precedence.
 *          Multiplicative operators bind tighter than additive, both are
 *          left-associative, shift binds looser than additive but tighter than
 *          relational, and parentheses override the default precedence.
 * @run main PrecedenceAndAssociativity
 */

public class PrecedenceAndAssociativity {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // multiplicative binds tighter than additive
        check(2 + 3 * 4 == 14, "2 + 3*4 == 14, not 20");
        check(2 * 3 + 4 == 10, "2*3 + 4 == 10");
        check(10 - 3 * 2 == 4, "10 - 3*2 == 4");
        // additive left-associative
        check(10 - 3 - 2 == 5, "10-3-2 == 5, not 9");
        check(10 + 3 - 2 == 11, "10+3-2 == 11");
        // multiplicative left-associative
        check(20 / 5 / 2 == 2, "20/5/2 == 2, not 8");
        check(100 / 10 * 2 == 20, "100/10*2 == 20");
        // unary minus binds tighter than multiplicative
        check(-2 * 3 == -6, "-2*3 == -6");
        check(2 * -3 == -6, "2*-3 == -6");
        // shift binds looser than additive
        check(1 + 1 << 2 == 8, "1+1<<2 == 8 (additive first)");
        check((1 + 1) << 2 == 8, "(1+1)<<2 == 8");
        // shift binds tighter than relational
        check(1 << 2 > 3, "1<<2 > 3 (shift first)");
        // parentheses override precedence
        check((2 + 3) * 4 == 20, "(2+3)*4 == 20");
        check(10 - (3 - 2) == 9, "10-(3-2) == 9");
        check(20 / (5 / 2) == 10, "20/(5/2) == 10");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
