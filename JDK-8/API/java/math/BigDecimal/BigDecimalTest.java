/*
 * @test
 * @summary java.math.BigDecimal API conformance: add/subtract/multiply/divide, setScale,
 *          stripTrailingZeros, compareTo/signum, valueOf, and scale.
 * @run main BigDecimalTest
 */

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBigDecimal() {
        BigDecimal a = new BigDecimal("10.00");
        BigDecimal b = new BigDecimal("3.0");
        check(a.add(b).equals(new BigDecimal("13.00")), "add");
        check(a.subtract(b).equals(new BigDecimal("7.00")), "subtract");
        check(a.multiply(b).equals(new BigDecimal("30.000")), "multiply");
        check(a.divide(b, 2, RoundingMode.HALF_UP).equals(new BigDecimal("3.33")),
              "divide with scale");
        check(a.setScale(0, RoundingMode.HALF_UP).equals(new BigDecimal("10")), "setScale");
        check(a.stripTrailingZeros().equals(new BigDecimal("1E+1")), "stripTrailingZeros");
        check(a.compareTo(b) > 0 && b.compareTo(a) < 0, "compareTo");
        check(a.signum() == 1 && BigDecimal.ZERO.signum() == 0, "signum");
        check(BigDecimal.valueOf(2.5).equals(new BigDecimal("2.5")), "valueOf double");
        check(a.scale() == 2, "scale");
    }

    public static void main(String[] args) {
        testBigDecimal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
