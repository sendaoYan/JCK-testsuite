/*
 * @test
 * @summary JLS 15.22: integer bitwise and boolean logical operators &, ^, | all
 *          evaluate both operands (no short-circuiting).
 * @run main BitwiseLogicalOperators
 */

public class BitwiseLogicalOperators {
    public static void main(String[] args) {
        if ((0b1100 & 0b1010) != 0b1000) throw new AssertionError("&");
        if ((0b1100 ^ 0b1010) != 0b0110) throw new AssertionError("^");
        if ((0b1100 | 0b1010) != 0b1110) throw new AssertionError("|");

        int side = 0;
        boolean r = false & ((side = 1) == 1);   // right operand is evaluated
        if (r || side != 1) throw new AssertionError("& evaluates both");

        boolean s = true | ((side = 2) == 2);    // right operand is evaluated
        if (!s || side != 2) throw new AssertionError("| evaluates both");

        System.out.println("PASS");
    }
}
