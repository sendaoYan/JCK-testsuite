/*
 * @test
 * @summary JLS 16.1.4 (Java SE 8): the logical complement operator ! always
 *          evaluates its operand, so a variable assigned inside it is definitely
 *          assigned after the expression.
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
        boolean b = !((x = 1) == 1);
        check(!b, "! of true is false");
        check(x == 1, "x assigned inside operand of !");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
