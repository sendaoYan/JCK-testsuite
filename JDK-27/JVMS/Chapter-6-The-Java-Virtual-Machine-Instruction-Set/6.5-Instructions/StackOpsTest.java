/*
 * @test
 * @summary JVMS 6.5: operand stack manipulation instructions dup, swap, pop, and
 *          pop2 reorder or discard values on the operand stack.
 * @compile StackOps.jasm
 * @run main StackOpsTest
 */

public class StackOpsTest {
    public static void main(String[] args) {
        if (StackOps.dupTest() != 10) throw new AssertionError("dup");
        if (StackOps.swapTest(5, 8) != 3) throw new AssertionError("swap: 8 - 5 == 3");
        if (StackOps.popTest() != 1) throw new AssertionError("pop");
        if (StackOps.pop2Test() != 1) throw new AssertionError("pop2");

        System.out.println("PASS");
    }
}
