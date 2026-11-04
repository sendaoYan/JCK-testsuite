/*
 * @test
 * @summary JLS 16.1.6 (Java SE 8): the boolean operators &, | and ^ always
 *          evaluate both operands, so a variable assigned in the right operand is
 *          definitely assigned after the expression.
 * @run main BitwiseLogicalDA
 */

public class BitwiseLogicalDA {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int x;
        boolean a = false & ((x = 1) == 1);
        check(!a, "false & true == false");
        check(x == 1, "x assigned in right operand of &");

        int y;
        boolean o = true | ((y = 2) == 2);
        check(o, "true | true == true");
        check(y == 2, "y assigned in right operand of |");

        int z;
        boolean p = false ^ ((z = 3) == 3);
        check(p, "false ^ true == true");
        check(z == 3, "z assigned in right operand of ^");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
