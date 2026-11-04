/*
 * @test
 * @summary Property-based conformance supplement (jqwik 1.10.2, fixed seed):
 *          BigDecimal add/multiply commutativity.
 * @library /lib/jqwik-api-1.10.2.jar /lib/jqwik-engine-1.10.2.jar
 * @run main BigDecimalPropertyTest
 */

import java.math.BigDecimal;
import java.util.Random;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;

public class BigDecimalPropertyTest {
    static final long SEED = 20261104L;
    static final int SAMPLES = 300;
    static final BigDecimal LO = new BigDecimal("-1000000.000");
    static final BigDecimal HI = new BigDecimal("1000000.000");

    static int props;

    static void check(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    public static void main(String[] args) {
        Arbitrary<BigDecimal> arb = Arbitraries.bigDecimals().between(LO, HI);
        RandomGenerator<BigDecimal> gen = arb.generator(1000);

        // 1. add commutativity (numeric equality via compareTo; add's result
        //    scale is symmetric, but compareTo guards against scale drift)
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigDecimal a = gen.next(r).value();
                BigDecimal b = gen.next(r).value();
                check(a.add(b).compareTo(b.add(a)) == 0,
                      "BigDecimal.add commutativity failed at sample " + i + ": a=" + a + ", b=" + b);
            }
            props++;
        }

        // 2. multiply commutativity
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigDecimal a = gen.next(r).value();
                BigDecimal b = gen.next(r).value();
                check(a.multiply(b).compareTo(b.multiply(a)) == 0,
                      "BigDecimal.multiply commutativity failed at sample " + i);
            }
            props++;
        }

        System.out.println("PASS (" + props + " properties)");
    }
}
