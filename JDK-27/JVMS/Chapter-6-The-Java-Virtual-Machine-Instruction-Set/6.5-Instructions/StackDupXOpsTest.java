/*
 * @test
 * @summary JVMS 6.5: dup_x1/dup_x2/dup2_x1/dup2_x2, including the forms
 *          involving a category-2 (long/double) operand, verifying the exact
 *          duplicated operand-stack ordering per form.
 * @compile StackDupXOps.jasm
 * @run main StackDupXOpsTest
 */

public class StackDupXOpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(StackDupXOps.dupX1Form1() == 17, "dup_x1 (both cat-1)");
        check(StackDupXOps.dupX2Form1() == 15, "dup_x2 form 1 (all cat-1)");
        check(StackDupXOps.dupX2Form2() == 15, "dup_x2 form 2 (long below, int top)");
        check(StackDupXOps.dup2X1Form2() == 9, "dup2_x1 form 2 (long top, int below)");
        check(StackDupXOps.dup2X2Form2() == 7, "dup2_x2 form 2 (long top, two ints below)");
        check(StackDupXOps.dup2X2Form3() == 15, "dup2_x2 form 3 (long below, two ints top)");
        check(StackDupXOps.dup2X2Form4() == 7, "dup2_x2 form 4 (both double)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
