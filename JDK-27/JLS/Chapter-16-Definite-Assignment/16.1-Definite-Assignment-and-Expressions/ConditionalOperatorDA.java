/*
 * @test
 * @summary JLS 16.1.5: a variable assigned in both arms of a conditional
 *          expression is definitely assigned after the expression.
 * @run main ConditionalOperatorDA
 */

public class ConditionalOperatorDA {
    public static void main(String[] args) {
        int x;
        boolean c = false;
        int y = c ? (x = 1) : (x = 2);
        if (x != 2 || y != 2) throw new AssertionError("?: x,y");

        int z = x;   // x is definitely assigned after ?:
        if (z != 2) throw new AssertionError("x after ?:");

        System.out.println("PASS");
    }
}
