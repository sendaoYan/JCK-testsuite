/*
 * @test
 * @summary JVMS 6.5: athrow throws the exception object on top of the operand
 *          stack.
 * @compile ExceptionOps.jasm
 * @run main ExceptionOpsTest
 */

public class ExceptionOpsTest {
    public static void main(String[] args) {
        try {
            ExceptionOps.throwNPE();
            throw new AssertionError("athrow NPE not thrown");
        } catch (NullPointerException e) { }

        try {
            ExceptionOps.throwMessage("boom");
            throw new AssertionError("athrow IAE not thrown");
        } catch (IllegalArgumentException e) {
            if (!"boom".equals(e.getMessage())) throw new AssertionError("message lost");
        }

        System.out.println("PASS");
    }
}
