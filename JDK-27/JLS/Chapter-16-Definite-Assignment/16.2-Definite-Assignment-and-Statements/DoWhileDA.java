/*
 * @test
 * @summary JLS 16.2.11: a do-while statement always executes its body at least
 *          once, so a variable assigned in the body is definitely assigned after.
 * @run main DoWhileDA
 */

public class DoWhileDA {
    public static void main(String[] args) {
        int x;
        int n = 0;
        do { x = 10; n++; } while (n < 1);
        if (x != 10 || n != 1) throw new AssertionError("do-while");

        System.out.println("PASS");
    }
}
