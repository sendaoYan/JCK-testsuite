/*
 * @test
 * @summary java.math.MathContext API conformance: DECIMAL32/64/128 and UNLIMITED precisions,
 *          and the (int) / (int, RoundingMode) constructors.
 * @run main MathContextTest
 */

import java.math.MathContext;
import java.math.RoundingMode;

public class MathContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMathContext() {
        check(MathContext.DECIMAL32.getPrecision() == 7, "DECIMAL32 precision");
        check(MathContext.DECIMAL64.getPrecision() == 16, "DECIMAL64 precision");
        check(MathContext.DECIMAL128.getPrecision() == 34, "DECIMAL128 precision");
        check(MathContext.UNLIMITED.getPrecision() == 0, "UNLIMITED precision");
        check(new MathContext(10).getPrecision() == 10, "(int) ctor precision");
        check(new MathContext(10, RoundingMode.HALF_UP).getRoundingMode() == RoundingMode.HALF_UP,
              "(int, RoundingMode) ctor");
    }

    public static void main(String[] args) {
        testMathContext();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
