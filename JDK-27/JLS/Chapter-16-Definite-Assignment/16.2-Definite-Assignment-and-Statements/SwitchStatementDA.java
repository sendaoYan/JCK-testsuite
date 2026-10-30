/*
 * @test
 * @summary JLS 16.2.9: a variable assigned in every arm of a switch statement
 *          (covering all paths via default) is definitely assigned after.
 * @run main SwitchStatementDA
 */

public class SwitchStatementDA {
    public static void main(String[] args) {
        // statement-group form with a default that covers every path
        int x;
        int k = 1;
        switch (k) {
            case 0: x = 100; break;
            default: x = 200; break;
        }
        if (x != 200) throw new AssertionError("switch group x == " + x);

        // switch-rule form with a default that covers every path
        int y;
        int m = 2;
        switch (m) {
            case 1 -> y = 10;
            case 2 -> y = 20;
            default -> y = 30;
        }
        if (y != 20) throw new AssertionError("switch rule y == " + y);

        System.out.println("PASS");
    }
}
