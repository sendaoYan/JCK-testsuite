/*
 * @test
 * @summary JLS 16.1.4: the logical complement operator ! always evaluates its
 *          operand, so a variable assigned inside the operand is definitely
 *          assigned after the expression; the true/false cases are swapped.
 * @run main LogicalComplementDA
 */

public class LogicalComplementDA {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int x;
        boolean b = !((x = 1) == 1);   // operand always evaluated; x assigned; b == false
        check(!b, "! of true is false");
        check(x == 1, "x assigned inside operand of !");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
