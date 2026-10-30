/*
 * @test
 * @summary JLS 15.7.1 (Example 15.7.1-2): for a compound assignment the value of
 *          the left-hand operand is fetched and saved before the right-hand
 *          operand is evaluated.
 * @run main CompoundAssignmentLeftOperand
 */

public class CompoundAssignmentLeftOperand {
    public static void main(String[] args) {
        int a = 9;
        a += (a = 3);          // 9 + 3 == 12
        if (a != 12) throw new AssertionError("compound: a == " + a);

        int b = 9;
        b = b + (b = 3);       // 9 + 3 == 12 (simple assignment, same order)
        if (b != 12) throw new AssertionError("simple: b == " + b);

        System.out.println("PASS");
    }
}
