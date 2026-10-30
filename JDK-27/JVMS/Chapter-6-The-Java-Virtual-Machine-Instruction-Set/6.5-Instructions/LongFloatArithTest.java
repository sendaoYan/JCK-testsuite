/*
 * @test
 * @summary JVMS 6.5: remaining long and floating-point arithmetic instructions
 *          lsub/lmul/ldiv/lrem (long division truncates toward zero and throws
 *          on division by zero), and fsub/fmul/fneg/dsub/dmul/dneg.
 * @compile LongFloatArith.jasm
 * @run main LongFloatArithTest
 */

public class LongFloatArithTest {
    public static void main(String[] args) {
        if (LongFloatArith.lsub(10L, 3L) != 7L) throw new AssertionError("lsub");
        if (LongFloatArith.lmul(6L, 7L) != 42L) throw new AssertionError("lmul");
        if (LongFloatArith.ldiv(7L, 2L) != 3L) throw new AssertionError("ldiv truncate");
        if (LongFloatArith.lrem(-7L, 2L) != -1L) throw new AssertionError("lrem sign");
        try {
            LongFloatArith.ldiv(1L, 0L);
            throw new AssertionError("ldiv by zero did not throw");
        } catch (ArithmeticException e) { }

        if (LongFloatArith.fsub(5.5f, 1.5f) != 4.0f) throw new AssertionError("fsub");
        if (LongFloatArith.fmul(3.0f, 4.0f) != 12.0f) throw new AssertionError("fmul");
        if (LongFloatArith.fneg(5.0f) != -5.0f) throw new AssertionError("fneg");

        if (LongFloatArith.dsub(5.5, 1.5) != 4.0) throw new AssertionError("dsub");
        if (LongFloatArith.dmul(3.0, 4.0) != 12.0) throw new AssertionError("dmul");
        if (LongFloatArith.dneg(5.0) != -5.0) throw new AssertionError("dneg");

        System.out.println("PASS");
    }
}
