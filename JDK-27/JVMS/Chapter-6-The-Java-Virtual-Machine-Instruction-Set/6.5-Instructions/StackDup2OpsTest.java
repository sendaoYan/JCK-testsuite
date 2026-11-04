/*
 * @test
 * @summary JVMS 6.5: dup2/dup2_x1/dup2_x2 duplicate the top two operand-stack
 *          words (or one category-2 value) per the three documented forms.
 * @compile StackDup2Ops.jasm
 * @run main StackDup2OpsTest
 */

public class StackDup2OpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(StackDup2Ops.dup2TwoInt() == 6, "dup2 two ints");
        check(StackDup2Ops.dup2Long() == 2L, "dup2 one long");
        check(StackDup2Ops.dup2x1() == 11, "dup2_x1 form 1");
        check(StackDup2Ops.dup2x2() == 17, "dup2_x2 form 1");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
