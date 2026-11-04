/*
 * @test
 * @summary Property-based conformance supplement (jqwik 1.10.2, fixed seed):
 *          BigInteger add/multiply commutativity and associativity.
 * @library /lib/jqwik-api-1.10.2.jar /lib/jqwik-engine-1.10.2.jar
 * @run main BigIntegerPropertyTest
 */

import java.math.BigInteger;
import java.util.Random;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;

public class BigIntegerPropertyTest {
    // Deterministic hard requirement: every run uses the same seed and same
    // generator genSize, so the exact input sequence (and therefore outcome)
    // is identical across runs.
    static final long SEED = 20261104L;
    static final int SAMPLES = 300;
    static final BigInteger RANGE = new BigInteger("1000000000000"); // +/- 1e12

    static int props;

    static void check(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    public static void main(String[] args) {
        Arbitrary<BigInteger> arb = Arbitraries.bigIntegers().between(RANGE.negate(), RANGE);
        RandomGenerator<BigInteger> gen = arb.generator(1000);

        // 1. add commutativity: a + b == b + a
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                check(a.add(b).equals(b.add(a)),
                      "BigInteger.add commutativity failed at sample " + i + ": a=" + a + ", b=" + b);
            }
            props++;
        }

        // 2. add associativity: (a + b) + c == a + (b + c)
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                BigInteger c = gen.next(r).value();
                check(a.add(b).add(c).equals(a.add(b.add(c))),
                      "BigInteger.add associativity failed at sample " + i);
            }
            props++;
        }

        // 3. multiply commutativity: a * b == b * a
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                check(a.multiply(b).equals(b.multiply(a)),
                      "BigInteger.multiply commutativity failed at sample " + i);
            }
            props++;
        }

        // 4. multiply associativity: (a * b) * c == a * (b * c)
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                BigInteger c = gen.next(r).value();
                check(a.multiply(b).multiply(c).equals(a.multiply(b.multiply(c))),
                      "BigInteger.multiply associativity failed at sample " + i);
            }
            props++;
        }

        System.out.println("PASS (" + props + " properties)");
    }
}
