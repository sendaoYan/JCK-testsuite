/*
 * @test
 * @summary JLS 15.7.1 (Example 15.7.1-1): the left-hand operand of a binary
 *          operator is fully evaluated before the right-hand operand.
 * @run main LeftHandOperandFirst
 */

public class LeftHandOperandFirst {
    public static void main(String[] args) {
        int i = 2;
        int j = (i = 3) * i;   // left operand (i=3) evaluated before right operand i
        if (j != 9) throw new AssertionError("expected 9, got " + j);
        System.out.println("PASS");
    }
}
