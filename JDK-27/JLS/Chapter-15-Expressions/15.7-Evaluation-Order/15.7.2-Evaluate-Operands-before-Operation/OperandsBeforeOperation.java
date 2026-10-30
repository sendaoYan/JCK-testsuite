/*
 * @test
 * @summary JLS 15.7.2 (Example 15.7.2-1): both operands are fully evaluated
 *          before any part of the operation, so a divide-by-zero exception is
 *          not signaled before a right-operand invocation completes abruptly.
 * @run main OperandsBeforeOperation
 */

public class OperandsBeforeOperation {
    static int loseBig() throws Exception { throw new Exception("lose"); }

    public static void main(String[] args) throws Exception {
        int divisor = 0;
        try {
            int i = 1 / (divisor * loseBig());
            throw new AssertionError("should have thrown");
        } catch (Exception e) {
            if (!"lose".equals(e.getMessage()))
                throw new AssertionError("expected lose, got " + e.getMessage());
        }
        System.out.println("PASS");
    }
}
