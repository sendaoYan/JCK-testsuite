/*
 * @test
 * @summary JLS 15.29: a constant expression is an expression of primitive or
 *          String type composed only of constants; a constant expression may be
 *          used as a case label.
 * @run main ConstantExpressions
 */

public class ConstantExpressions {
    static final int A = 1 + 2;              // constant expression
    static final int B = A * 3;              // constant expression
    static final int C = Integer.MAX_VALUE;  // constant variable

    public static void main(String[] args) {
        if (A != 3 || B != 9 || C != Integer.MAX_VALUE)
            throw new AssertionError("constant values");

        int r = 0;
        switch (B) {
            case A * 3: r = 1; break;   // constant expression as a case label
            default: r = 2;
        }
        if (r != 1) throw new AssertionError("constant case label");

        System.out.println("PASS");
    }
}
