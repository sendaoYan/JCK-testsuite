/*
 * @test
 * @summary JVMS 6.5: long and floating-point arithmetic.  ladd wraps on
 *          overflow; fdiv/ddiv by zero yield Infinity rather than throwing.
 * @compile FloatArith.jasm
 * @run main FloatArithTest
 */

public class FloatArithTest {
    public static void main(String[] args) {
        if (FloatArith.ladd(Long.MAX_VALUE, 1L) != Long.MIN_VALUE)
            throw new AssertionError("ladd overflow wrap");
        if (FloatArith.fadd(1.5f, 2.25f) != 3.75f) throw new AssertionError("fadd");
        if (FloatArith.dadd(1.5, 2.25) != 3.75) throw new AssertionError("dadd");

        if (!Float.isInfinite(FloatArith.fdiv(1.0f, 0.0f))) throw new AssertionError("fdiv by zero");
        if (!Double.isInfinite(FloatArith.ddiv(1.0, 0.0))) throw new AssertionError("ddiv by zero");

        if (FloatArith.frem(7.5f, 2.0f) != 1.5f) throw new AssertionError("frem");

        System.out.println("PASS");
    }
}
