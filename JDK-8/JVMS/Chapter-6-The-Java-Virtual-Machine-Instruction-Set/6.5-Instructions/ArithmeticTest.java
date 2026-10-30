/*
 * @test
 * @summary JVMS 6.5: integer arithmetic instructions (iadd, idiv, irem, ishl);
 *          int addition wraps, integer division by zero throws
 *          ArithmeticException, and the remainder takes the dividend's sign.
 * @compile Arith.jasm
 * @run main ArithmeticTest
 */

public class ArithmeticTest {
    public static void main(String[] args) {
        if (Arith.iadd(2, 3) != 5) throw new AssertionError("iadd 2+3");
        if (Arith.iadd(Integer.MAX_VALUE, 1) != Integer.MIN_VALUE)
            throw new AssertionError("iadd overflow wrap");

        if (Arith.idiv(7, 2) != 3) throw new AssertionError("idiv 7/2");
        try {
            Arith.idiv(1, 0);
            throw new AssertionError("idiv by zero did not throw");
        } catch (ArithmeticException e) { }

        if (Arith.irem(-7, 2) != -1) throw new AssertionError("irem -7 % 2");
        if (Arith.ishl(1, 3) != 8) throw new AssertionError("ishl 1<<3");

        System.out.println("PASS");
    }
}
