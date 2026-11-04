/*
 * @test
 * @summary JVMS 3.11: swap exchanges the top two operand-stack values and dup
 *          duplicates the top value; a jasm method using them computes the
 *          swapped subtraction and the doubled sum.
 * @compile OperandStackOps.jasm
 * @run main OperandStackOpsTest
 */

public class OperandStackOpsTest {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("OperandStackOps");

        check(c.getMethod("swapped", int.class, int.class).invoke(null, 10, 3).equals(-7),
                "swap makes isub compute b - a");
        check(c.getMethod("doubled", int.class).invoke(null, 5).equals(10),
                "dup makes iadd compute x + x");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
