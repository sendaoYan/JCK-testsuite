/*
 * @test
 * @summary JLS 16.1.1: a boolean constant expression true/false has a
 *          compile-time-known value, so the right operand of && / || and the
 *          selected arm of ?: are known to be evaluated or skipped; this
 *          determines whether a variable assigned there is definitely assigned.
 * @run main BooleanConstantExpressionDA
 */

public class BooleanConstantExpressionDA {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // true && (x = 1): the right operand is always evaluated, so x is assigned.
        int x;
        if (true && (x = 1) == 1) check(x == 1, "true && evaluates right operand");

        // false || (y = 2): the left operand is false, so the right operand runs.
        int y;
        if (false || (y = 2) == 2) check(y == 2, "false || evaluates right operand");

        // true ? (z = 3) : (z = 4): only the then-arm is evaluated.
        int z;
        int r = true ? (z = 3) : (z = 4);
        check(z == 3 && r == 3, "true ?: evaluates then-arm");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
