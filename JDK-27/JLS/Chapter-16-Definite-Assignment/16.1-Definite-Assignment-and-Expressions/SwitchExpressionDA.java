/*
 * @test
 * @summary JLS 16.1.6: a variable assigned in every arm of a switch expression is
 *          definitely assigned after the expression.
 * @run main SwitchExpressionDA
 */

public class SwitchExpressionDA {
    public static void main(String[] args) {
        int x;
        int r = switch (1) {
            case 1 -> { x = 10; yield 1; }
            default -> { x = 20; yield 2; }
        };
        // x is definitely assigned in every arm, so it may be read afterward.
        if (r != 1 || x != 10) throw new AssertionError("switch expr r,x");

        System.out.println("PASS");
    }
}
